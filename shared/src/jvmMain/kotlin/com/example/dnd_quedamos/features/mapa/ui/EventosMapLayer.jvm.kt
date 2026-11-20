package com.example.dnd_quedamos.features.mapa.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.mapa.presentation.MapaViewModel
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.location.Location
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.PositionWithAccuracy
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.TimeSource

@Composable
actual fun EventosMapLayer(
    eventos: List<DataEvento>,
    usuarioActualId: Int?,
    viewModel: MapaViewModel,
    cameraState: CameraState,
    onEventoSelected: (DataEvento) -> Unit
) {
    for (evento in eventos) {
        val estaApuntado =
            usuarioActualId != null && usuarioActualId in evento.participantes
        val colorPuck = when {
            estaApuntado -> Color(0xFF9C27B0)            // Morado: Ya estás apuntado
            evento.plazasLibres > 0 -> Color(0xFF4CAF50) // Verde: Plazas disponibles
            else -> Color(0xFFFB5050)                    // Rojo: Plazas llenas
        }
        LocationPuck(
            idPrefix = "evento-${evento.id}",
            cameraState = cameraState,
            location = Location(
                PositionWithAccuracy(
                    Position(
                        longitude = evento.ubicacion.longitud,
                        latitude = evento.ubicacion.latitud
                    ),
                    accuracy = 15.0.meters
                ),
                timestamp = TimeSource.Monotonic.markNow()
            ),
            colors = LocationPuckColors(
                dotFillColorCurrentLocation = colorPuck,
                dotStrokeColor = Color.White
            ),
            onClick = {
                viewModel.seleccionarEvento(evento)
            }
        )
    }
}