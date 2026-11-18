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
 *   "descripcion": "Sesión 0",
 *   "fecha": "2026-11-20T17:00:00Z",
 *   "ubicacion": { "direccion": "Biblioteca María Zambrano", "latitud": 40.4508, "longitud": -3.7277 },
 *   "tipoActividad": "ROL",
 *   "organizadorId": 3,
 *   "maxParticipantes": 6,
 *   "participantes": [1, 2, 3]
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

    // Fecha en formato ISO-8601 en UTC ("2026-11-20T17:00:00Z"). JSON no tiene tipo fecha:
    // viaja como texto y el mapper la convierte en Instant.
    @SerialName("fecha")
    val fecha: String,

    @SerialName("ubicacion")
    val ubicacion: UbicacionDto,

    // Texto y no enum: si el servidor añade un tipo nuevo, el mapper lo convierte en OTRO
    // en vez de que falle toda la lista.
    @SerialName("tipoActividad")
    val tipoActividad: String? = null,

    @SerialName("organizadorId")
    val organizadorId: Int,

    @SerialName("maxParticipantes")
    val maxParticipantes: Int,

    // Lo rellena el servidor al devolver eventos; al crear/editar lo ignora
    @SerialName("participantes")
    val participantes: List<Int> = emptyList(),
)

/** Objeto anidado "ubicacion" del JSON de un evento. */
@Serializable
data class UbicacionDto(
    @SerialName("direccion")
    val direccion: String? = null,

    @SerialName("latitud")
    val latitud: Double,

    @SerialName("longitud")
    val longitud: Double,
)
