package com.example.dnd_quedamos.core.session

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

interface CurrentUserProvider {
    suspend fun currentUser(): DataUsuario?
}

class FakeCurrentUserProvider : CurrentUserProvider {

    override suspend fun currentUser(): DataUsuario {
        return DataUsuario(
            id = 1,
            name = "Admin"
        )
    }
}
