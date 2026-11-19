@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.evento.presentation.DetalleEventoViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

/** Detalle de un evento. Se abre desde el botón "Más" de la tarjeta. */
@Composable
fun PantallaViewEvent(
    eventoId: Int,
    onBack: () -> Unit,
    onAbrirChat: (eventoId: Int) -> Unit,
    onEditar: (eventoId: Int) -> Unit,
    viewModel: DetalleEventoViewModel = koinViewModel { parametersOf(eventoId) },
) {
    LaunchedEffect(Unit) { viewModel.cargar() } // cada vez que se entra (p. ej. al volver de editar)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val evento = uiState.evento
    var confirmarBorrado by remember { mutableStateOf(false) }

    fun nombre(id: Int) = uiState.nombres[id] ?: "Usuario #$id"

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Button(onClick = onBack) { Text("Volver") }

        if (uiState.error != null) Text("Error: ${uiState.error}")

        if (evento == null) {
            Text("Cargando...")
        } else {
            Text(evento.nombre)
            Text("Tipo: ${evento.tipoActividad}")
            Text("Fecha: ${formatearFecha(evento.fecha)}")
            Text("Ubicación: ${evento.ubicacion.direccion}")
            Text("Organizador: ${nombre(evento.organizadorId)}")
            Text("Plazas: ${evento.participantes.size}/${evento.maxParticipantes}")
            Text("Participantes: ${evento.participantes.joinToString { nombre(it) }}")
            Text("Descripción: ${evento.descripcion}")

            val miId = uiState.miId
            val apuntado = miId in evento.participantes
            val puedeApuntarse = miId != null && miId != evento.organizadorId // el organizador no

            if (puedeApuntarse && apuntado) {
                Button(onClick = viewModel::desapuntarse) { Text("Desapuntarme") }
            }
            if (puedeApuntarse && !apuntado && !evento.estaCompleto) {
                Button(onClick = viewModel::apuntarse) { Text("Apuntarme") }
            }

            Button(onClick = { onAbrirChat(evento.id) }) { Text("Chat del evento") }

            if (uiState.puedeGestionar) {
                Button(onClick = { onEditar(evento.id) }) { Text("Editar") }
                Button(onClick = { confirmarBorrado = true }) { Text("Borrar") }
            }
        }
    }

    if (confirmarBorrado) {
        ConfirmarBorrado(
            onConfirmar = { confirmarBorrado = false; viewModel.borrar(onBack) },
            onCancelar = { confirmarBorrado = false },
        )
    }
}
