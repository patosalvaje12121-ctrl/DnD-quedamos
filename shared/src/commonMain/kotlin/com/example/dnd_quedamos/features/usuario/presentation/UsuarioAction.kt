
package com.example.dnd_quedamos.features.usuario.presentation

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

sealed interface UsuarioAction {

    data class Mostrar(
        val id: Int
    ) : UsuarioAction

    data class Crear(
        val usuario: DataUsuario
    ) : UsuarioAction

    data class Editar(
        val usuario: DataUsuario
    ) : UsuarioAction

    data class Eliminar(
        val id: Int
    ) : UsuarioAction
}
