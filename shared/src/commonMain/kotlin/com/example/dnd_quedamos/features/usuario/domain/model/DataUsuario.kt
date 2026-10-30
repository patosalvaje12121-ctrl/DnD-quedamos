package com.example.dnd_quedamos.features.usuario.domain.model

data class DataUsuario (
    val id : Int,
    val name : String,
    val imageID: Int,
    val descBio: String,

    val assistedEvents: List<Int>,
)

