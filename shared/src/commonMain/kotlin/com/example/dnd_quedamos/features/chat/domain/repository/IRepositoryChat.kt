package com.example.dnd_quedamos.features.chat.domain.repository

import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.DataContacto
import com.example.dnd_quedamos.features.chat.domain.model.DataMensaje

interface IRepositoryChat {
    /** Chats (directos y de evento) en los que participa el usuario. */
    suspend fun getChats(userId: Int): List<DataChat>

    suspend fun getChat(chatId: Int): DataChat

    /** Mensajes de un chat, ordenados del más antiguo al más reciente. */
    suspend fun getMensajes(chatId: Int): List<DataMensaje>

    suspend fun enviarMensaje(chatId: Int, autorId: Int, texto: String): DataMensaje

    /** Devuelve el chat directo entre dos usuarios, creándolo si no existe. */
    suspend fun getOrCreateChatDirecto(userA: Int, userB: Int): DataChat

    /** Devuelve el chat de grupo de un evento, creándolo si no existe. */
    suspend fun getChatDeEvento(eventoId: Int): DataChat

    /** Usuarios con los que se puede empezar un chat directo. */
    suspend fun getContactos(): List<DataContacto>
}
