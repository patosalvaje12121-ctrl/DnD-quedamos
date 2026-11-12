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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.TipoChat
import com.example.dnd_quedamos.features.chat.presentation.ChatListUiState
import com.example.dnd_quedamos.features.chat.presentation.ChatListViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Pantalla de "📜 Partidas": lista de chats directos y de eventos. */
@Composable
fun ChatListScreen(
    onAbrirChat: (chatId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = koinViewModel(),
) {
    // Se ejecuta cada vez que la pantalla entra en composición (también al volver de un chat)
    LaunchedEffect(Unit) { viewModel.cargar() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier.fillMaxSize()) {
        when (val s = uiState) {
            ChatListUiState.Cargando ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is ChatListUiState.Error -> Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(s.mensaje)
                Spacer(Modifier.padding(4.dp))
                Button(onClick = viewModel::cargar) { Text("Reintentar") }
            }

            is ChatListUiState.Listo ->
                if (s.chats.isEmpty()) {
                    Text(
                        "No tienes chats todavía.\nAbre el chat de un evento desde Inicio.",
                        Modifier.align(Alignment.Center).padding(24.dp),
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(s.chats, key = { it.id }) { chat ->
                            ChatRow(chat, onClick = { onAbrirChat(chat.id) })
                        }
                    }
                }
        }
    }
}

@Composable
private fun ChatRow(chat: DataChat, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (chat.tipo == TipoChat.EVENTO) "🎲" else "👤", fontSize = 26.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(chat.nombre, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val ultimo = chat.ultimoMensaje
                Text(
                    text = when {
                        ultimo == null -> "Sin mensajes"
                        chat.tipo == TipoChat.EVENTO -> "${ultimo.autorNombre}: ${ultimo.texto}"
                        else -> ultimo.texto
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            chat.ultimoMensaje?.let {
                Spacer(Modifier.width(8.dp))
                Text(
                    formatearHora(it.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
