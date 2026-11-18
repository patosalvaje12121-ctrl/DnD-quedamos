
package com.example.dnd_quedamos.features.usuario.data.repository

import com.example.dnd_quedamos.features.usuario.data.remote.PermisosUsuarioApi
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryPermisosUsuario

class RepositoryPermisosUsuario(
    private val permisosUsuarioApi: PermisosUsuarioApi
) : IRepositoryPermisosUsuario {

    override suspend fun esAdmin(): Boolean {
        return permisosUsuarioApi.esAdmin()
    }
}
