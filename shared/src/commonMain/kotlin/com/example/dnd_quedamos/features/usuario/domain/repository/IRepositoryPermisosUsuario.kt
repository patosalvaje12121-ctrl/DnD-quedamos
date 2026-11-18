
package com.example.dnd_quedamos.features.usuario.domain.repository

interface IRepositoryPermisosUsuario {
    suspend fun esAdmin(): Boolean
}
