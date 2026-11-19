package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeEventViewRoot(modifier: Modifier = Modifier,
                      onVerMas: (eventoId: Int) -> Unit = {},
                      viewModel: EventoViewModel = koinViewModel()
) {
    // 1. Inicializa el ViewModel de forma segura una sola vez
    LaunchedEffect(Unit) {
        viewModel.inicializar()
    }

    // 2. Observa el estado de la UI de manera reactiva
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarFiltro by remember { mutableStateOf(false) }

    if (mostrarFiltro) {
        // reutilizamos el formulario de crear evento para filtrar
        Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = { mostrarFiltro = false }) { Text("← Volver") }
            Formulario(
                original = null,
                error = null,
                onGuardar = { campos ->
                    viewModel.filtrar(campos)
                    mostrarFiltro = false
                },
                titulo = "Filtrar eventos",
                textoBoton = "Filtrar",
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { mostrarFiltro = true }) { Text("Filtrar") }
            TextButton(onClick = { viewModel.quitarFiltro() }) { Text("Quitar filtro") }
        }

        // 3. Pintamos la lista directamente aquí dentro de la función Composable
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.dataEventos) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Ajusta estos campos según las propiedades reales de tu clase 'DataEvento'
                        Text(evento.nombre, fontWeight = FontWeight.Bold)
                        Text(evento.descripcion, fontSize = 14.sp)
                        TextButton(onClick = { onVerMas(evento.id) }) { Text("➕ Más") }
                    }
                }
            }
        }
    }
}
