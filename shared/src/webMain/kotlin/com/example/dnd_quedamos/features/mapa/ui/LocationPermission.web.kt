package com.example.dnd_quedamos.features.mapa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
actual fun rememberLocationPermissionState(): LocationPermissionState {
    // En Web: empieza en false para mostrar el cartel hasta que se active
    var haCargadoUbicacion by remember { mutableStateOf(false) }

    // Si el usuario pulsa directamente el botón de la mira del mapa web, también lo detectamos
    LaunchedEffect(Unit) {
        delay(1000)
        js("""
            var btn = document.querySelector('.maplibregl-ctrl-geolocate');
            if (btn) {
                btn.addEventListener('click', function() {
                    window.dispatchEvent(new Event('ubicacion_web_cargada'));
                });
            }
        """)
    }

    DisposableEffect(Unit) {
        val listener: (dynamic) -> Unit = {
            haCargadoUbicacion = true
        }
        js("window.addEventListener('ubicacion_web_cargada', listener);")
        onDispose {
            js("window.removeEventListener('ubicacion_web_cargada', listener);")
        }
    }

    return remember(haCargadoUbicacion) {
        object : LocationPermissionState {
            override val isGranted: Boolean get() = haCargadoUbicacion
            override val isRequired: Boolean = false // Mantiene a salvo a Web de los crashes

            override fun requestPermission() {
                // Al pulsar "Activar ubicación" en el cartel, pulsamos la mira nativa de MapLibre en Web:
                js("""
                    var btn = document.querySelector('.maplibregl-ctrl-geolocate');
                    if (btn) {
                        btn.click();
                    } else if (navigator.geolocation) {
                        navigator.geolocation.getCurrentPosition(function() {});
                    }
                """)
                haCargadoUbicacion = true
            }

            override fun openAppSettings() {}
            override val getPlatform: String = "web"
        }
    }
}