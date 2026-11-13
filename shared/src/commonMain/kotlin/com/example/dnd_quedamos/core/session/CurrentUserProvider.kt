package com.example.dnd_quedamos.core.session

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

/** Quién es el usuario que está usando la app. */
interface CurrentUserProvider {
    suspend fun currentUser(): DataUsuario?
}

/**
 * Usuario fijo mientras no hay login.
 * TODO: cuando se integre la rama `user`, delegar en IRepositoryUsuario.obtenerPerfil().
 */
class FakeCurrentUserProvider : CurrentUserProvider {
    override suspend fun currentUser(): DataUsuario = DataUsuario(id = 1, nombre = "Marco")
}
