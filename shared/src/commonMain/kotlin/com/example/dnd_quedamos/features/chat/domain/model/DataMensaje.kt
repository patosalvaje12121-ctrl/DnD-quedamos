package com.example.dnd_quedamos.features.chat.domain.model

data class DataMensaje(
    val id: Int,
    val chatId: Int,
    val autorId: Int,
    val autorNombre: String,
    val texto: String,
    val timestamp: Long, // milisegundos desde epoch
)
