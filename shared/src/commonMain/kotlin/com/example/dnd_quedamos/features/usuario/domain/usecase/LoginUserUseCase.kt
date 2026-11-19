package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.MutableCurrentUserProvider
import com.example.dnd_quedamos.features.usuario.data.remote.AuthApi
import com.example.dnd_quedamos.features.usuario.data.remote.mapper.toDomain
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

class LoginUserUseCase(
    private val authApi: AuthApi,
    private val session: MutableCurrentUserProvider
) {
    suspend fun iniciarSesion(nombre: String, password: String): DataUsuario {
        require(nombre.trim().isNotEmpty()) { "Introduce el nombre de usuario" }
        require(password.isNotBlank()) { "Introduce la contraseña" }
        val usuario = authApi.iniciarSesion(nombre.trim(), password).toDomain()
        session.iniciarSesion(usuario)
        return usuario
    }
}
