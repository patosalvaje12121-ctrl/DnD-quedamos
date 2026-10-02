package com.example.dnd_quedamos.features.evento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.usercase.ManagerEventoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.copy
import kotlin.random.Random
import kotlin.time.Clock

data class EventoUiState(
    val dataEventos: List<DataEvento>,
)

class EventoViewModel(
    private val managerEventoUseCase: ManagerEventoUseCase
): ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(EventoUiState(listOf<DataEvento> ()))
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    fun inicializar(){/*
        managerEventoUseCase.MostrarEvento()
            .onSuccess { currentState -> _uiState.copy
            _uiState.update { currentState ->
                currentState.copy(
                    dataEventos = ,
                )
            }
        }*/

        viewModelScope.launch {
            managerEventoUseCase.MostrarEvento()
                .onSuccess {
                    ret ->
                    _uiState.update {
                        it.copy(dataEventos=ret)
                    }
                }
                .onFailure { exception -> /*throw exception*/ }
        }

    }


    // Handle business logic
//    fun cambiar(numID: Int, _nombre : String, _descripcion : String) {
//        _uiState.update { currentState ->
//            currentState.copy(
//                id = numID,
//                nombre = _nombre,
//                descripcion = _descripcion,
//            )
//        }
//    }
}