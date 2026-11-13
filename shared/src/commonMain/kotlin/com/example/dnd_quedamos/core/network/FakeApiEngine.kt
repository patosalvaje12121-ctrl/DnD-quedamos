package com.example.dnd_quedamos.core.network

import com.example.dnd_quedamos.features.evento.data.remote.dto.EventoDto
import com.example.dnd_quedamos.features.evento.data.remote.dto.ParticipanteDto
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * "Servidor falso" para desarrollar sin backend.
 *
 * Se pasa a createHttpClient(createFakeApiEngine()) y, a partir de ahí, TODAS las peticiones
 * las contesta este código en vez de salir a Internet. El resto de la app (EventoApi, DTOs,
 * repositorio, ViewModel...) funciona exactamente igual que con la API real.
 *
 * Para cambiar entre falso y real: USE_FAKE_API en di/AppModule.kt.
 */

/** Datos de ejemplo que "devuelve el servidor". */
internal val eventosFalsos = listOf(
    EventoDto(1, "Partida en la biblioteca", "Sesión 0 de la nueva campaña. Traed fichas de personaje."),
    EventoDto(2, "One-shot de terror", "Aventura corta nivel 3. Plazas limitadas a 5 jugadores."),
    EventoDto(3, "Taller de pintura de minis", "Trae tus miniaturas, ponemos pinturas y pinceles."),
    EventoDto(4, "Campaña Curse of Strahd", "Sesión 12. Seguimos en el castillo."),
    EventoDto(5, "Noche de juegos de mesa", null), // sin descripción: prueba el mapper (null -> "")
    EventoDto(6, "Torneo de Magic", "Formato draft. Inscripción en la tienda."),
)

/**
 * Quién está apuntado a cada evento (eventoId -> ids de usuario). Se guarda en memoria mientras
 * la app esté abierta, así que apuntarse/desapuntarse se nota al volver a pedir la lista.
 * Los ids de usuario son los de FakeChatServer: 1 Marco, 2 Ana, 3 Luis, 4 Sara.
 */
private val participantesFalsos: Map<Int, MutableList<Int>> = mapOf(
    1 to mutableListOf(1, 2, 3, 4),
    2 to mutableListOf(2),
    3 to mutableListOf(),
    4 to mutableListOf(1, 3),
    5 to mutableListOf(),
    6 to mutableListOf(4),
)
private val participantesMutex = Mutex()

private val rutaParticipantes = Regex("$API/eventos/(\\d+)/participantes")
private val rutaParticipante = Regex("$API/eventos/(\\d+)/participantes/(\\d+)")

/** Simula el tiempo de respuesta de una red real (para ver estados de carga). */
private const val RETARDO_MS = 600L

/** Prefijo de todas las rutas: es la parte de ruta de BASE_URL ("/api/"). */
internal const val API = "/api"

fun createFakeApiEngine(): HttpClientEngine = MockEngine { request ->
    delay(RETARDO_MS)

    val path = request.url.encodedPath   // p. ej. "/api/eventos/3"
    val method = request.method

    // Rutas de chat (devuelve null si la petición no es de chat)
    FakeChatServer.manejar(this, request)?.let { return@MockEngine it }

    // Participantes: van antes que las rutas genéricas de /eventos/ para que no las capturen ellas
    rutaParticipantes.matchEntire(path)?.let { match ->
        val lista = participantesFalsos[match.groupValues[1].toInt()]
            ?: return@MockEngine respondError(HttpStatusCode.NotFound)
        return@MockEngine when (method) {
            // GET -> ids de los apuntados
            HttpMethod.Get -> respondJson(participantesMutex.withLock { AppJson.encodeToString(lista.toList()) })
            // POST -> apuntarse (409 si ya estaba)
            HttpMethod.Post -> {
                val body = AppJson.decodeFromString<ParticipanteDto>(request.body.toByteArray().decodeToString())
                participantesMutex.withLock {
                    if (body.usuarioId in lista) respondError(HttpStatusCode.Conflict)
                    else { lista.add(body.usuarioId); respond("", HttpStatusCode.Created) }
                }
            }
            else -> respondError(HttpStatusCode.MethodNotAllowed)
        }
    }
    // DELETE /api/eventos/{id}/participantes/{usuarioId} -> desapuntarse (404 si no estaba)
    rutaParticipante.matchEntire(path)?.let { match ->
        val lista = participantesFalsos[match.groupValues[1].toInt()]
        val usuarioId = match.groupValues[2].toInt()
        return@MockEngine participantesMutex.withLock {
            when {
                method != HttpMethod.Delete -> respondError(HttpStatusCode.MethodNotAllowed)
                lista == null || !lista.remove(usuarioId) -> respondError(HttpStatusCode.NotFound)
                else -> respond("", HttpStatusCode.NoContent)
            }
        }
    }

    when {
        // GET /api/eventos -> lista completa
        method == HttpMethod.Get && path == "$API/eventos" ->
            respondJson(AppJson.encodeToString(eventosFalsos))

        // GET /api/eventos/{id} -> un evento, o 404 si no existe
        method == HttpMethod.Get && path.startsWith("$API/eventos/") -> {
            val id = path.substringAfterLast("/").toIntOrNull()
            val evento = eventosFalsos.find { it.id == id }
            if (evento != null) respondJson(AppJson.encodeToString(evento))
            else respondError(HttpStatusCode.NotFound)
        }

        // POST / PUT /api/eventos... -> devuelve lo que se envió, como haría un servidor real
        (method == HttpMethod.Post || method == HttpMethod.Put) && path.startsWith("$API/eventos") ->
            respondJson(request.body.toByteArray().decodeToString(), HttpStatusCode.Created)

        // DELETE /api/eventos/{id} -> 204 sin cuerpo
        method == HttpMethod.Delete && path.startsWith("$API/eventos/") ->
            respond("", HttpStatusCode.NoContent)

        // Cualquier otra ruta: 404 (igual que un servidor real con una ruta que no existe)
        else -> respondError(HttpStatusCode.NotFound)
    }
}

/** Respuesta 200 con cabecera JSON (sin ella, Ktor no sabría convertir el cuerpo en DTOs). */
internal fun MockRequestHandleScope.respondJson(
    json: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = json,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)
