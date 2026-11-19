@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.model.TipoActividad
import com.example.dnd_quedamos.features.evento.presentation.DetalleEventoViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Vista con toda la información de un evento. Se abre desde el botón "Más" de la tarjeta. */
@Composable
fun PantallaViewEvent(
    eventoId: Int,
    onBack: () -> Unit,
    onAbrirChat: (eventoId: Int) -> Unit,
    viewModel: DetalleEventoViewModel = koinViewModel { parametersOf(eventoId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val evento = uiState.evento

    Column(modifier = Modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) { Text("← Volver") }

        when {
            uiState.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            evento == null -> Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(uiState.error ?: "No se pudo cargar el evento")
                Button(onClick = viewModel::cargar, modifier = Modifier.padding(top = 8.dp)) { Text("Reintentar") }
            }

            else -> DetalleEvento(
                evento = evento,
                nombre = { id -> uiState.nombres[id] ?: "Usuario #$id" },
                onAbrirChat = { onAbrirChat(evento.id) },
            )
        }
    }
}

@Composable
private fun DetalleEvento(
    evento: DataEvento,
    nombre: (Int) -> String,
    onAbrirChat: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(evento.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        AssistChip(onClick = {}, label = { Text(evento.tipoActividad.etiqueta()) })

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Dato("📅", "Fecha", formatearFecha(evento.fecha))
                Dato("📍", "Ubicación", evento.ubicacion.direccion)
                Dato("👑", "Organizador", nombre(evento.organizadorId))
            }
        }

        Seccion("Descripción") {
            Text(evento.descripcion.ifBlank { "Sin descripción" })
        }

        Seccion("Plazas") {
            val ocupadas = evento.participantes.size
            Text(
                if (evento.estaCompleto) "Completo ($ocupadas/${evento.maxParticipantes})"
                else "$ocupadas/${evento.maxParticipantes} · ${evento.plazasLibres} libres"
            )
            LinearProgressIndicator(
                progress = { if (evento.maxParticipantes > 0) ocupadas.toFloat() / evento.maxParticipantes else 0f },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
        }

        Seccion("Participantes") {
            if (evento.participantes.isEmpty()) {
                Text("Todavía no hay nadie apuntado")
            } else {
                evento.participantes.forEach { id ->
                    Text("• ${nombre(id)}" + if (id == evento.organizadorId) " (organizador)" else "")
                }
            }
        }

        Button(onClick = onAbrirChat, modifier = Modifier.fillMaxWidth()) { Text("💬 Chat del evento") }
    }
}

@Composable
private fun Dato(emoji: String, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, modifier = Modifier.padding(end = 8.dp))
        Column {
            Text(titulo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor)
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column {
        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        contenido()
    }
}

private fun TipoActividad.etiqueta(): String = when (this) {
    TipoActividad.ROL -> "🎲 Rol"
    TipoActividad.JUEGOS_DE_MESA -> "♟️ Juegos de mesa"
    TipoActividad.CARTAS -> "🃏 Cartas"
    TipoActividad.WARGAME -> "⚔️ Wargame"
    TipoActividad.CONVENCION -> "🏰 Convención"
    TipoActividad.OTRO -> "✨ Otro"
}

/** Instant -> "dd/MM/yyyy HH:mm" en la zona horaria del dispositivo. */
private fun formatearFecha(fecha: Instant): String {
    val t = fecha.toLocalDateTime(TimeZone.currentSystemDefault())
    fun Int.dos() = toString().padStart(2, '0')
    return "${t.day.dos()}/${t.month.ordinal.plus(1).dos()}/${t.year} ${t.hour.dos()}:${t.minute.dos()}"
}
