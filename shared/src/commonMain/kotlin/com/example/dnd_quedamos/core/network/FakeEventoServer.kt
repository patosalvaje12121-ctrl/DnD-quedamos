@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.core.network

import com.example.dnd_quedamos.features.evento.data.remote.dto.EventoDto
import com.example.dnd_quedamos.features.evento.data.remote.dto.ParticipanteDto
import com.example.dnd_quedamos.features.evento.data.remote.dto.UbicacionDto
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/**
 * Datos de ejemplo con los que arranca el servidor falso.
 *
 * Están pensados para probar casos concretos:
 *  - Fechas relativas a "ahora": siempre hay eventos próximos y uno pasado (el 4), sin importar
 *    cuándo se ejecute la app.
 *  - Coordenadas por Madrid, para que el mapa muestre chinchetas cerca.
 *  - Evento 2 completo (3/3) para probar "no quedan plazas".
 *  - Evento 5 sin descripción ni dirección (null -> "" en el mapper) y organizado por el usuario
 *    actual (1, Marco) para probar editar/cancelar.
 *  - Evento 6 con un tipo que la app no conoce ("TORNEO") -> el mapper lo convierte en OTRO.
 *
 * Los ids de usuario son los de FakeChatServer: 1 Marco, 2 Ana, 3 Luis, 4 Sara.
 * FakeChatServer usa esta lista para crear los chats de ejemplo de los eventos 1 y 4.
 */
internal val eventosFalsos: List<EventoDto> = run {
    val ahora = Clock.System.now()
    fun en(d: Duration) = (ahora + d).toString() // Instant -> ISO-8601, igual que la API real
    listOf(
        EventoDto(
            1, "Partida en la biblioteca", "Sesión 0 de la nueva campaña. Traed fichas de personaje.",
            en(3.days), UbicacionDto("Biblioteca María Zambrano, Ciudad Universitaria", 40.4508, -3.7277),
            "ROL", organizadorId = 3, maxParticipantes = 6,
        ),
        EventoDto(
            2, "One-shot de terror", "Aventura corta nivel 3. Plazas limitadas a 3 jugadores.",
            en(1.days + 2.hours), UbicacionDto("Parque del Oeste, Madrid", 40.4296, -3.7244),
            "ROL", organizadorId = 2, maxParticipantes = 3,
        ),
        EventoDto(
            3, "Taller de pintura de minis", "Trae tus miniaturas, ponemos pinturas y pinceles.",
            en(7.days), UbicacionDto("Calle de Embajadores 35, Lavapiés", 40.4087, -3.7012),
            "WARGAME", organizadorId = 4, maxParticipantes = 10,
        ),
        EventoDto(
            4, "Campaña Curse of Strahd", "Sesión 12. Seguimos en el castillo.",
            en(-3.days), UbicacionDto("Parque del Retiro, Madrid", 40.4153, -3.6844),
            "ROL", organizadorId = 3, maxParticipantes = 5,
        ),
        EventoDto(
            5, "Noche de juegos de mesa", null,
            en(14.days), UbicacionDto(null, 40.4260, -3.7040),
            "JUEGOS_DE_MESA", organizadorId = 1, maxParticipantes = 8,
        ),
        EventoDto(
            6, "Torneo de Magic", "Formato draft. Inscripción en la tienda.",
            en(30.days), UbicacionDto("Calle de Fuencarral 80, Malasaña", 40.4255, -3.7018),
            "TORNEO", organizadorId = 2, maxParticipantes = 32,
        ),
        EventoDto(
            7, "Convención de rol de Madrid", "Mesas abiertas, charlas y tiendas durante todo el día.",
            en(45.days), UbicacionDto("IFEMA, Madrid", 40.4637, -3.6164),
            "CONVENCION", organizadorId = 4, maxParticipantes = 500,
        ),
    )
}

/**
 * Parte de eventos del "servidor falso" (ver FakeApiEngine.kt). Guarda eventos y participantes
 * en memoria mientras la app esté abierta: crear, editar, cancelar, apuntarse y desapuntarse
 * se ven al volver a pedir los datos.
 *
 * Rutas:
 *   GET    /api/eventos                              -> todos los eventos
 *   GET    /api/eventos/{id}                         -> un evento
 *   POST   /api/eventos                              -> crear (el servidor pone el id y apunta al organizador)
 *   PUT    /api/eventos/{id}                         -> editar (id y organizador no se pueden cambiar)
 *   DELETE /api/eventos/{id}                         -> cancelar
 *   GET    /api/eventos/{id}/participantes           -> ids de los apuntados
 *   POST   /api/eventos/{id}/participantes           -> apuntarse (409 si ya estaba o está completo)
 *   DELETE /api/eventos/{id}/participantes/{usuario} -> desapuntarse (404 si no estaba)
 *
 * Los eventos se guardan SIN participantes; el campo "participantes" se rellena al responder,
 * así solo hay una fuente de verdad (el mapa [participantes]).
 */
internal object FakeEventoServer {

    private val mutex = Mutex()
    private val eventos = eventosFalsos.toMutableList()
    private val participantes: MutableMap<Int, MutableList<Int>> = mutableMapOf(
        1 to mutableListOf(1, 2, 3, 4),
        2 to mutableListOf(2, 3, 4),      // completo (max 3)
        3 to mutableListOf(4),
        4 to mutableListOf(1, 3),
        5 to mutableListOf(1),
        6 to mutableListOf(2, 4),
        7 to mutableListOf(),
    )
    private var nextId = (eventos.maxOfOrNull { it.id } ?: 0) + 1

    /** Para otras partes del servidor falso (p. ej. el chat de un evento). */
    suspend fun buscarEvento(id: Int): EventoDto? = mutex.withLock { eventos.find { it.id == id } }

    /** Devuelve la respuesta si la petición es de eventos, o null para que la maneje otra parte. */
    suspend fun manejar(scope: MockRequestHandleScope, request: HttpRequestData): HttpResponseData? {
        val seg = request.url.encodedPath.removePrefix("$API/").trimEnd('/').split("/")
        if (seg[0] != "eventos") return null
        val method = request.method
        val cuerpo = if (method == HttpMethod.Post || method == HttpMethod.Put) {
            request.body.toByteArray().decodeToString()
        } else ""

        return mutex.withLock {
            with(scope) {
                val id = seg.getOrNull(1)?.toIntOrNull()
                when {
                    // GET eventos
                    method == HttpMethod.Get && seg.size == 1 ->
                        respondJson(AppJson.encodeToString(eventos.map { it.conParticipantes() }))

                    // POST eventos
                    method == HttpMethod.Post && seg.size == 1 -> {
                        val nuevo = AppJson.decodeFromString<EventoDto>(cuerpo).copy(id = nextId++)
                        eventos += nuevo
                        participantes[nuevo.id] = mutableListOf(nuevo.organizadorId)
                        respondJson(AppJson.encodeToString(nuevo.conParticipantes()), HttpStatusCode.Created)
                    }

                    // Todo lo demás necesita un evento que exista
                    id == null -> respondError(HttpStatusCode.NotFound)
                    eventos.none { it.id == id } -> respondError(HttpStatusCode.NotFound)

                    // GET eventos/{id}
                    method == HttpMethod.Get && seg.size == 2 ->
                        respondJson(AppJson.encodeToString(evento(id).conParticipantes()))

                    // PUT eventos/{id}
                    method == HttpMethod.Put && seg.size == 2 -> {
                        val actual = evento(id)
                        val editado = AppJson.decodeFromString<EventoDto>(cuerpo)
                            .copy(id = id, organizadorId = actual.organizadorId)
                        eventos[eventos.indexOf(actual)] = editado
                        respondJson(AppJson.encodeToString(editado.conParticipantes()))
                    }

                    // DELETE eventos/{id}
                    method == HttpMethod.Delete && seg.size == 2 -> {
                        eventos.removeAll { it.id == id }
                        participantes.remove(id)
                        respond("", HttpStatusCode.NoContent)
                    }

                    // GET eventos/{id}/participantes
                    method == HttpMethod.Get && seg.size == 3 && seg[2] == "participantes" ->
                        respondJson(AppJson.encodeToString(apuntados(id).toList()))

                    // POST eventos/{id}/participantes
                    method == HttpMethod.Post && seg.size == 3 && seg[2] == "participantes" -> {
                        val usuarioId = AppJson.decodeFromString<ParticipanteDto>(cuerpo).usuarioId
                        val lista = apuntados(id)
                        if (usuarioId in lista || lista.size >= evento(id).maxParticipantes) {
                            respondError(HttpStatusCode.Conflict)
                        } else {
                            lista += usuarioId
                            respond("", HttpStatusCode.Created)
                        }
                    }

                    // DELETE eventos/{id}/participantes/{usuarioId}
                    method == HttpMethod.Delete && seg.size == 4 && seg[2] == "participantes" -> {
                        val usuarioId = seg[3].toIntOrNull()
                        if (usuarioId != null && apuntados(id).remove(usuarioId)) {
                            respond("", HttpStatusCode.NoContent)
                        } else {
                            respondError(HttpStatusCode.NotFound)
                        }
                    }

                    // Otras rutas bajo /eventos (p. ej. eventos/{id}/chat la contesta FakeChatServer antes)
                    else -> null
                }
            }
        }
    }

    // ---------- helpers (llamar siempre con el mutex cogido) ----------

    private fun evento(id: Int): EventoDto = eventos.first { it.id == id }

    private fun apuntados(id: Int): MutableList<Int> = participantes.getOrPut(id) { mutableListOf() }

    private fun EventoDto.conParticipantes(): EventoDto = copy(participantes = apuntados(id).toList())
}
