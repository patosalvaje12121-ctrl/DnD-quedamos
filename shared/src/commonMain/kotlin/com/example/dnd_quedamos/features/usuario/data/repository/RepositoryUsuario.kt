package com.example.dnd_quedamos.features.usuario.data.repository

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

class RepositoryUsuario : IRepositoryUsuario {

    val map: HashMap<Int, DataUsuario> = hashMapOf(

        1 to DataUsuario(1, "Admin", 0, "Admin User", listOf()),
        2 to DataUsuario(2, "Default", 0, "Default User", listOf(2, 3)),
        3 to DataUsuario(3, "Default2", 0, "Default User 2", listOf(1, 2, 3)),

    );

    override suspend fun bbddUsuario(ID: Int): DataUsuario? {

        return map[ID];

    }
}