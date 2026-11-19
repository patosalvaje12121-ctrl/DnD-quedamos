@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.model.DataUbicacion
import com.example.dnd_quedamos.features.evento.domain.model.TipoActividad
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.evento.ui.errorFechaHora
import com.example.dnd_quedamos.features.evento.ui.leerFecha
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

data class FormularioEventoUiState(
    val listo: Boolean = false,        // false mientras se carga el evento a editar
    val original: DataEvento? = null,  // evento que se edita (null = crear uno nuevo)
    val error: String? = null,
)

/** Textos tal cual los escribe el usuario en el formulario. */
data class CamposEvento(
    val nombre: String,
    val descripcion: String,
    val tipo: TipoActividad,
    val fecha: String,       // "dd/MM/yyyy"
    val hora: String,        // "HH:mm"
    val direccion: String,
    val latitud: String,
    val longitud: String,
    val plazas: String,
)

/**
 * Formulario para crear o editar un evento.
 * eventoId = -1 -> crear. Otro valor -> editar ese evento.
 */
class FormularioEventoViewModel(
    private val eventoId: Int,
    private val managerEventoUseCase: ManagerEventoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioEventoUiState())
    val uiState: StateFlow<FormularioEventoUiState> = _uiState

    init {
        if (eventoId == -1) {
            _uiState.value = FormularioEventoUiState(listo = true)
        } else {
            viewModelScope.launch {
                managerEventoUseCase.ObtenerEvento(eventoId)
                    .onSuccess { _uiState.value = FormularioEventoUiState(listo = true, original = it) }
                    .onFailure { _uiState.value = FormularioEventoUiState(error = it.message) }
            }
        }
    }

    /** Convierte los textos y guarda. Si todo va bien llama a [onGuardado]. */
    fun guardar(campos: CamposEvento, onGuardado: () -> Unit) {
        val fecha = leerFecha(campos.fecha, campos.hora)
        val latitud = campos.latitud.toDoubleOrNull()
        val longitud = campos.longitud.toDoubleOrNull()
        val plazas = campos.plazas.toIntOrNull()

        // Se borra el error del intento anterior
        _uiState.value = _uiState.value.copy(error = null)

        // Errores de formato. El resto (fecha pasada, nombre vacío...) los comprueba el caso de uso
        val errorFormato = when {
            fecha == null -> errorFechaHora(campos.fecha, campos.hora)
            latitud == null || longitud == null -> "Latitud y longitud tienen que ser números"
            plazas == null -> "Las plazas tienen que ser un número"
            else -> null
        }
        if (errorFormato != null) {
            _uiState.value = _uiState.value.copy(error = errorFormato)
            return
        }

        val ubicacion = DataUbicacion(campos.direccion.trim(), latitud!!, longitud!!)
        val original = _uiState.value.original

        viewModelScope.launch {
            val resultado = if (original == null) {
                // id y organizador los pone el servidor / el caso de uso
                managerEventoUseCase.CrearEvento(
                    DataEvento(
                        id = 0,
                        nombre = campos.nombre,
                        descripcion = campos.descripcion,
                        fecha = fecha!!,
                        ubicacion = ubicacion,
                        tipoActividad = campos.tipo,
                        organizadorId = 0,
                        maxParticipantes = plazas!!,
                    )
                )
            } else {
                // copy: conserva id, organizador y participantes
                managerEventoUseCase.EditarEvento(
                    original.copy(
                        nombre = campos.nombre,
                        descripcion = campos.descripcion,
                        fecha = fecha!!,
                        ubicacion = ubicacion,
                        tipoActividad = campos.tipo,
                        maxParticipantes = plazas!!,
                    )
                )
            }
            resultado
                .onSuccess { onGuardado() }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}
