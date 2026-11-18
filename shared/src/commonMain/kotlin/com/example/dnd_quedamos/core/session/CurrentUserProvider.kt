package com.example.dnd_quedamos.core.session

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

/** Quién es el usuario que está usando la app. */
interface CurrentUserProvider {
    suspend fun currentUser(): DataUsuario?
}

interface MutableCurrentUserProvider : CurrentUserProvider {
    suspend fun iniciarSesion(usuario: DataUsuario)
    suspend fun cerrarSesion()
}

class FakeCurrentUserProvider : MutableCurrentUserProvider {

    private var usuarioActual: DataUsuario? = DataUsuario(
        id = 1,
        nombre = "Marco"
    )

    override suspend fun currentUser(): DataUsuario? {
        return usuarioActual
    }

    override suspend fun iniciarSesion(usuario: DataUsuario) {
        usuarioActual = usuario
    }

    override suspend fun cerrarSesion() {
        usuarioActual = null
    }
}
