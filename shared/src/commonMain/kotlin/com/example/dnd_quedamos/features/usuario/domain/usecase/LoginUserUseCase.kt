package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.MutableCurrentUserProvider
import com.example.dnd_quedamos.features.usuario.data.remote.AuthApi
import com.example.dnd_quedamos.features.usuario.data.remote.mapper.toDomain
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

class LoginUserUseCase(
    private val authApi: AuthApi,
    private val session: MutableCurrentUserProvider
) {
    suspend fun iniciarSesion(id: Int, password: String): DataUsuario {
        require(id > 0) { "Introduce un ID válido" }
        require(password.isNotBlank()) { "Introduce la contraseña" }
        // Solo iniciamos sesión si la API confirma las credenciales.
        val usuario = authApi.iniciarSesion(id, password).toDomain()
        session.iniciarSesion(usuario)
        return usuario
    }
}

