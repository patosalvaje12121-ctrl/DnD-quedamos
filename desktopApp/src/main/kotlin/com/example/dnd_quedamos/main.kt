package com.example.dnd_quedamos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DnDquedamos",
    ) {
        App()
    }
}