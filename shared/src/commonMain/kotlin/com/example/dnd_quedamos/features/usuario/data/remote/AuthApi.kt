package com.example.dnd_quedamos.features.usuario.data.remote

import com.example.dnd_quedamos.features.usuario.data.remote.dto.UsuarioDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class AuthApi(private val client: HttpClient) {
    suspend fun registrar(nombre: String, biografia: String, password: String): UsuarioDto =
        client.post("auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegistroRequest(nombre, biografia, password))
        }.body()

    suspend fun iniciarSesion(nombre: String, password: String): UsuarioDto =
        client.post("auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(nombre, password))
        }.body()
}

@Serializable
private data class RegistroRequest(val nombre: String, val biografia: String, val password: String)

@Serializable
private data class LoginRequest(val nombre: String, val password: String)
