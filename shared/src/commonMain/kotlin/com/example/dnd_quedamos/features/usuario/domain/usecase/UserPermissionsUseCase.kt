
package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryPermisosUsuario

class UserPermissionsUseCase(
    private val repository: IRepositoryPermisosUsuario
) {
    suspend fun esAdmin(): Boolean {
        return repository.esAdmin()
    }
}
//Esta hecho asi para mayor escalabilidad, por ejemplo en un futuro se puede implementar
//lo que se propuso al principio de que un usuario solo puede crear eventos si tenia x reputacion
//o cualquier otra cosa relacionada con permisos de usuario sin tener que tocar ManagerUsuarioUseCase