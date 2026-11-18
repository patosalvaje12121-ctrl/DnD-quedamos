package com.example.dnd_quedamos.features.mapa.ui

import androidx.compose.runtime.Composable

interface LocationPermissionState {
    val isGranted: Boolean
    fun requestPermission()
    fun openAppSettings()
}

/**
 * Contrato multiplataforma para consultar y solicitar permisos de ubicación.
 */
@Composable
expect fun rememberLocationPermissionState(): LocationPermissionState