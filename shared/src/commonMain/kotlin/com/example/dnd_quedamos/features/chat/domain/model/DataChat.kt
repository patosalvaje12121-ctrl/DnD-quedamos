package com.example.dnd_quedamos.features.chat.domain.model

/** Tipo de conversación: privada entre dos usuarios o grupal asociada a un evento. */
enum class TipoChat { DIRECTO, EVENTO }

data class DataChat(
    val id: Int,
    val nombre: String,
    val tipo: TipoChat,
    val participantes: List<Int>,
    val eventoId: Int? = null,          // solo en chats de tipo EVENTO
    val ultimoMensaje: DataMensaje? = null,
)
