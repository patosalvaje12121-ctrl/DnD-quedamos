package com.example.dnd_quedamos.features.chat.data.remote

import com.example.dnd_quedamos.features.chat.data.remote.dto.ChatDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.CrearChatDirectoDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.EnviarMensajeDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.MensajeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Endpoints de chat (relativos a BASE_URL). Igual que EventoApi: solo DTOs y sin capturar errores.
 */
class ChatApi(
    private val client: HttpClient,
) {

    /** GET chats?userId= -> chats en los que participa el usuario */
    suspend fun getChats(userId: Int): List<ChatDto> =
        client.get("chats") { parameter("userId", userId) }.body()

    /** GET chats/{id}?userId= -> un chat (el nombre depende de quién lo pide en los directos) */
    suspend fun getChat(chatId: Int, userId: Int): ChatDto =
        client.get("chats/$chatId") { parameter("userId", userId) }.body()

    /** GET chats/{id}/mensajes?desde= -> mensajes, opcionalmente solo los posteriores a un id */
    suspend fun getMensajes(chatId: Int, desdeId: Int? = null): List<MensajeDto> =
        client.get("chats/$chatId/mensajes") {
            if (desdeId != null) parameter("desde", desdeId)
        }.body()

    /** POST chats/{id}/mensajes -> crea un mensaje y lo devuelve completo */
    suspend fun enviarMensaje(chatId: Int, mensaje: EnviarMensajeDto): MensajeDto =
        client.post("chats/$chatId/mensajes") {
            contentType(ContentType.Application.Json)
            setBody(mensaje)
        }.body()

    /** POST chats/directo -> chat privado entre dos usuarios (lo crea si no existe) */
    suspend fun getOrCreateChatDirecto(body: CrearChatDirectoDto): ChatDto =
        client.post("chats/directo") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    /** GET eventos/{id}/chat?userId= -> chat de grupo del evento (lo crea si no existe) */
    suspend fun getChatDeEvento(eventoId: Int, userId: Int): ChatDto =
        client.get("eventos/$eventoId/chat") { parameter("userId", userId) }.body()
}
