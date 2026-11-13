package com.example.dnd_quedamos.features.evento.data.repository

import com.example.dnd_quedamos.features.evento.data.remote.EventoApi
import com.example.dnd_quedamos.features.evento.data.remote.mapper.toDomain
import com.example.dnd_quedamos.features.evento.data.remote.mapper.toDto
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento

/**
 * Única clase que conoce el origen de los datos de eventos.
 *
 * Ahora: solo red (EventoApi).
 * Más adelante: también la BD local (Room / SQLDelight) — se añadirá como otro parámetro
 * del constructor y el resto de la app no tendrá que cambiar.
 *
 * Recibe DTOs de la API y devuelve siempre modelos de dominio (DataEvento).
 */
class RepositoryEvento(
    private val api: EventoApi,
) : IRepositoryEvento {

    override suspend fun bbddEvento(): List<DataEvento> =
        api.getEventos().toDomain()

    override suspend fun crearEvento(evento: DataEvento): DataEvento =
        api.crearEvento(evento.toDto()).toDomain()

    override suspend fun editarEvento(evento: DataEvento): DataEvento =
        api.actualizarEvento(evento.toDto()).toDomain()

    // Sin campo "estado" en el modelo, cancelar = borrar en el servidor
    override suspend fun cancelarEvento(eventoId: Int) =
        api.borrarEvento(eventoId)

    override suspend fun participantesEvento(eventoId: Int): List<Int> =
        api.getParticipantes(eventoId)

    override suspend fun unirseEvento(eventoId: Int, usuarioId: Int) =
        api.unirseEvento(eventoId, usuarioId)

    override suspend fun abandonarEvento(eventoId: Int, usuarioId: Int) =
        api.abandonarEvento(eventoId, usuarioId)
}
