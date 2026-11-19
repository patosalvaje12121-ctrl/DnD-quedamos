package com.example.dnd_quedamos.core.network

import okio.ByteString.Companion.encodeUtf8

internal object Sha256Demo {
    fun hash(texto: String): String = texto.encodeUtf8().sha256().hex()
}
