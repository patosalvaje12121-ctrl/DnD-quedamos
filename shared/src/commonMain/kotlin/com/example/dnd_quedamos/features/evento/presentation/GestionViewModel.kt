package com.example.dnd_quedamos.features.evento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.usuario.domain.usecase.UserPermissionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GestionUiState(
    val eventos: List<DataEvento> = emptyList(),
    val error: String? = null,
)

/** Pantalla Gestión: eventos que el usuario puede editar y borrar. */
class GestionViewModel(
    private val managerEventoUseCase: ManagerEventoUseCase,
    private val permisos: UserPermissionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionUiState())
    val uiState: StateFlow<GestionUiState> = _uiState

    /** Administrador -> todos los eventos. Usuario normal -> solo los que ha creado. */
    fun cargar() {
        viewModelScope.launch {
            val esAdmin = runCatching { permisos.esAdmin() }.getOrDefault(false)
            val resultado = if (esAdmin) managerEventoUseCase.MostrarEvento() else managerEventoUseCase.MisEventos()
            resultado
                .onSuccess { _uiState.value = GestionUiState(eventos = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun borrar(eventoId: Int) {
        viewModelScope.launch {
            managerEventoUseCase.CancelarEvento(eventoId)
                .onSuccess { cargar() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}
