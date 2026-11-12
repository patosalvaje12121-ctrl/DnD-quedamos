package com.example.dnd_quedamos.features.chat.data.remote.mapper

import com.example.dnd_quedamos.features.chat.data.remote.dto.ChatDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.MensajeDto
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.DataMensaje
import com.example.dnd_quedamos.features.chat.domain.model.TipoChat
import kotlin.jvm.JvmName

fun MensajeDto.toDomain(): DataMensaje = DataMensaje(
    id = id,
    chatId = chatId,
    autorId = autorId,
    autorNombre = autorNombre ?: "Usuario $autorId", // sin nombre en la API -> uno genérico
    texto = texto,
    timestamp = timestamp,
)

fun ChatDto.toDomain(): DataChat = DataChat(
    id = id,
    nombre = nombre,
    // Un tipo desconocido se trata como chat directo en vez de romper la app
    tipo = TipoChat.entries.firstOrNull { it.name == tipo } ?: TipoChat.DIRECTO,
    participantes = participantes,
    eventoId = eventoId,
    ultimoMensaje = ultimoMensaje?.toDomain(),
)

@JvmName("chatsToDomain")
fun List<ChatDto>.toDomain(): List<DataChat> = map { it.toDomain() }

@JvmName("mensajesToDomain")
fun List<MensajeDto>.toDomain(): List<DataMensaje> = map { it.toDomain() }
