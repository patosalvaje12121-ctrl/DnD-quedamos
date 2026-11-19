package com.example.dnd_quedamos.features.mapa.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.evento.data.remote.dto.EventoDto
import com.example.dnd_quedamos.features.mapa.presentation.MapaViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.files.SystemFileSystem
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.*
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.meters
import kotlin.let
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

@Composable
fun VistaMainMapa(
    modifier: Modifier = Modifier,
    viewModel: MapaViewModel = koinViewModel()
) {
    // Estado de la pantalla desde el ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarConfirmacionDesapuntar by remember { mutableStateOf(false) }

    // Primero revisamos permisos de ubicación
    val permissionState = rememberLocationPermissionState()

    // Definimos variables de ubicacion del susuario, si no tiene permisos, se asigna null
    val locationProvider = if (permissionState.isGranted && permissionState.isRequired) {
        rememberDefaultLocationProvider()
    } else {
        rememberNullLocationProvider()
    }

    val orientationProvider = if (permissionState.getPlatform != "android") {
        rememberNullOrientationProvider()
    } else {
        rememberDefaultOrientationProvider()
    }

    val locationState = rememberUserLocationState(
        locationProvider = locationProvider,
        orientationProvider = orientationProvider
    )

    val cameraState = rememberCameraState()
    val coroutineScope = rememberCoroutineScope()

    var tieneCentradoInicial by rememberSaveable { mutableStateOf(false) }
    val userPosition = locationState.location?.position?.value

    LaunchedEffect(userPosition) {
        if (userPosition != null && !tieneCentradoInicial) {
            // Se ejecuta para centrar la camara en el usuario al abrir el mapa la unica vez y dejar la camara
            // libre el resto de tiempo
            cameraState.position = cameraState.position.copy(
                zoom = 10.0,
                target = userPosition
            )
            tieneCentradoInicial = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // Se utiliza para establecer interfaz grafica del mapa
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            cameraState = cameraState,
            zoomRange = 1.0f..22.0f
        ) {
            if (permissionState.isGranted && permissionState.isRequired) {
                // Punto azul del usuario
                LocationPuck(
                    idPrefix = "user-location",
                    cameraState = cameraState,
                    location = locationState.location
                )
            }
            for (evento in uiState.eventos) {
                val estaApuntado = uiState.usuarioActualId != null && uiState.usuarioActualId in evento.participantes
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

        // Card con detalle del evento si el usuario hace clic en un evento
        uiState.eventoSeleccionado?.let { evento ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = evento.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { viewModel.deseleccionarEvento() }) {
                            Text("✕")
                        }
                    }
                    Text(
                        text = evento.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = "${evento.ubicacion.direccion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Plazas libres: ${evento.plazasLibres} / ${evento.maxParticipantes}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Boton Apuntarse/Desapuntarse
                    val estoyApuntado = uiState.usuarioActualId != null && uiState.usuarioActualId in evento.participantes

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (estoyApuntado) {
                            Button(
                                onClick = { mostrarConfirmacionDesapuntar = true },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Desapuntarse")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.unirseAEvento(evento.id) },
                                enabled = evento.plazasLibres > 0
                            ) {
                                Text(if (evento.plazasLibres > 0) "Apuntarse" else "Completo")
                            }
                        }
                    }

                    // Confirmación Desapuntado
                    if (mostrarConfirmacionDesapuntar) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { mostrarConfirmacionDesapuntar = false },
                            title = { Text("Confirmar") },
                            text = { Text("¿Seguro que quieres desapuntarte de este evento?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        mostrarConfirmacionDesapuntar = false
                                        viewModel.abandonarEvento(evento.id)
                                    }
                                ) {
                                    Text("Sí, desapuntarme")
                                }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(
                                    onClick = { mostrarConfirmacionDesapuntar = false }
                                ) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }

                }
            }
        }

        // Centrar vista en el usuario
        if (permissionState.isGranted && permissionState.isRequired) {
            FloatingActionButton(
                onClick = {
                    userPosition?.let { pos ->
                        coroutineScope.launch {
                            cameraState.animateTo(
                                CameraPosition(
                                    target = pos,
                                    zoom = 10.0
                                ),
                                duration = 500.milliseconds
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Text("\uD83D\uDD35", fontSize = 22.sp)
            }
        }

        if (!permissionState.isGranted && permissionState.isRequired && permissionState.getPlatform != "desktop") {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "La ubicación está desactivada. Actívala para centrar el mapa en tu posición y ver eventos cercanos.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { permissionState.requestPermission() }) {
                        Text("Activar ubicación")
                    }
                }
            }
        }

        // Mensaje de feedback por 2 segundos
        uiState.mensajeFeedback?.let { mensaje ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Text(
                    text = mensaje,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}