package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.MutableCurrentUserProvider
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario

class DeleteUserUseCase(
    private val repository: IRepositoryUsuario,
    private val session: MutableCurrentUserProvider
) {
    suspend fun eliminarCuenta(): Boolean {
        val usuario = session.currentUser() ?: error("No hay sesión iniciada")
        val eliminado = repository.eliminarUsuario(usuario.id)
        if (eliminado) session.cerrarSesion()
        return eliminado
    }
}
