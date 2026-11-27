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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.core.ui.componentes.AccionBoton
import com.example.dnd_quedamos.core.ui.componentes.DndBoton
import com.example.dnd_quedamos.core.ui.componentes.DndPanel
import com.example.dnd_quedamos.core.ui.componentes.SeparatorOr
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import com.example.dnd_quedamos.features.evento.presentation.DetalleEventoViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

/** Detalle de un evento. Se abre desde la lista de gestión o desde un chat. */
@Composable
fun PantallaViewEvent(
    eventoId: Int,
    onBack: () -> Unit,
    onAbrirChat: () -> Unit,
    onEditar: () -> Unit,
    showBack: Boolean = true,
    viewModel: DetalleEventoViewModel = koinViewModel { parametersOf(eventoId) },
) {
    LaunchedEffect(Unit) { viewModel.cargar() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val evento = uiState.evento
    var confirmarBorrado by remember { mutableStateOf(false) }

    fun nombre(id: Int) = uiState.nombres[id] ?: "Usuario #$id"

    DndPanel (skin = DndSkins.panel) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Top bar ──────────────────────────────────────────────
            if(showBack)
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DndBoton(
                        onClick = onBack,
                        skin = DndSkins.Botones.principal_fino,
                    ) {
                        Text("←")
                    }
                }
            }

            if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (evento == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Cargando...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // ── Header ───────────────────────────────────────────
                DndPanel(
                    skin = DndSkins.filaChat,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = evento.nombre,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${evento.tipoActividad} · ${formatearFecha(evento.fecha)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (evento.ubicacion.direccion.isNotBlank()) {
                            Text(
                                text = "📍 ${evento.ubicacion.direccion}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                // ── Details ──────────────────────────────────────────
                DndPanel(
                    skin = DndSkins.filaChat,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        InfoRow("Organizador", nombre(evento.organizadorId))
                        InfoRow(
                            "Plazas",
                            "${evento.participantes.size}/${evento.maxParticipantes}",
                        )
                        InfoRow(
                            "Participantes",
                            if (evento.participantes.isEmpty()) "—"
                            else evento.participantes.joinToString { nombre(it) },
                        )
                    }
                }

                // ── Description ──────────────────────────────────────
                if (evento.descripcion.isNotBlank()) {
                    DndPanel(
                        skin = DndSkins.filaChat,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = evento.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                // ── Primary actions ──────────────────────────────────
                val miId = uiState.miId
                val apuntado = miId in evento.participantes
                val puedeApuntarse = miId != null && miId != evento.organizadorId

                if (puedeApuntarse && apuntado) {
                    AccionBoton(
                        texto = "Desapuntarme",
                        onClick = viewModel::desapuntarse,
                        modifier = Modifier.fillMaxWidth(),
                        skin = DndSkins.Botones.secundario,
                    )
                }
                if (puedeApuntarse && !apuntado && !evento.estaCompleto) {
                    AccionBoton(
                        texto = "Apuntarme",
                        onClick = viewModel::apuntarse,
                        modifier = Modifier.fillMaxWidth(),
                        skin = DndSkins.Botones.principal,
                    )
                }

                AccionBoton(
                    texto = "Chat del evento",
                    onClick = onAbrirChat,
                    modifier = Modifier.fillMaxWidth(),
                    skin = DndSkins.Botones.secundario,
                )

                // ── Manage actions ───────────────────────────────────
                if (uiState.puedeGestionar) {
                    SeparatorOr()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DndBoton(
                            onClick = onEditar,
                            modifier = Modifier.weight(1f),
                            skin = DndSkins.Botones.texto,
                        ) {
                            Text("Editar")
                        }
                        DndBoton(
                            onClick = { confirmarBorrado = true },
                            modifier = Modifier.weight(1f),
                            skin = DndSkins.Botones.texto,
                        ) {
                            Text("Borrar")
                        }
                    }
                }
            }
        }

        if (confirmarBorrado) {
            ConfirmarBorrado(
                onConfirmar = {
                    confirmarBorrado = false
                    viewModel.borrar(onBack)
                },
                onCancelar = { confirmarBorrado = false },
            )
        }
    }

}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(2f)
                .padding(start = 12.dp),
        )
    }
}