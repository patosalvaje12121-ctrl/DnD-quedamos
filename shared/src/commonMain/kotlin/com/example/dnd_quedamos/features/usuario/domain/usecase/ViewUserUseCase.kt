package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

class ViewUserUseCase(
    private val repository: IRepositoryUsuario,
    private val session: CurrentUserProvider
) {
    suspend fun obtenerPerfil(id: Int): DataUsuario {
        val idReal = if (id == -1) {
            session.currentUser()?.id ?: error("No hay sesión iniciada")
        } else id
        require(idReal > 0) { "ID de usuario no válido" }
        return repository.obtenerUsuario(idReal)
            ?: throw NoSuchElementException("No existe el usuario con ID $idReal")
    }
}
