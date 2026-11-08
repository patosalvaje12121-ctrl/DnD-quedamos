package com.example.dnd_quedamos.features.evento.data.repository

import com.example.dnd_quedamos.features.evento.data.remote.EventoApi
import com.example.dnd_quedamos.features.evento.data.remote.mapper.toDomain
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
}
