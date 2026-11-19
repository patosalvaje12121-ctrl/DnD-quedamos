package com.example.dnd_quedamos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.dnd_quedamos.di.initKoin
import org.maplibre.compose.desktop.ProvideMapHost
import org.maplibre.compose.desktop.rememberAwtComposeMapHost

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "DnDquedamos",
        ) {
            val mapHost = rememberAwtComposeMapHost(window)
            ProvideMapHost(host = mapHost) {
                App()
            }
        }
    }
}