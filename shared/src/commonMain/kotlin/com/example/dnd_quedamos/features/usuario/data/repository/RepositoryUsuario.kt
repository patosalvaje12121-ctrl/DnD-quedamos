package com.example.dnd_quedamos.features.usuario.data.repository

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

class RepositoryUsuario : IRepositoryUsuario {

    override suspend fun bbddUsuario(ID: Int): DataUsuario {
        TODO("Not yet implemented")
    }
}