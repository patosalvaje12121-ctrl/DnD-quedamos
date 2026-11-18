package com.example.dnd_quedamos.core.network

import com.example.dnd_quedamos.features.chat.data.remote.dto.ChatDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.ContactoDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.CrearChatDirectoDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.EnviarMensajeDto
import com.example.dnd_quedamos.features.chat.data.remote.dto.MensajeDto
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Parte de chat del "servidor falso" (ver FakeApiEngine.kt). Guarda chats y mensajes en memoria
 * mientras la app esté abierta, así que lo que envías sigue ahí al volver a entrar en el chat.
 *
 * Rutas:
 *   GET  /api/chats?userId=              -> chats del usuario
 *   GET  /api/chats/{id}?userId=         -> un chat
 *   GET  /api/chats/{id}/mensajes?desde= -> mensajes
 *   POST /api/chats/{id}/mensajes        -> enviar mensaje
 *   POST /api/chats/directo              -> chat privado (se crea si no existe)
 *   GET  /api/eventos/{id}/chat?userId=  -> chat del evento (se crea si no existe)
 *   GET  /api/usuarios                   -> lista de usuarios (para empezar un chat nuevo)
 *
 * Extra para probar el polling: en los chats directos, el otro usuario "contesta" unos
 * segundos después de que le escribas.
 */
internal object FakeChatServer {

    private val usuarios = mapOf(1 to "Marco", 2 to "Ana", 3 to "Luis", 4 to "Sara")

    private class Chat(
        val id: Int,
        val tipo: String,
        val participantes: List<Int>,
        val eventoId: Int? = null,
        val nombreEvento: String? = null,
    )

    private val mutex = Mutex()
    private val chats = mutableListOf<Chat>()
    private val mensajes = mutableListOf<MensajeDto>()
    private var nextChatId = 1
    private var nextMensajeId = 1

    private val respuestasAutomaticas = listOf(
        "¡Jaja, buena esa!",
        "Me apunto 🎲",
        "¿A qué hora quedamos?",
        "Llevo mi pícaro, que el bardo murió la última vez…",
        "Vale, lo hablamos en la partida.",
        "Tiro iniciativa: 17 ⚔️",
    )

    init {
        val ahora = ahora()
        val minuto = 60_000L
        val dmAna = nuevoChat("DIRECTO", listOf(1, 2))
        mensajeSemilla(dmAna, 2, "¡Hola Marco! ¿Vienes el viernes?", ahora - 30 * minuto)
        mensajeSemilla(dmAna, 1, "¡Claro! Llevo los dados.", ahora - 28 * minuto)
        mensajeSemilla(dmAna, 2, "Perfecto, nos vemos allí 🐉", ahora - 27 * minuto)

        val dmLuis = nuevoChat("DIRECTO", listOf(1, 3))
        mensajeSemilla(dmLuis, 3, "¿Me prestas el Manual del Jugador?", ahora - 120 * minuto)

        eventosFalsos.find { it.id == 1 }?.let { ev ->
            val c = nuevoChat("EVENTO", usuarios.keys.toList(), ev.id, ev.nombre)
            mensajeSemilla(c, 4, "Yo llevo snacks 🍕", ahora - 90 * minuto)
            mensajeSemilla(c, 3, "Yo hago de DM esta vez", ahora - 80 * minuto)
            mensajeSemilla(c, 2, "¡Genial!", ahora - 75 * minuto)
        }
        eventosFalsos.find { it.id == 4 }?.let { ev ->
            val c = nuevoChat("EVENTO", usuarios.keys.toList(), ev.id, ev.nombre)
            mensajeSemilla(c, 3, "Strahd nos espera… 🧛", ahora - 300 * minuto)
        }
    }

    /** Devuelve la respuesta si la petición es de chat, o null para que la maneje otra parte. */
    suspend fun manejar(scope: MockRequestHandleScope, request: HttpRequestData): HttpResponseData? {
        val path = request.url.encodedPath.removePrefix("$API/").trimEnd('/')
        val seg = path.split("/")
        val method = request.method
        val userId = request.url.parameters["userId"]?.toIntOrNull()

        return mutex.withLock {
            with(scope) {
                when {
                    // GET chats
                    method == HttpMethod.Get && seg == listOf("chats") -> {
                        val uid = userId ?: return@withLock respondError(HttpStatusCode.BadRequest)
                        val lista = chats
                            .filter { uid in it.participantes }
                            .map { it.toDto(uid) }
                            .sortedByDescending { it.ultimoMensaje?.timestamp ?: 0L }
                        respondJson(AppJson.encodeToString(lista))
                    }

                    // GET usuarios -> todos los usuarios (para elegir con quién empezar un chat)
                    method == HttpMethod.Get && seg == listOf("usuarios") -> {
                        val lista = usuarios.map { (id, nombre) -> ContactoDto(id, nombre) }
                        respondJson(AppJson.encodeToString(lista))
                    }

                    // POST chats/directo
                    method == HttpMethod.Post && seg == listOf("chats", "directo") -> {
                        val body = AppJson.decodeFromString<CrearChatDirectoDto>(leerCuerpo(request))
                        if (body.userA == body.userB || body.userB !in usuarios) {
                            return@withLock respondError(HttpStatusCode.BadRequest)
                        }
                        val chat = chats.find {
                            it.tipo == "DIRECTO" && it.participantes.toSet() == setOf(body.userA, body.userB)
                        } ?: nuevoChat("DIRECTO", listOf(body.userA, body.userB))
                        respondJson(AppJson.encodeToString(chat.toDto(body.userA)))
                    }

                    // GET chats/{id}
                    method == HttpMethod.Get && seg.size == 2 && seg[0] == "chats" -> {
                        val chat = buscarChat(seg[1]) ?: return@withLock respondError(HttpStatusCode.NotFound)
                        respondJson(AppJson.encodeToString(chat.toDto(userId)))
                    }

                    // GET chats/{id}/mensajes
                    method == HttpMethod.Get && seg.size == 3 && seg[0] == "chats" && seg[2] == "mensajes" -> {
                        val chat = buscarChat(seg[1]) ?: return@withLock respondError(HttpStatusCode.NotFound)
                        val desde = request.url.parameters["desde"]?.toIntOrNull() ?: 0
                        respondJson(AppJson.encodeToString(mensajesVisibles(chat.id).filter { it.id > desde }))
                    }

                    // POST chats/{id}/mensajes
                    method == HttpMethod.Post && seg.size == 3 && seg[0] == "chats" && seg[2] == "mensajes" -> {
                        val chat = buscarChat(seg[1]) ?: return@withLock respondError(HttpStatusCode.NotFound)
                        val body = AppJson.decodeFromString<EnviarMensajeDto>(leerCuerpo(request))
                        if (body.texto.isBlank() || body.autorId !in chat.participantes) {
                            return@withLock respondError(HttpStatusCode.BadRequest)
                        }
                        val ahora = ahora()
                        val creado = mensajeSemilla(chat, body.autorId, body.texto.trim(), ahora)
                        // Respuesta automática en chats directos, "llega" 2.5 s después
                        if (chat.tipo == "DIRECTO") {
                            val otro = chat.participantes.first { it != body.autorId }
                            mensajeSemilla(chat, otro, respuestasAutomaticas.random(), ahora + 2_500)
                        }
                        respondJson(AppJson.encodeToString(creado), HttpStatusCode.Created)
                    }

                    // GET eventos/{id}/chat
                    method == HttpMethod.Get && seg.size == 3 && seg[0] == "eventos" && seg[2] == "chat" -> {
                        val eventoId = seg[1].toIntOrNull()
                        // Se busca en el servidor de eventos (no en los datos de ejemplo) para que
                        // los eventos creados desde la app también tengan chat
                        val evento = eventoId?.let { FakeEventoServer.buscarEvento(it) }
                            ?: return@withLock respondError(HttpStatusCode.NotFound)
                        val chat = chats.find { it.tipo == "EVENTO" && it.eventoId == evento.id }
                            ?: nuevoChat("EVENTO", usuarios.keys.toList(), evento.id, evento.nombre)
                        respondJson(AppJson.encodeToString(chat.toDto(userId)))
                    }

                    else -> null
                }
            }
        }
    }

    // ---------- helpers (llamar siempre con el mutex cogido) ----------

    @OptIn(ExperimentalTime::class)
    private fun ahora(): Long = Clock.System.now().toEpochMilliseconds()

    private suspend fun leerCuerpo(request: HttpRequestData): String =
        request.body.toByteArray().decodeToString()

    private fun buscarChat(id: String): Chat? = chats.find { it.id == id.toIntOrNull() }

    private fun nuevoChat(
        tipo: String,
        participantes: List<Int>,
        eventoId: Int? = null,
        nombreEvento: String? = null,
    ): Chat = Chat(nextChatId++, tipo, participantes, eventoId, nombreEvento).also { chats += it }

    private fun mensajeSemilla(chat: Chat, autorId: Int, texto: String, timestamp: Long): MensajeDto =
        MensajeDto(
            id = nextMensajeId++,
            chatId = chat.id,
            autorId = autorId,
            autorNombre = usuarios[autorId],
            texto = texto,
            timestamp = timestamp,
        ).also { mensajes += it }

    /** Mensajes ya "enviados" (las respuestas automáticas programadas a futuro aún no se ven). */
    private fun mensajesVisibles(chatId: Int): List<MensajeDto> {
        val ahora = ahora()
        return mensajes.filter { it.chatId == chatId && it.timestamp <= ahora }.sortedBy { it.timestamp }
    }

    /** En chats directos el nombre es el del OTRO participante, visto desde [viewer]. */
    private fun Chat.toDto(viewer: Int?): ChatDto = ChatDto(
        id = id,
        nombre = when (tipo) {
            "EVENTO" -> nombreEvento ?: "Evento $eventoId"
            else -> participantes.firstOrNull { it != viewer }?.let { usuarios[it] } ?: "Chat"
        },
        tipo = tipo,
        participantes = participantes,
        eventoId = eventoId,
        ultimoMensaje = mensajesVisibles(id).lastOrNull(),
    )
}
