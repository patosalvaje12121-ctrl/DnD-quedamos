package com.example.dnd_quedamos.features.usuario.domain.model

data class DataUsuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val descripcion: String = "",
    val intereses: List<String> = emptyList()
)