package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.MutableCurrentUserProvider
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

//TODO: ver que hacer con la contraseña
class RegisterUserUseCase(
    private val repository: IRepositoryUsuario,
    private val session: MutableCurrentUserProvider
) {
    suspend fun registrar(nombre: String, descripcion: String): DataUsuario {
        val nombreLimpio = nombre.trim()
        val bioLimpia = descripcion.trim()
        require(nombreLimpio.isNotEmpty() && nombreLimpio.length <= 50) {
            "El nombre debe tener entre 1 y 50 caracteres"
        }
        require(bioLimpia.length <= 500) { "La biografía no puede superar 500 caracteres" }
        val creado = repository.crearUsuario(DataUsuario(id = 0, nombre = nombreLimpio, descBio = bioLimpia))
        session.iniciarSesion(creado)
        return creado
    }
}
