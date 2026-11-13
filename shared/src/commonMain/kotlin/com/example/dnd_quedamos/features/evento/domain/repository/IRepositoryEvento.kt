package com.example.dnd_quedamos.features.evento.domain.repository

import com.example.dnd_quedamos.features.evento.domain.model.DataEvento

interface IRepositoryEvento {
    suspend fun bbddEvento(): List<DataEvento>

    /** Crea el evento y devuelve el guardado (con el id que le asigne el servidor). */
    suspend fun crearEvento(evento: DataEvento): DataEvento

    /** Sustituye los datos de un evento existente y devuelve cómo ha quedado. */
    suspend fun editarEvento(evento: DataEvento): DataEvento

    suspend fun cancelarEvento(eventoId: Int)

    /** Ids de los usuarios apuntados al evento. */
    suspend fun participantesEvento(eventoId: Int): List<Int>

    suspend fun unirseEvento(eventoId: Int, usuarioId: Int)

    suspend fun abandonarEvento(eventoId: Int, usuarioId: Int)
}
