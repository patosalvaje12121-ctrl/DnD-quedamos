package com.example.dnd_quedamos.features.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Usuario tal y como lo devuelve GET usuarios, solo con lo que necesita el chat:
 * { "id": 4, "nombre": "Sara" }
 */
@Serializable
data class ContactoDto(
    @SerialName("id") val id: Int,
    @SerialName("nombre") val nombre: String,
)
