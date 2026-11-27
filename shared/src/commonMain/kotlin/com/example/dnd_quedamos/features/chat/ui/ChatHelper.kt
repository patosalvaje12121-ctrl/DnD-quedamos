package com.example.dnd_quedamos.features.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.usecase.ManagerChatUseCase
import com.example.dnd_quedamos.features.chat.presentation.EstadoEnvio
import com.example.dnd_quedamos.features.chat.presentation.MensajeUi
import org.koin.compose.koinInject

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
