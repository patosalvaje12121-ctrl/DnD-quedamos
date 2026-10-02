package com.example.dnd_quedamos.features.evento.data.repository

import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento

class RepositoryEvento(
    //private val httpClient: HttpClient
): IRepositoryEvento {
    override suspend fun  bbddEvento(): List<DataEvento>{

        val eventosDeEjemplo = (1..30).map { i ->
            DataEvento(id = i, nombre = "Objeto $i", descripcion = "Descripción de ejemplo $i")
        }

        return eventosDeEjemplo;
    }
}