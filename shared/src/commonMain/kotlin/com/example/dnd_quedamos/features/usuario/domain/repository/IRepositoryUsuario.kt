package com.example.dnd_quedamos.features.usuario.domain.repository

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

interface IRepositoryUsuario {
    suspend fun registrar(
        nombre: String,
        email: String,
        password: String
    ): DataUsuario

    suspend fun iniciarSesion(
        email: String,
        password: String
    ): DataUsuario

    suspend fun obtenerPerfil(): DataUsuario?

    suspend fun actualizarPerfil(
        usuario: DataUsuario
    ): DataUsuario

    suspend fun cerrarSesion()

}