package com.example.dnd_quedamos.features.mapa.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.mapa.presentation.MapaViewModel
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

@Composable
actual fun EventosMapLayer(
    eventos: List<DataEvento>,
    usuarioActualId: Int?,
    viewModel: MapaViewModel,
    cameraState: CameraState,
    onEventoSelected: (DataEvento) -> Unit
) {
    for (evento in eventos) {
        val estaApuntado = usuarioActualId != null && usuarioActualId in evento.participantes
        val colorEvento = when {
            estaApuntado -> Color(0xFF9C27B0)            // Morado
            evento.plazasLibres > 0 -> Color(0xFF4CAF50) // Verde
            else -> Color(0xFFFB5050)                    // Rojo
        }

        val source = rememberGeoJsonSource(
            data = GeoJsonData.Features(
                Point(
                    Position(
                        longitude = evento.ubicacion.longitud,
                        latitude = evento.ubicacion.latitud
                    )
                )
            )
        )

        CircleLayer(
            id = "layer-evento-${evento.id}",
            source = source,
            color = const(colorEvento),
            radius = const(10.dp),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp),
            onClick = {
                onEventoSelected(evento)
                ClickResult.Consume
            }
        )
    }
}