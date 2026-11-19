@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.ui

import kotlin.time.ExperimentalTime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.evento.presentation.DetalleEventoViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Vista con toda la información de un evento. Se abre desde el botón "Más" de la tarjeta. */
@Composable
fun PantallaViewEvent(
    eventoId: Int,
    onBack: () -> Unit,
    onAbrirChat: (eventoId: Int) -> Unit,
    onEditar: (eventoId: Int) -> Unit,
    viewModel: DetalleEventoViewModel = koinViewModel { parametersOf(eventoId) },
) {
    // Se recarga cada vez que se entra (p. ej. al volver de editar)
    LaunchedEffect(Unit) { viewModel.cargar() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmarBorrado by remember { mutableStateOf(false) }
    val evento = uiState.evento
    val miId = uiState.miId
    fun nombre(id: Int) = uiState.nombres[id] ?: "Usuario #$id" // si no se encontró, se enseña el id

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Volver") }

        uiState.error?.let { Text("Error: $it") }

        if (evento == null) {
            Text("Cargando...")
        } else {
            Text(evento.nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Tipo: ${evento.tipoActividad}")
            Text("Fecha: ${formatearFecha(evento.fecha)}")
            Text("Ubicación: ${evento.ubicacion.direccion}")
            Text("Organizador: ${nombre(evento.organizadorId)}")
            Text("Plazas: ${evento.participantes.size}/${evento.maxParticipantes}")
            Text("Participantes: ${evento.participantes.joinToString { nombre(it) }}")
            Text("Descripción:", fontWeight = FontWeight.Bold)
            Text(evento.descripcion)

            // Apuntarse / desapuntarse (el organizador no ve el botón)
            if (miId != null && miId != evento.organizadorId) {
                if (miId in evento.participantes) {
                    Button(onClick = viewModel::desapuntarse) { Text("Desapuntarme") }
                } else {
                    Button(onClick = viewModel::apuntarse, enabled = !evento.estaCompleto) { Text("Apuntarme") }
                }
            }

            Button(onClick = { onAbrirChat(evento.id) }) { Text("Chat del evento") }

            // Solo el organizador o un administrador
            if (uiState.puedeGestionar) {
                Row {
                    TextButton(onClick = { onEditar(evento.id) }) { Text("Editar") }
                    TextButton(onClick = { confirmarBorrado = true }) { Text("Borrar") }
                }
            }
        }
    }

    if (confirmarBorrado) {
        ConfirmarBorrado(
            onConfirmar = { confirmarBorrado = false; viewModel.borrar(onBorrado = onBack) },
            onCancelar = { confirmarBorrado = false },
        )
    }
}

