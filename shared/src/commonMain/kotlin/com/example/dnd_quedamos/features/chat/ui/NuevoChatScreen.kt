package com.example.dnd_quedamos.features.chat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.chat.presentation.NuevoChatViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Nuevo chat": buscador + lista de usuarios. Al tocar uno se llama a [onElegir] con su id
 * y App.kt navega a Route.ChatWith, que abre (o crea) el chat directo.
 */
@Composable
fun NuevoChatScreen(
    onElegir: (userId: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NuevoChatViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        // Cabecera
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", fontSize = 20.sp) }
            Text("Nuevo chat", fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()

        OutlinedTextField(
            value = uiState.busqueda,
            onValueChange = viewModel::onBusquedaChange,
            placeholder = { Text("Buscar usuario…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.cargando ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                uiState.error != null -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.error!!)
                    Button(onClick = viewModel::cargar) { Text("Reintentar") }
                }

                uiState.filtrados.isEmpty() ->
                    Text("No hay usuarios que coincidan", Modifier.align(Alignment.Center))

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.filtrados, key = { it.id }) { contacto ->
                        Card(Modifier.fillMaxWidth().clickable { onElegir(contacto.id) }) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("👤", fontSize = 22.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(contacto.nombre, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
