package com.example.dnd_quedamos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.dnd_quedamos.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "DnDquedamos",
        ) {
            App()
        }
    }
}
