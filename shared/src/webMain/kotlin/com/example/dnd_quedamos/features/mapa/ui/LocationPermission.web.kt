package com.example.dnd_quedamos.features.mapa.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberLocationPermissionState(): LocationPermissionState {
    return remember {
        object : LocationPermissionState {
            override val isGranted: Boolean = true
            override fun requestPermission() {}
            override fun openAppSettings() {
                TODO("Not yet implemented")
            }
        }
    }
}