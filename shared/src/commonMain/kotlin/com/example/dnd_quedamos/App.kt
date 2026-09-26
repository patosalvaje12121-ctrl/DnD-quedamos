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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(objetosDeEjemplo) { objeto ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(objeto.nombre, fontWeight = FontWeight.Bold)
                            Text(objeto.descripcion, fontSize = 14.sp)
                        }
                    }
                }
            }


        }
    }
}
