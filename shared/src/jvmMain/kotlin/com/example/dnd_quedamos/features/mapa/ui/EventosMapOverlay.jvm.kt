package com.example.dnd_quedamos.features.mapa.ui

import androidx.compose.runtime.Composable
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.mapa.presentation.MapaViewModel
import org.maplibre.compose.camera.CameraState

@Composable
actual fun EventosMapOverlay(
    eventos: List<DataEvento>,
    usuarioActualId: Int?,
    viewModel: MapaViewModel,
    cameraState: CameraState,
    onEventoSelected: (DataEvento) -> Unit
) {
}