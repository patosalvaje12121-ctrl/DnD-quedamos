package com.example.dnd_quedamos.features.evento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.usuario.domain.usecase.ViewUserUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleEventoUiState(
    val cargando: Boolean = true,
    val evento: DataEvento? = null,
    val error: String? = null,
    // id de usuario -> nombre (organizador y participantes). Si no se encuentra, la UI pone "Usuario #id"
    val nombres: Map<Int, String> = emptyMap(),
)

/** Pantalla de detalle de un evento. Recibe el eventoId con koinViewModel { parametersOf(eventoId) }. */
class DetalleEventoViewModel(
    private val eventoId: Int,
    private val managerEventoUseCase: ManagerEventoUseCase,
    private val viewUserUseCase: ViewUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleEventoUiState())
    val uiState: StateFlow<DetalleEventoUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            managerEventoUseCase.ObtenerEvento(eventoId)
                .onSuccess { evento ->
                    _uiState.update { it.copy(cargando = false, evento = evento) }
                    cargarNombres(evento)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(cargando = false, error = e.message ?: "No se pudo cargar el evento")
                    }
                }
        }
    }

    private suspend fun cargarNombres(evento: DataEvento) {
        val ids = (listOf(evento.organizadorId) + evento.participantes).distinct()
        val nombres = ids.mapNotNull { id ->
            try {
                id to viewUserUseCase.obtenerPerfil(id).nombre
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null // si un usuario no se encuentra se muestra su id
            }
        }.toMap()
        _uiState.update { it.copy(nombres = nombres) }
    }
}
