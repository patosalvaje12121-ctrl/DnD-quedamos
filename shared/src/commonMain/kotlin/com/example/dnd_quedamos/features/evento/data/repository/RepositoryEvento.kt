package com.example.dnd_quedamos.features.evento.data.repository

import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento

class RepositoryEvento(
    //private val httpClient: HttpClient
): IRepositoryEvento {
    override suspend fun  bbddEvento(): List<DataEvento>{
        return listOf(
            DataEvento(1, nombre = "PrimerEvento", descripcion = "estoesunaprueba1"),
            DataEvento(2, nombre = "SegundoEvento", descripcion = "estoesunaprueba2")
        )
    }
}