package com.example.dnd_quedamos.features.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.min
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutBoundsHolder
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutBounds
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.TipoChat
import com.example.dnd_quedamos.features.chat.presentation.ChatListUiState
import com.example.dnd_quedamos.features.chat.presentation.ChatListViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/** Pantalla de "📜 Partidas": lista de chats directos y de eventos. */
@Composable
fun ChatListScreen(
    onAbrirChat: (chatId: Int) -> Unit,
    onNuevoChat: () -> Unit,
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
                        "No tienes chats todavía.\nPulsa ✏️ para empezar una conversación.",
                        Modifier.align(Alignment.Center).padding(24.dp),
                    )
                } else {
                    LazyColumn(
                        // abajo deja sitio para que el botón ✏️ no tape el último chat
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(s.chats, key = { it.id }) { chat ->
                            ChatRow(chat, onClick = { onAbrirChat(chat.id) })
                        }
                    }
                }
        }

        // Botón flotante para empezar un chat directo con alguien nuevo
        ExtendedFloatingActionButton(
            onClick = onNuevoChat,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Text("✏️ Nuevo chat") }
    }
}

@Composable
private fun ChatRow(chat: DataChat, onClick: () -> Unit = {}) {
    val painter = painterResource(DndSkins.panelFilaChat.res)
    val intrinsic = painter.intrinsicSize

    Card(Modifier.fillMaxWidth().sizeIn(10.dp, 24.dp, 800.dp, 120.dp ).clickable(onClick = onClick).background(Color.Unspecified)) {
        if (intrinsic.isSpecified && intrinsic.width > 0f && intrinsic.height > 0f) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val widthPx          = constraints.maxWidth.toFloat()
                // natural height at FillWidth scale:
                val scaledHeightPx   = intrinsic.height * (widthPx / intrinsic.width)
                // cap at the drawable's own pixel height (no vertical up-scaling):
                val cappedHeightPx   = min(scaledHeightPx, intrinsic.height)
                val cappedHeightDp   = with(LocalDensity.current) { cappedHeightPx.toDp() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cappedHeightDp)     // ← the new constraint
                        .paint(
                            painter     = painter,
                            contentScale = ContentScale.FillWidth,
                        )
                ) {
                    ChatRowContent(chat)
                }
            }
        } else {
            // Fallback: painter exposes no intrinsics (e.g. vector without a size)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .paint(painter = painter, contentScale = ContentScale.FillWidth)
            ) {
                ChatRowContent(chat)
            }
        }
    }
}

@Composable
private fun ChatRowContent(chat: DataChat) {
    Row(
        modifier = Modifier.padding(36.dp, 24.dp, 64.dp, 6.dp ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (chat.tipo == TipoChat.EVENTO) "🎲" else "👤", fontSize = 26.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(chat.nombre, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.secondaryContainer)
            val ultimo = chat.ultimoMensaje
            Text(
                text = when {
                    ultimo == null -> "Sin mensajes"
                    chat.tipo == TipoChat.EVENTO -> "${ultimo.autorNombre}: ${ultimo.texto}"
                    else -> ultimo.texto
                },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        chat.ultimoMensaje?.let {
            Spacer(Modifier.width(8.dp))
            Text(
                formatearHora(it.timestamp),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}
