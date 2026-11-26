package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.MutableCurrentUserProvider
import com.example.dnd_quedamos.features.usuario.data.remote.AuthApi
import com.example.dnd_quedamos.features.usuario.data.remote.mapper.toDomain
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

// TODO: obligar a elegir foto de perfil al registrarse (SeleccionFotoDialog) y enviar imagenId; ahora se crea con 0 (sin foto)
class RegisterUserUseCase(
    private val authApi: AuthApi,
    private val session: MutableCurrentUserProvider
) {
    suspend fun registrar(nombre: String, descripcion: String, password: String, repetir: String): DataUsuario {
        val nombreLimpio = nombre.trim()
        val bioLimpia = descripcion.trim()
        require(nombreLimpio.isNotEmpty() && nombreLimpio.length <= 50) {
            "El nombre debe tener entre 1 y 50 caracteres"
        }
        require(bioLimpia.length <= 500) { "La biografía no puede superar 500 caracteres" }
        require(password.isNotBlank()) { "Introduce una contraseña" }
        require(password == repetir) { "Las contraseñas no coinciden" }

        // El hash se calcula en FakeLoginServer, nunca en la pantalla.
        val usuario = authApi.registrar(nombreLimpio, bioLimpia, password).toDomain()
        session.iniciarSesion(usuario)
        return usuario
    }
}