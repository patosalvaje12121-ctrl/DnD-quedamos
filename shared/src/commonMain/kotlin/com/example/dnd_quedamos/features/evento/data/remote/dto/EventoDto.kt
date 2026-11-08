package com.example.dnd_quedamos.features.evento.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO (Data Transfer Object): representa EXACTAMENTE el JSON que envía/recibe la API.
 *
 * - Solo se usa dentro de data/remote (Api y mappers). Nunca llega a la UI ni al dominio.
 * - Si la API cambia un nombre de campo, solo se toca este archivo (y su mapper).
 *
 * JSON esperado:
 * {
 *   "id": 1,
 *   "nombre": "Partida en la biblioteca",
 *   "descripcion": "Sesión 0"
 * }
 */
@Serializable
data class EventoDto(
    // Obligatorio: si el JSON no trae "id", la deserialización falla (un evento sin id no tiene sentido)
    @SerialName("id")
    val id: Int,

    @SerialName("nombre")
    val nombre: String,

    // Opcional: si el JSON no lo trae o viene null, vale null en vez de romper
    @SerialName("descripcion")
    val descripcion: String? = null,
)
