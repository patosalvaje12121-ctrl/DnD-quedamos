package com.example.dnd_quedamos.features.mapa.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.mapa.domain.usecase.ManagerMapaUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapaUiState(
    val eventos: List<DataEvento> = emptyList(),
    val eventoSeleccionado: DataEvento? = null,
    val usuarioActualId: Int? = null,
    val mensajeFeedback: String? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class MapaViewModel(
    private val managerMapaUseCase: ManagerMapaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            val userId = managerMapaUseCase.usuarioActualId()
            _uiState.update { it.copy(usuarioActualId = userId) }
            cargarEventos()
        }
    }

    fun cargarEventos() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            managerMapaUseCase.obtenerEventos()
                .onSuccess { lista ->
                    _uiState.update { current ->
                        // Si hay un evento seleccionado, actualizamos sus plazas en tiempo real
                        val actualizado = current.eventoSeleccionado?.let { sel ->
                            lista.find { it.id == sel.id }
                        }
                        current.copy(
                            eventos = lista,
                            eventoSeleccionado = actualizado ?: current.eventoSeleccionado,
                            cargando = false
                        )
                    }
                }
                .onFailure { excepcion ->
                    _uiState.update { it.copy(error = excepcion.message, cargando = false) }
                }
        }
    }

    fun seleccionarEvento(evento: DataEvento) {
        _uiState.update { it.copy(eventoSeleccionado = evento) }
    }

    fun deseleccionarEvento() {
        _uiState.update { it.copy(eventoSeleccionado = null) }
    }

    // Apuntarse a evento
    fun unirseAEvento(eventoId: Int) {
        viewModelScope.launch {
            managerMapaUseCase.unirseEvento(eventoId)
                .onSuccess {
                    cargarEventos()
                    mostrarMensajeTemporal("¡Te has apuntado al evento!")
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    // Desapuntarse a evento
    fun abandonarEvento(eventoId: Int) {
        viewModelScope.launch {
            managerMapaUseCase.abandonarEvento(eventoId)
                .onSuccess {
                    cargarEventos()
                    mostrarMensajeTemporal("Te has desapuntado del evento")
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    // Mensaje de confirmacion por 2 segundos
    private fun mostrarMensajeTemporal(mensaje: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(mensajeFeedback = mensaje) }
            delay(2000)
            _uiState.update { it.copy(mensajeFeedback = null) }
        }
    }
}