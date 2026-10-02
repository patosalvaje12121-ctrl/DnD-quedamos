package com.example.dnd_quedamos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.usercase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel

data class Objeto(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

// Lista fija de ejemplo, con pocos elementos
val objetosDeEjemplo = (1..30).map { i ->
    Objeto(id = i, nombre = "Objeto $i", descripcion = "Descripción de ejemplo $i")
}

@Composable
fun BottomNavButton(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            //.clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(label, fontSize = 12.sp, color = color)
    }
}
@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme() // DnD = dark fantasy vibe
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "DnD Quedamos",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            },

            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BottomNavButton(emoji = "🏠", label = "Inicio", selected = true) { }
                    BottomNavButton(emoji = "🧙", label = "Personajes", selected = false) { }
                    BottomNavButton(emoji = "📜", label = "Partidas", selected = false) { }
                    BottomNavButton(emoji = "⚙️", label = "Ajustes", selected = false) { }
                }
            }



        ) { innerPadding ->
            EventoScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

// Use the 'viewModel()' function from the lifecycle-viewmodel-compose artifact
@Composable
fun EventoScreen(
    modifier: Modifier = Modifier,
    viewModel: EventoViewModel = EventoViewModel(ManagerEventoUseCase())
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
                }
            }
        }
    }
}