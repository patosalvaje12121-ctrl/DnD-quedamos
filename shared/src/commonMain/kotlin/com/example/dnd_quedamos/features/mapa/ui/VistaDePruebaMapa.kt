package com.example.dnd_quedamos.features.mapa.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.*
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import kotlin.let
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun VistaDePruebaMapa(modifier: Modifier = Modifier) {

    // Primero revisamos permisos de ubicación
    val permissionState = rememberLocationPermissionState()

    // Definimos variables de ubicacion del susuario, si no tiene permisos, se asigna null
    val locationProvider = if (permissionState.isGranted) {
        rememberDefaultLocationProvider()
    } else {
        rememberNullLocationProvider()
    }
    val orientationProvider = rememberDefaultOrientationProvider()

    val locationState = rememberUserLocationState(
        locationProvider = locationProvider,
        orientationProvider = orientationProvider
    )
    val cameraState = rememberCameraState()
    val coroutineScope = rememberCoroutineScope()

    var tieneCentradoInicial by rememberSaveable { mutableStateOf(false) }

    val userPosition = locationState.location?.position?.value // La posicion del usuario

    if (userPosition != null && !tieneCentradoInicial) {
        // Se ejecuta para centrar la camara en el usuario al abrir el mapa la unica vez y dejar la camara
        // libre el resto de tiempo
        cameraState.position = cameraState.position.copy(
            zoom = 10.0,
            target = userPosition
        )
        tieneCentradoInicial = true
    }

    Box(modifier = modifier.fillMaxSize()) {

        // Se utiliza para establecer interfaz grafica del mapa
        MaplibreMap(
            modifier = modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            cameraState = cameraState,
            zoomRange = 1.0f..22.0f
        ) {
            if (permissionState.isGranted) {
                // Punto azul del usuario
                LocationPuck(
                    idPrefix = "user-location",
                    cameraState = cameraState,
                    location = locationState.location
                )
            }
        }

        // Centrar vista en el usuario
        if (permissionState.isGranted) {
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

        if (!permissionState.isGranted) {
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
    }
}