package com.example.dnd_quedamos.features.chat.data.repository

import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.chat.data.remote.ChatApi
import com.example.dnd_quedamos.features.chat.data.remote.dto.CrearChatDirectoDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.EnviarMensajeDto
import com.example.dnd_quedamos.features.chat.data.remote.mapper.toDomain
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.DataContacto
import com.example.dnd_quedamos.features.chat.domain.model.DataMensaje
import com.example.dnd_quedamos.features.chat.domain.repository.IRepositoryChat

/**
 * Recibe DTOs de ChatApi y devuelve modelos de dominio.
 *
 * Usa CurrentUserProvider para decir a la API quién pregunta: en los chats directos
 * el nombre del chat es el del OTRO usuario, así que depende de quién lo pida.
 */
class RepositoryChat(
    private val api: ChatApi,
    private val currentUser: CurrentUserProvider,
) : IRepositoryChat {

    override suspend fun getChats(userId: Int): List<DataChat> =
        api.getChats(userId).toDomain()

    override suspend fun getChat(chatId: Int): DataChat =
        api.getChat(chatId, yo()).toDomain()

    override suspend fun getMensajes(chatId: Int): List<DataMensaje> =
        api.getMensajes(chatId).toDomain()

    override suspend fun enviarMensaje(chatId: Int, autorId: Int, texto: String): DataMensaje =
        api.enviarMensaje(chatId, EnviarMensajeDto(autorId, texto)).toDomain()

    override suspend fun getOrCreateChatDirecto(userA: Int, userB: Int): DataChat =
        api.getOrCreateChatDirecto(CrearChatDirectoDto(userA, userB)).toDomain()

    override suspend fun getChatDeEvento(eventoId: Int): DataChat =
        api.getChatDeEvento(eventoId, yo()).toDomain()

    override suspend fun getContactos(): List<DataContacto> =
        api.getUsuarios().toDomain()

    private suspend fun yo(): Int =
        currentUser.currentUser()?.id ?: error("No hay usuario con sesión iniciada")
}
