package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeEventViewRoot(modifier: Modifier = Modifier,
                      onAbrirChat: (eventoId: Int) -> Unit = {},
                      viewModel: EventoViewModel = koinViewModel()
) {
    // 1. Inicializa el ViewModel de forma segura una sola vez
    LaunchedEffect(Unit) {
        viewModel.inicializar()
    }

    // 2. Observa el estado de la UI de manera reactiva
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 3. Pintamos la lista directamente aquí dentro de la función Composable
    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                    TextButton(onClick = { onAbrirChat(evento.id) }) { Text("💬 Chat") }
                }
            }
        }
    }
}