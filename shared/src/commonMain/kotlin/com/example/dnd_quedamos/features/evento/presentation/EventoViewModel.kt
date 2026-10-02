package com.example.dnd_quedamos.features.evento.presentation

import com.example.dnd_quedamos.features.evento.domain.usercase.ManagerEventoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.copy
import kotlin.random.Random

data class EventoUiState(
    val id: Int = 0,
    val nombre : String? = null,
    val descripcion: String? = null,
)

class EventoViewModel(
    private val managerEventoUseCase: ManagerEventoUseCase
) {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(EventoUiState())
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    fun inicializar(){
        _uiState.update { currentState ->
            currentState.copy(
                id = managerEventoUseCase,
                nombre = _nombre,
                descripcion = _descripcion,
            )
        }
    }




    // Handle business logic
    fun cambiar(numID: Int, _nombre : String, _descripcion : String) {
        _uiState.update { currentState ->
            currentState.copy(
                id = numID,
                nombre = _nombre,
                descripcion = _descripcion,
            )
        }
    }
}