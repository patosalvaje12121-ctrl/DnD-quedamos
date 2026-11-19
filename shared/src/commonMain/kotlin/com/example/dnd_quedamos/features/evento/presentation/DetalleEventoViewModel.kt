package com.example.dnd_quedamos.features.evento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.usuario.domain.usecase.ViewUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetalleEventoUiState(
    val evento: DataEvento? = null,              // null mientras carga
    val miId: Int? = null,                       // usuario con la sesión iniciada
    val nombres: Map<Int, String> = emptyMap(),  // id de usuario -> nombre
    val puedeGestionar: Boolean = false,         // organizador o administrador
    val error: String? = null,
)

/** Detalle de un evento. eventoId llega con koinViewModel { parametersOf(eventoId) }. */
class DetalleEventoViewModel(
    private val eventoId: Int,
    private val manager: ManagerEventoUseCase,
    private val currentUserProvider: CurrentUserProvider,
    private val viewUserUseCase: ViewUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleEventoUiState())
    val uiState: StateFlow<DetalleEventoUiState> = _uiState

    private fun ponerError(e: Throwable) {
        _uiState.value = _uiState.value.copy(error = e.message)
    }

    fun cargar() {
        viewModelScope.launch {
            val evento = manager.ObtenerEvento(eventoId).getOrElse {
                ponerError(it)
                return@launch
            }

            // Nombre del organizador y de cada participante (si alguno falla, se queda sin nombre)
            val nombres = mutableMapOf<Int, String>()
            for (id in listOf(evento.organizadorId) + evento.participantes) {
                runCatching { nombres[id] = viewUserUseCase.obtenerPerfil(id).nombre }
            }

            _uiState.value = DetalleEventoUiState(
                evento = evento,
                miId = currentUserProvider.currentUser()?.id,
                nombres = nombres,
                puedeGestionar = manager.PuedeGestionar(eventoId).getOrDefault(false),
            )
        }
    }

    fun apuntarse() {
        viewModelScope.launch {
            manager.UnirseEvento(eventoId).onSuccess { cargar() }.onFailure { ponerError(it) }
        }
    }

    fun desapuntarse() {
        viewModelScope.launch {
            manager.AbandonarEvento(eventoId).onSuccess { cargar() }.onFailure { ponerError(it) }
        }
    }

    fun borrar(onBorrado: () -> Unit) {
        viewModelScope.launch {
            manager.CancelarEvento(eventoId).onSuccess { onBorrado() }.onFailure { ponerError(it) }
        }
    }
}
