package com.example.dnd_quedamos.features.evento.data.remote.mapper

import com.example.dnd_quedamos.features.evento.data.remote.dto.EventoDto
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento

/*
 * Conversiones entre el modelo de red (EventoDto) y el de dominio (DataEvento).
 * Aquí se decide qué hacer con los datos "imperfectos" de la API
 * (valores nulos, formatos distintos...) para que el dominio siempre reciba datos limpios.
 */

/** API -> app: lo que usa el repositorio al recibir datos. */
fun EventoDto.toDomain(): DataEvento = DataEvento(
    id = id,
    nombre = nombre,
    descripcion = descripcion.orEmpty(), // null en la API -> "" en la app
)

/** app -> API: lo que usará el repositorio al enviar (crear/editar) un evento. */
fun DataEvento.toDto(): EventoDto = EventoDto(
    id = id,
    nombre = nombre,
    descripcion = descripcion.ifBlank { null }, // "" en la app -> no se envía a la API
)

/** Atajo para listas: api.getEventos().toDomain() */
fun List<EventoDto>.toDomain(): List<DataEvento> = map { it.toDomain() }
