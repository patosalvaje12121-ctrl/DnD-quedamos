
package com.example.dnd_quedamos.features.usuario.data.repository

import com.example.dnd_quedamos.features.usuario.data.remote.UsuarioApi
import com.example.dnd_quedamos.features.usuario.data.remote.mapper.toDomain
import com.example.dnd_quedamos.features.usuario.data.remote.mapper.toDto
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

class RepositoryUsuarioApi(
    private val usuarioApi: UsuarioApi
) : IRepositoryUsuario {

    override suspend fun bbddUsuario(ID: Int): DataUsuario? {
        return obtenerUsuario(ID)
    }

    override suspend fun obtenerUsuario(id: Int): DataUsuario? {
        return try {
            usuarioApi.getUsuario(id).toDomain()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                null
            } else {
                throw e
            }
        }
    }

    override suspend fun crearUsuario(
        usuario: DataUsuario
    ): DataUsuario {
        return usuarioApi.crearUsuario(
            usuario.toDto()
        ).toDomain()
    }

    override suspend fun editarUsuario(
        usuario: DataUsuario
    ): DataUsuario {
        return usuarioApi.actualizarUsuario(
            usuario.toDto()
        ).toDomain()
    }

    override suspend fun eliminarUsuario(id: Int): Boolean {
        return try {
            usuarioApi.borrarUsuario(id)
            true
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                false
            } else {
                throw e
            }
        }
    }
}
