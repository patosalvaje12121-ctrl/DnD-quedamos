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
    val evento: DataEvento? = null, // null mientras carga
    val miId: Int? = null,          // usuario con la sesión iniciada
    val nombres: Map<Int, String> = emptyMap(), // id de usuario -> nombre
    val puedeGestionar: Boolean = false, // organizador o administrador: ve Editar y Borrar
    val error: String? = null,
)

/** Detalle de un evento. Recibe el eventoId con koinViewModel { parametersOf(eventoId) }. */
class DetalleEventoViewModel(
    private val eventoId: Int,
    private val managerEventoUseCase: ManagerEventoUseCase,
    private val currentUserProvider: CurrentUserProvider,
    private val viewUserUseCase: ViewUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleEventoUiState())
    val uiState: StateFlow<DetalleEventoUiState> = _uiState

    // cargar() lo llama la pantalla cada vez que se entra (así se ve actualizado al volver de editar)
    fun cargar() {
        viewModelScope.launch {
            val miId = currentUserProvider.currentUser()?.id
            managerEventoUseCase.ObtenerEvento(eventoId)
                .onSuccess { evento ->
                    // Nombre del organizador y de cada participante (si falla alguno, no se pone)
                    val ids = listOf(evento.organizadorId) + evento.participantes
                    val nombres = mutableMapOf<Int, String>()
                    for (id in ids) {
                        runCatching { viewUserUseCase.obtenerPerfil(id) }
                            .onSuccess { nombres[id] = it.nombre }
                    }
                    val puedeGestionar = managerEventoUseCase.PuedeGestionar(eventoId).getOrDefault(false)
                    _uiState.value = DetalleEventoUiState(
                        evento = evento, miId = miId, nombres = nombres, puedeGestionar = puedeGestionar,
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun apuntarse() {
        viewModelScope.launch {
            managerEventoUseCase.UnirseEvento(eventoId)
                .onSuccess { cargar() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun desapuntarse() {
        viewModelScope.launch {
            managerEventoUseCase.AbandonarEvento(eventoId)
                .onSuccess { cargar() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    /** Borra el evento. Si va bien llama a [onBorrado] (normalmente, volver atrás). */
    fun borrar(onBorrado: () -> Unit) {
        viewModelScope.launch {
            managerEventoUseCase.CancelarEvento(eventoId)
                .onSuccess { onBorrado() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}
