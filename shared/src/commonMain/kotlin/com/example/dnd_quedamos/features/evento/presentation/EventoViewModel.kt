@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.evento.ui.textoFecha
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.ExperimentalTime

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
                    todos = ret
                    _uiState.update {
                        it.copy(dataEventos=ret)
                    }
                }
                .onFailure { exception -> /*throw exception*/ }
        }

    }

    // lista sin filtrar, para poder quitar el filtro sin volver a pedirla
    private var todos = listOf<DataEvento>()

    // Filtra con los campos del formulario de crear evento. Los vacios no cuentan.
    // TODO: el tipo siempre viene marcado (ROL por defecto) asi que siempre filtra por tipo.
    //  Habria que poder dejarlo sin marcar (CamposEvento.tipo nullable)
    // TODO: la hora y la direccion de momento se ignoran
    // TODO: latitud y longitud hay que escribirlas a mano, estaria bien cogerlas del mapa
    fun filtrar(campos: CamposEvento) {
        val lat = campos.latitud.toDoubleOrNull()
        val lon = campos.longitud.toDoubleOrNull()
        val plazas = campos.plazas.toIntOrNull()

        val filtrados = todos.filter { e ->
            (campos.nombre.isBlank() || e.nombre.contains(campos.nombre.trim(), ignoreCase = true)) &&
            (campos.descripcion.isBlank() || e.descripcion.contains(campos.descripcion.trim(), ignoreCase = true)) &&
            e.tipoActividad == campos.tipo &&
            (campos.fecha.isBlank() || textoFecha(e.fecha) == campos.fecha.trim()) &&
            (plazas == null || e.plazasLibres >= plazas) &&
            (lat == null || lon == null || distanciaKm(lat, lon, e.ubicacion.latitud, e.ubicacion.longitud) <= 10.0)
        }
        _uiState.update { it.copy(dataEventos = filtrados) }
    }

    fun quitarFiltro() {
        _uiState.update { it.copy(dataEventos = todos) }
    }

    // formula de haversine
    private fun distanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI / 180
        val dLon = (lon2 - lon1) * PI / 180
        val a = sin(dLat / 2).pow(2) + cos(lat1 * PI / 180) * cos(lat2 * PI / 180) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a))
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