@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.ui

import kotlin.time.ExperimentalTime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.evento.presentation.GestionViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Lista de eventos que puedes gestionar, con botones para crear, editar y borrar. */
@Composable
fun PantallaGestion(
    onCrear: () -> Unit,
    onEditar: (eventoId: Int) -> Unit,
    viewModel: GestionViewModel = koinViewModel(),
) {
    // Se recarga cada vez que se entra (p. ej. al volver del formulario)
    LaunchedEffect(Unit) { viewModel.cargar() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var borrarId by remember { mutableStateOf<Int?>(null) } // evento pendiente de confirmar

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onCrear, modifier = Modifier.fillMaxWidth()) { Text("+ Crear evento") }

        uiState.error?.let { Text("Error: $it") }
        if (uiState.eventos.isEmpty()) Text("No tienes eventos que gestionar")

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.eventos) { evento ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(evento.nombre, fontWeight = FontWeight.Bold)
                        Text(formatearFecha(evento.fecha))
                        Row {
                            TextButton(onClick = { onEditar(evento.id) }) { Text("Editar") }
                            TextButton(onClick = { borrarId = evento.id }) { Text("Borrar") }
                        }
                    }
                }
            }
        }
    }

    borrarId?.let { id ->
        ConfirmarBorrado(
            onConfirmar = { viewModel.borrar(id); borrarId = null },
            onCancelar = { borrarId = null },
        )
    }
}
