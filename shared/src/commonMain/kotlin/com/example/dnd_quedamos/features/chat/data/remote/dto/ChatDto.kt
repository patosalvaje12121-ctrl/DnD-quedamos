package com.example.dnd_quedamos.features.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON de un chat:
 * { "id": 1, "nombre": "Ana", "tipo": "DIRECTO", "participantes": [1, 2],
 *   "eventoId": null, "ultimoMensaje": { ...MensajeDto } }
 */
@Serializable
data class ChatDto(
    @SerialName("id") val id: Int,
    @SerialName("nombre") val nombre: String,
    @SerialName("tipo") val tipo: String,              // "DIRECTO" | "EVENTO"
    @SerialName("participantes") val participantes: List<Int> = emptyList(),
    @SerialName("eventoId") val eventoId: Int? = null,
    @SerialName("ultimoMensaje") val ultimoMensaje: MensajeDto? = null,
)

/** Cuerpo de POST chats/directo */
@Serializable
data class CrearChatDirectoDto(
    @SerialName("userA") val userA: Int,
    @SerialName("userB") val userB: Int,
)
