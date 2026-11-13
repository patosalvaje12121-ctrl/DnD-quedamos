package com.example.dnd_quedamos.features.evento.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cuerpo de POST /eventos/{id}/participantes (apuntarse a un evento).
 *
 * JSON enviado:
 * { "usuarioId": 1 }
 */
@Serializable
data class ParticipanteDto(
    @SerialName("usuarioId")
    val usuarioId: Int,
)
