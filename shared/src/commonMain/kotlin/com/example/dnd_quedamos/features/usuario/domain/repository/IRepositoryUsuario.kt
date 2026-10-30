package com.example.dnd_quedamos.features.usuario.domain.repository

import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario

interface IRepositoryUsuario {
    suspend fun bbddUsuario(ID: Int) : DataUsuario;
}