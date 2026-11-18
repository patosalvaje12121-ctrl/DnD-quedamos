package com.example.dnd_quedamos.features.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.DataMensaje
import com.example.dnd_quedamos.features.chat.domain.usecase.ManagerChatUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de envío de un mensaje tal y como se ve en pantalla. */
enum class EstadoEnvio { ENVIADO, ENVIANDO, ERROR }

/** Mensaje + su estado de envío. Los pendientes usan ids negativos temporales. */
data class MensajeUi(
    val mensaje: DataMensaje,
    val estado: EstadoEnvio = EstadoEnvio.ENVIADO,
)

data class ChatUiState(
    val chat: DataChat? = null,
    val mensajes: List<MensajeUi> = emptyList(), // del más antiguo al más reciente
    val input: String = "",
    val currentUserId: Int? = null,
    val cargando: Boolean = true,
    val error: String? = null,                    // para el snackbar (se borra con errorMostrado())
)

class ChatViewModel(
    private val chatId: Int,
    private val useCase: ManagerChatUseCase,
    private val currentUser: CurrentUserProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /** Lo último que dijo el servidor. */
    private var delServidor: List<DataMensaje> = emptyList()

    /** Mensajes ya confirmados por POST que quizá el último sondeo todavía no incluía. */
    private val confirmados = mutableListOf<DataMensaje>()

    /** Mensajes enviándose o que fallaron (id temporal negativo). */
    private val pendientes = mutableListOf<MensajeUi>()
    private var siguienteIdTemporal = -1

    init {
        viewModelScope.launch {
            val user = currentUser.currentUser()
            _uiState.update { it.copy(currentUserId = user?.id) }
            useCase.getChat(chatId)
                .onSuccess { chat -> _uiState.update { it.copy(chat = chat) } }
                .onFailure { _uiState.update { it.copy(error = "No se pudo cargar el chat") } }
        }

        // Polling: pide los mensajes cada 3 segundos mientras la pantalla esté abierta.
        // El bucle termina solo: al salir del chat se cancela viewModelScope y delay() deja de esperar.
        viewModelScope.launch {
            while (true) {
                useCase.getMensajes(chatId)
                    .onSuccess { lista ->
                        delServidor = lista
                        val ids = lista.mapTo(HashSet()) { it.id }
                        confirmados.removeAll { it.id in ids }
                        publicar(cargando = false)
                    }
                    .onFailure {
                        // Un fallo puntual no corta el chat: el siguiente sondeo lo reintenta
                        if (_uiState.value.cargando) {
                            _uiState.update { it.copy(cargando = false, error = "Sin conexión con el servidor") }
                        }
                    }
                delay(3_000)
            }
        }
    }

    fun onInputChange(texto: String) {
        _uiState.update { it.copy(input = texto) }
    }

    fun enviar() {
        val texto = _uiState.value.input.trim()
        val autorId = _uiState.value.currentUserId ?: return
        if (texto.isEmpty()) return

        val pendiente = MensajeUi(
            mensaje = DataMensaje(
                id = siguienteIdTemporal--,
                chatId = chatId,
                autorId = autorId,
                autorNombre = "",
                texto = texto,
                timestamp = (delServidor.lastOrNull()?.timestamp ?: 0L) + 1, // para el orden
            ),
            estado = EstadoEnvio.ENVIANDO,
        )
        pendientes += pendiente
        _uiState.update { it.copy(input = "") }
        publicar()
        mandar(pendiente)
    }

    /** Reintenta un mensaje que falló (se toca sobre él en la UI). */
    fun reintentar(idTemporal: Int) {
        val i = pendientes.indexOfFirst { it.mensaje.id == idTemporal }
        if (i < 0) return
        val p = pendientes[i].copy(estado = EstadoEnvio.ENVIANDO)
        pendientes[i] = p
        publicar()
        mandar(p)
    }

    fun errorMostrado() {
        _uiState.update { it.copy(error = null) }
    }

    private fun mandar(p: MensajeUi) {
        viewModelScope.launch {
            useCase.enviarMensaje(chatId, p.mensaje.autorId, p.mensaje.texto)
                .onSuccess { real ->
                    pendientes.removeAll { it.mensaje.id == p.mensaje.id }
                    if (delServidor.none { it.id == real.id }) confirmados += real
                    publicar()
                }
                .onFailure {
                    val i = pendientes.indexOfFirst { it.mensaje.id == p.mensaje.id }
                    if (i >= 0) pendientes[i] = pendientes[i].copy(estado = EstadoEnvio.ERROR)
                    _uiState.update { it.copy(error = "No se pudo enviar el mensaje") }
                    publicar()
                }
        }
    }

    /** Une servidor + confirmados + pendientes (sin duplicados) y lo publica en el estado. */
    private fun publicar(cargando: Boolean = _uiState.value.cargando) {
        val enviados = (delServidor + confirmados)
            .distinctBy { it.id }
            .sortedWith(compareBy({ it.timestamp }, { it.id }))
            .map { MensajeUi(it) }
        _uiState.update { it.copy(mensajes = enviados + pendientes, cargando = cargando) }
    }
}
