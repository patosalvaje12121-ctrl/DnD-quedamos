
package com.example.dnd_quedamos.features.usuario.data.remote

import com.example.dnd_quedamos.features.usuario.data.remote.dto.UsuarioDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UsuarioApi(
    private val client: HttpClient
) {

    // GET /api/usuarios/{id}
    suspend fun getUsuario(id: Int): UsuarioDto =
        client.get("usuarios/$id").body()

    // POST /api/usuarios
    suspend fun crearUsuario(usuario: UsuarioDto): UsuarioDto =
        client.post("usuarios") {
            contentType(ContentType.Application.Json)
            setBody(usuario)
        }.body()

    // PUT /api/usuarios/{id}
    suspend fun actualizarUsuario(usuario: UsuarioDto): UsuarioDto =
        client.put("usuarios/${usuario.id}") {
            contentType(ContentType.Application.Json)
            setBody(usuario)
        }.body()

    // DELETE /api/usuarios/{id}
    suspend fun borrarUsuario(id: Int) {
        client.delete("usuarios/$id")
    }
}
