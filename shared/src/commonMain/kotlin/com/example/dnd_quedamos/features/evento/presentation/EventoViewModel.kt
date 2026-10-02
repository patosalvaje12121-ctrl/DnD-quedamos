package com.example.dnd_quedamos.features.evento.presentation

import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.usercase.ManagerEventoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.copy
import kotlin.random.Random

data class EventoUiState(
    val dataEventos: List<DataEvento>?,
)

class EventoViewModel(
    private val managerEventoUseCase: ManagerEventoUseCase
) {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(EventoUiState(listOf<DataEvento> ()))
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    fun inicializar(){
        _uiState.update { currentState ->
            currentState.copy(
                dataEventos = managerEventoUseCase.MostrarEvento(),
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