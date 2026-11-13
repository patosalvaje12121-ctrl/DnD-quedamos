@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class DataEvento (
    val id: Int,
    val nombre : String,
    val descripcion: String,
    val fecha: Instant,                 // momento exacto de inicio (la UI lo pasa a la hora local)
    val ubicacion: DataUbicacion,
    val tipoActividad: TipoActividad,
    val organizadorId: Int,
    val maxParticipantes: Int,
    val participantes: List<Int> = emptyList(), // ids de usuario; lo rellena el servidor
) {
    val plazasLibres: Int get() = (maxParticipantes - participantes.size).coerceAtLeast(0)
    val estaCompleto: Boolean get() = plazasLibres == 0
}
