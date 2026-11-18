package com.example.dnd_quedamos.features.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.TipoChat
import com.example.dnd_quedamos.features.chat.domain.usecase.ManagerChatUseCase
import com.example.dnd_quedamos.features.chat.presentation.ChatViewModel
import com.example.dnd_quedamos.features.chat.presentation.EstadoEnvio
import com.example.dnd_quedamos.features.chat.presentation.MensajeUi
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Conversación: cabecera, burbujas de mensajes y barra para escribir. */
@Composable
fun ChatScreen(
    chatId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel { parametersOf(chatId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // La lista está invertida: el índice 0 es el mensaje más reciente (abajo del todo)
    val mensajes = uiState.mensajes.asReversed()

    // Al llegar mensajes nuevos, bajar solo si el usuario ya estaba abajo (no le movemos si lee arriba)
    LaunchedEffect(mensajes.firstOrNull()?.mensaje?.id) {
        if (listState.firstVisibleItemIndex <= 1) listState.animateScrollToItem(0)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbar.showSnackbar(it)
            viewModel.errorMostrado()
        }
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().imePadding()) {
            // Cabecera
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("←", fontSize = 20.sp) }
                Text(if (uiState.chat?.tipo == TipoChat.EVENTO) "🎲" else "👤", fontSize = 20.sp)
                Text(
                    uiState.chat?.nombre ?: "…",
                    Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            HorizontalDivider()

            // Mensajes
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    uiState.cargando && mensajes.isEmpty() ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    mensajes.isEmpty() ->
                        Text("Aún no hay mensajes. ¡Saluda! 👋", Modifier.align(Alignment.Center))

                    else -> LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(mensajes, key = { it.mensaje.id }) { m ->
                            Burbuja(
                                m = m,
                                esMio = m.mensaje.autorId == uiState.currentUserId,
                                mostrarAutor = uiState.chat?.tipo == TipoChat.EVENTO,
                                onReintentar = { viewModel.reintentar(m.mensaje.id) },
                            )
                        }
                    }
                }
            }

            // Barra de escritura
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = uiState.input,
                    onValueChange = viewModel::onInputChange,
                    placeholder = { Text("Escribe un mensaje…") },
                    maxLines = 4,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.padding(4.dp))
                FilledIconButton(
                    onClick = {
                        viewModel.enviar()
                        scope.launch { listState.animateScrollToItem(0) }
                    },
                    enabled = uiState.input.isNotBlank() && uiState.currentUserId != null,
                ) { Text("➤") }
            }
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp))
    }
}

@Composable
private fun Burbuja(
    m: MensajeUi,
    esMio: Boolean,
    mostrarAutor: Boolean,
    onReintentar: () -> Unit,
) {
    val colores = MaterialTheme.colorScheme
    val fondo = if (esMio) colores.primary else colores.surfaceVariant
    val texto = if (esMio) colores.onPrimary else colores.onSurfaceVariant

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (esMio) Alignment.End else Alignment.Start,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .alpha(if (m.estado == EstadoEnvio.ENVIANDO) 0.6f else 1f)
                .then(if (m.estado == EstadoEnvio.ERROR) Modifier.clickable(onClick = onReintentar) else Modifier),
        ) {
            if (!esMio && mostrarAutor) {
                Text(m.mensaje.autorNombre, fontSize = 12.sp, color = colores.primary, fontWeight = FontWeight.Bold)
            }
            Text(
                m.mensaje.texto,
                color = texto,
                modifier = Modifier
                    .background(fondo, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
            Text(
                text = when (m.estado) {
                    EstadoEnvio.ENVIANDO -> "enviando…"
                    EstadoEnvio.ERROR -> "⚠ No enviado · toca para reintentar"
                    EstadoEnvio.ENVIADO -> formatearHora(m.mensaje.timestamp)
                },
                fontSize = 11.sp,
                color = if (m.estado == EstadoEnvio.ERROR) colores.error else colores.onSurfaceVariant,
            )
        }
    }
}

/**
 * Pantalla puente para Route.ChatEvento: consigue (o crea) el chat del evento
 * y llama a [onResuelto] con su id para navegar a ChatScreen.
 */
@Composable
fun ChatEventoResolver(eventoId: Int, onResuelto: (chatId: Int) -> Unit, onBack: () -> Unit) {
    val useCase: ManagerChatUseCase = koinInject()
    ResolverChat(clave = "evento-$eventoId", onResuelto = onResuelto, onBack = onBack) {
        useCase.getChatDeEvento(eventoId)
    }
}

/**
 * Pantalla puente para Route.ChatWith: consigue (o crea) el chat directo con [userId].
 */
@Composable
fun ChatDirectoResolver(userId: Int, onResuelto: (chatId: Int) -> Unit, onBack: () -> Unit) {
    val useCase: ManagerChatUseCase = koinInject()
    val currentUser: CurrentUserProvider = koinInject()
    ResolverChat(clave = "directo-$userId", onResuelto = onResuelto, onBack = onBack) {
        val yo = currentUser.currentUser()?.id
            ?: return@ResolverChat Result.failure(IllegalStateException("Sin sesión"))
        useCase.getOrCreateChatDirecto(yo, userId)
    }
}

@Composable
private fun ResolverChat(
    clave: String,
    onResuelto: (Int) -> Unit,
    onBack: () -> Unit,
    resolver: suspend () -> Result<DataChat>,
) {
    var error by remember(clave) { mutableStateOf(false) }
    var intento by remember(clave) { mutableStateOf(0) }

    LaunchedEffect(clave, intento) {
        error = false
        resolver()
            .onSuccess { onResuelto(it.id) }
            .onFailure { error = true }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!error) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No se pudo abrir el chat")
                Row {
                    TextButton(onClick = onBack) { Text("Volver") }
                    Button(onClick = { intento++ }) { Text("Reintentar") }
                }
            }
        }
    }
}
