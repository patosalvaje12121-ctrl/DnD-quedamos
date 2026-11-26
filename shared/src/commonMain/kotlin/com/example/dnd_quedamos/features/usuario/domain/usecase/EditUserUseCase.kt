package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

class EditUserUseCase(
    private val repository: IRepositoryUsuario,
    private val session: CurrentUserProvider
) {
    suspend fun editar(usuario: DataUsuario, nombre: String, descripcion: String, imagenId: Int): DataUsuario {
        val actual = session.currentUser() ?: error("No hay sesión iniciada")
        check(actual.id == usuario.id) { "No puedes editar otro usuario" }
        val nombreLimpio = nombre.trim()
        val bioLimpia = descripcion.trim()
        require(nombreLimpio.isNotEmpty() && nombreLimpio.length <= 50) {
            "El nombre debe tener entre 1 y 50 caracteres"
        }
        require(bioLimpia.length <= 500) { "La biografía no puede superar 500 caracteres" }
        require(imagenId >= 0) { "Imagen no válida" }
        // Conservamos los eventos asistidos del registro persistido.
        val guardado = repository.obtenerUsuario(actual.id)
            ?: throw NoSuchElementException("El usuario no existe")
        val actualizado = repository.editarUsuario(
            guardado.copy(nombre = nombreLimpio, descBio = bioLimpia, imageID = imagenId)
        )
        // La sesión de prueba contiene también datos del usuario.
        // El perfil se recarga después del guardado para reflejar los datos de la API.
        return actualizado
    }
}
