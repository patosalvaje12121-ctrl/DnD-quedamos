package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.features.usuario.data.repository.RepositoryUsuario
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

class ManagerUsuarioUseCase (
    private val repositoryUsuario: IRepositoryUsuario = RepositoryUsuario()
) {

    suspend fun MostrarUsuario(ID: Int): Result<DataUsuario>{

        val de: DataUsuario =
            repositoryUsuario.bbddUsuario(ID) ?: return Result.failure(NoSuchElementException())

        return Result.success(de)
    }
}