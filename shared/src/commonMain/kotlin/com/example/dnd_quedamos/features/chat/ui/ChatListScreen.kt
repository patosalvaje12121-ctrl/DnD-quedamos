package com.example.dnd_quedamos.features.chat.ui

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
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
import com.example.dnd_quedamos.core.ui.componentes.DndBoton
import com.example.dnd_quedamos.core.ui.componentes.DndPanel
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.TipoChat
import com.example.dnd_quedamos.features.chat.presentation.ChatListUiState
import com.example.dnd_quedamos.features.chat.presentation.ChatListViewModel
import com.example.dnd_quedamos.features.usuario.presentation.ViewUserViewModel
import com.example.dnd_quedamos.features.usuario.ui.AvatarUsuario
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
                        contentPadding = PaddingValues(start = 0.dp, top = 16.dp, end = 0.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {

                        items(s.chats, key = { it.id }) { chat ->
                            val otroUsuarioId = chat.participantes
                                .firstOrNull { it != s.currentUserID }

                            ChatRow(
                                chat = chat,
                                onClick = { onAbrirChat(chat.id) },
                                userId = otroUsuarioId,
                            )
                        }
                    }
                }
        }

        // Botón flotante para empezar un chat directo con alguien nuevo
        DndBoton(
            onClick = onNuevoChat,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Text("✏️ Nuevo chat") }
    }
}

@Composable
private fun ChatRow(chat: DataChat, onClick: () -> Unit = {}, userId: Int?) {

    if (chat.tipo == TipoChat.EVENTO || userId == null) {
        ChatRowLayout(chat = chat, onClick = onClick, imageID = null)
        return
    }

    val viewUserViewModel: ViewUserViewModel = koinViewModel(
        key = "chat_user_$userId",   // one VM per peer user
    ) { parametersOf(userId) }       // load THAT user, not the current one

    val perfil by viewUserViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewUserViewModel) { viewUserViewModel.inicializar() }

    ChatRowLayout(
        chat = chat,
        onClick = onClick,
        imageID = perfil.dataUsuario?.imageID,
    )
}

@Composable
private fun ChatRowLayout(
    chat: DataChat,
    onClick: () -> Unit,
    imageID: Int?,
) {
    Row (modifier = Modifier.fillMaxWidth())
    {
        DndPanel (
            modifier = Modifier.fillMaxWidth(),
            skin = DndSkins.filaChat,
            onClick = onClick,
            trailing = {
                chat.ultimoMensaje?.let {
                    Text(
                        text = formatearHora(it.timestamp),
                        fontSize = 18.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        ) {
            ChatRowContent(chat, imageID)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ChatRowContent(chat: DataChat, imageID: Int?) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (chat.tipo != TipoChat.EVENTO)
        {
            if (imageID != null) {
                AvatarUsuario(imageID, size = 55.dp)
            }
            else
                LoadingIndicator()
        }
        else
            Text("🎲", fontSize = 26.sp)

        Spacer(Modifier.width(12.dp))
        Column(Modifier.fillMaxWidth()) {
            Text(chat.nombre, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val ultimo = chat.ultimoMensaje
            Text(
                text = when {
                    ultimo == null -> "Sin mensajes"
                    else -> "${ultimo.autorNombre}: ${ultimo.texto}"
                },
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
