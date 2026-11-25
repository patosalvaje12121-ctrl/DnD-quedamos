package com.example.dnd_quedamos.features.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.core.ui.componentes.Separator
import com.example.dnd_quedamos.features.chat.domain.model.TipoChat
import com.example.dnd_quedamos.features.chat.presentation.ChatViewModel
import com.example.dnd_quedamos.features.chat.presentation.EstadoEnvio
import com.example.dnd_quedamos.features.chat.presentation.MensajeUi
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatView(
    chatId: Int,
    showBack : Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }
    val snackbar = remember { SnackbarHostState() }

    val mensajes = uiState.mensajes.asReversed()

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
        viewModel.reintentar(chatId)
    }


    // Scroll to bottom if a new message entered & was already on the bottom
    LaunchedEffect(mensajes.firstOrNull()?.mensaje?.id) {
        if (listState.firstVisibleItemIndex <= 1) listState.animateScrollToItem(0)
    }

    // Error Handler
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbar.showSnackbar(it)
            viewModel.errorMostrado()
        }
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().imePadding()) {
            // Cabecera
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(showBack) TextButton(onClick = onBack) { Text("←", fontSize = 20.sp) }
                Column ( Modifier.padding(6.dp, 6.dp)) {
                    Row (Modifier.padding(6.dp)) {
                        Text(if (uiState.chat?.tipo == TipoChat.EVENTO) "🎲" else "👤", fontSize = 20.sp)
                        Text(
                            uiState.chat?.nombre ?: "…",
                            Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Body
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
                            ThinkingBubble(
                                m = m,
                                imOwner = m.mensaje.autorId == uiState.currentUserId,
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
                    placeholder = { Text("Escribe un mensaje...") },
                    maxLines = 4,
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
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
private fun ThinkingBubble(
    m: MensajeUi,
    imOwner: Boolean,
    mostrarAutor: Boolean,
    onReintentar: () -> Unit,
) {
    val colores = MaterialTheme.colorScheme
    val fondo = if (imOwner) colores.primary else colores.surfaceVariant
    val texto = if (imOwner) colores.onPrimary else colores.onSurfaceVariant

    // Arte ASCII (muchas líneas): ocupa todo el ancho y la letra se ajusta para que quepa entero
    val lineas = m.mensaje.texto.lines()
    val esAscii = lineas.size > 20

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (imOwner) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (imOwner) Alignment.End else Alignment.Start,
            modifier = Modifier
                .then(if (esAscii) Modifier.fillMaxWidth() else Modifier.widthIn(max = 300.dp))
                .alpha(if (m.estado == EstadoEnvio.ENVIANDO) 0.6f else 1f)
                .then(if (m.estado == EstadoEnvio.ERROR) Modifier.clickable(onClick = onReintentar) else Modifier),
        ) {
            if (!imOwner && mostrarAutor) {
                Text(m.mensaje.autorNombre, fontSize = 12.sp, color = colores.primary, fontWeight = FontWeight.Bold)
            }
            val burbuja = Modifier
                .background(fondo, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
            if (esAscii) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val columnas = lineas.maxOf { it.length }.coerceAtLeast(1)
                    // Una letra monoespaciada mide ~0.6 de ancho respecto a su tamaño
                    val tamano = with(LocalDensity.current) {
                        ((maxWidth - 24.dp) / (columnas * 0.62f)).toSp()
                    }
                    Text(
                        m.mensaje.texto,
                        color = texto,
                        fontFamily = FontFamily.Monospace,
                        fontSize = tamano,
                        lineHeight = tamano,
                        softWrap = false,
                        modifier = burbuja,
                    )
                }
            } else {
                Text(m.mensaje.texto, color = texto, modifier = burbuja)
            }
            Text(
                text = when (m.estado) {
                    EstadoEnvio.ENVIANDO -> "Enviando…"
                    EstadoEnvio.ERROR -> "⚠ No enviado · Toca para reintentar"
                    EstadoEnvio.ENVIADO -> formatearHora(m.mensaje.timestamp)
                },
                fontSize = 11.sp,
                color = if (m.estado == EstadoEnvio.ERROR) colores.error else colores.onSurfaceVariant,
            )
        }
    }
}