package com.example.dnd_quedamos.features.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON de un mensaje:
 * { "id": 10, "chatId": 1, "autorId": 2, "autorNombre": "Ana", "texto": "Hola", "timestamp": 1731400000000 }
 */
@Serializable
data class MensajeDto(
    @SerialName("id") val id: Int,
    @SerialName("chatId") val chatId: Int,
    @SerialName("autorId") val autorId: Int,
    @SerialName("autorNombre") val autorNombre: String? = null,
    @SerialName("texto") val texto: String,
    @SerialName("timestamp") val timestamp: Long,
)

/** Cuerpo de POST chats/{id}/mensajes (el servidor pone id, autorNombre y timestamp). */
@Serializable
data class EnviarMensajeDto(
    @SerialName("autorId") val autorId: Int,
    @SerialName("texto") val texto: String,
)
