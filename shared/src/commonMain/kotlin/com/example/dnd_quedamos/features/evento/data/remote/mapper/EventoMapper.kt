@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.data.remote.mapper

import com.example.dnd_quedamos.features.evento.data.remote.dto.EventoDto
import com.example.dnd_quedamos.features.evento.data.remote.dto.UbicacionDto
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.model.TipoActividad
import com.example.dnd_quedamos.features.evento.domain.model.DataUbicacion
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/*
 * Conversiones entre el modelo de red (EventoDto) y el de dominio (DataEvento).
 * Aquí se decide qué hacer con los datos "imperfectos" de la API
 * (valores nulos, formatos distintos...) para que el dominio siempre reciba datos limpios.
 */

/**
 * API -> app: lo que usa el repositorio al recibir datos.
 * Si "fecha" no es ISO-8601 válido, Instant.parse lanza una excepción y el use case la convierte
 * en Result.failure: preferimos un error visible a mostrar un evento con una fecha inventada.
 */
fun EventoDto.toDomain(): DataEvento = DataEvento(
    id = id,
    nombre = nombre,
    descripcion = descripcion.orEmpty(), // null en la API -> "" en la app
    fecha = Instant.parse(fecha),        // "2026-11-20T17:00:00Z" -> Instant
    ubicacion = ubicacion.toDomain(),
    tipoActividad = tipoActividad.toTipoActividad(),
    organizadorId = organizadorId,
    maxParticipantes = maxParticipantes,
    participantes = participantes,
)

/** app -> API: lo que usa el repositorio al enviar (crear/editar) un evento. */
fun DataEvento.toDto(): EventoDto = EventoDto(
    id = id,
    nombre = nombre,
    descripcion = descripcion.ifBlank { null }, // "" en la app -> no se envía a la API
    fecha = fecha.toString(),                   // Instant -> "2026-11-20T17:00:00Z"
    ubicacion = ubicacion.toDto(),
    tipoActividad = tipoActividad.name,
    organizadorId = organizadorId,
    maxParticipantes = maxParticipantes,
    participantes = participantes,
)

/** Atajo para listas: api.getEventos().toDomain() */
fun List<EventoDto>.toDomain(): List<DataEvento> = map { it.toDomain() }

private fun UbicacionDto.toDomain(): DataUbicacion = DataUbicacion(
    direccion = direccion.orEmpty(),
    latitud = latitud,
    longitud = longitud,
)

private fun DataUbicacion.toDto(): UbicacionDto = UbicacionDto(
    direccion = direccion.ifBlank { null },
    latitud = latitud,
    longitud = longitud,
)

/** "ROL" -> TipoActividad.ROL; null o un tipo desconocido -> OTRO (nunca rompe). */
private fun String?.toTipoActividad(): TipoActividad =
    TipoActividad.entries.firstOrNull { it.name == this } ?: TipoActividad.OTRO
