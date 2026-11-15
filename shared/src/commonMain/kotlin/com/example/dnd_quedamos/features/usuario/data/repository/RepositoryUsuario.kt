package com.example.dnd_quedamos.features.usuario.data.repository

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

class RepositoryUsuario : IRepositoryUsuario {

    // Usuarios simulados mientras no exista BBDD
    private val usuarios = hashMapOf(
        1 to DataUsuario(
            id = 1,
            name = "Admin",
            imageID = 0,
            descBio = "Admin User",
            assistedEvents = emptyList()
        ),

        2 to DataUsuario(
            id = 2,
            name = "Default",
            imageID = 0,
            descBio = "Default User",
            assistedEvents = listOf(2, 3)
        ),

        3 to DataUsuario(
            id = 3,
            name = "Default2",
            imageID = 0,
            descBio = "Default User 2",
            assistedEvents = listOf(1, 2, 3)
        )
    )

    override suspend fun bbddUsuario(ID: Int): DataUsuario? {
        return usuarios[ID]
    }

    override suspend fun obtenerUsuario(id: Int): DataUsuario? {
        return bbddUsuario(id)
    }
}