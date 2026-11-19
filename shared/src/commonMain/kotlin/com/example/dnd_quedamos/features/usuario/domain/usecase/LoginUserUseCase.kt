package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.MutableCurrentUserProvider
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

//TODO: ver que hacer con la contraseña
class LoginUserUseCase(
    private val repository: IRepositoryUsuario,
    private val session: MutableCurrentUserProvider
) {
    suspend fun iniciarSesionPrueba(id: Int): DataUsuario {
        require(id > 0) { "Introduce un ID válido" }
        val usuario = repository.obtenerUsuario(id)
            ?: throw NoSuchElementException("No existe el usuario con ID $id")
        session.iniciarSesion(usuario)
        return usuario
    }
}
