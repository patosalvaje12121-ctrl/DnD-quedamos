
package com.example.dnd_quedamos.features.usuario.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
private data class PermisosResponse(
    val esAdmin: Boolean
)

class PermisosUsuarioApi(
    private val client: HttpClient
) {
    suspend fun esAdmin(): Boolean =
        client.get("usuarios/me/permisos")
            .body<PermisosResponse>()
            .esAdmin
}
