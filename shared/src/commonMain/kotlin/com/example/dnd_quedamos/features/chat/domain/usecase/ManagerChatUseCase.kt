package com.example.dnd_quedamos.features.chat.domain.usecase

import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.model.DataContacto
import com.example.dnd_quedamos.features.chat.domain.model.DataMensaje
import com.example.dnd_quedamos.features.chat.domain.repository.IRepositoryChat
import kotlinx.coroutines.CancellationException

class ManagerChatUseCase(
    private val repositoryChat: IRepositoryChat
) {

    suspend fun getChats(userId: Int): Result<List<DataChat>> =
        runSafe { repositoryChat.getChats(userId) }

    suspend fun getChat(chatId: Int): Result<DataChat> =
        runSafe { repositoryChat.getChat(chatId) }

    suspend fun getMensajes(chatId: Int): Result<List<DataMensaje>> =
        runSafe { repositoryChat.getMensajes(chatId) }

    suspend fun enviarMensaje(chatId: Int, autorId: Int, texto: String): Result<DataMensaje> {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return Result.failure(IllegalArgumentException("Mensaje vacío"))
        return runSafe { repositoryChat.enviarMensaje(chatId, autorId, limpio) }
    }

    suspend fun getOrCreateChatDirecto(userA: Int, userB: Int): Result<DataChat> =
        runSafe { repositoryChat.getOrCreateChatDirecto(userA, userB) }

    suspend fun getChatDeEvento(eventoId: Int): Result<DataChat> =
        runSafe { repositoryChat.getChatDeEvento(eventoId) }

    /** Usuarios con los que [yo] puede empezar un chat (todos menos yo), ordenados por nombre. */
    suspend fun getContactos(yo: Int): Result<List<DataContacto>> =
        runSafe { repositoryChat.getContactos().filter { it.id != yo }.sortedBy { it.nombre } }

    // Igual que en ManagerEventoUseCase: errores -> Result.failure, la cancelación se deja pasar
    private inline fun <T> runSafe(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
