package com.example.dnd_quedamos.features.usuario.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    @SerialName("id")
    val id: Int,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("imagenId")
    val imagenId: Int = 0,

    @SerialName("biografia")
    val biografia: String = "",

    @SerialName("eventosAsistidos")
    val eventosAsistidos: List<Int> = emptyList()
)
