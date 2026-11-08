package com.example.dnd_quedamos.core.network

import com.example.dnd_quedamos.features.evento.data.remote.dto.EventoDto
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
private val eventosFalsos = listOf(
    EventoDto(1, "Partida en la biblioteca", "Sesión 0 de la nueva campaña. Traed fichas de personaje."),
    EventoDto(2, "One-shot de terror", "Aventura corta nivel 3. Plazas limitadas a 5 jugadores."),
    EventoDto(3, "Taller de pintura de minis", "Trae tus miniaturas, ponemos pinturas y pinceles."),
    EventoDto(4, "Campaña Curse of Strahd", "Sesión 12. Seguimos en el castillo."),
    EventoDto(5, "Noche de juegos de mesa", null), // sin descripción: prueba el mapper (null -> "")
    EventoDto(6, "Torneo de Magic", "Formato draft. Inscripción en la tienda."),
)

/** Simula el tiempo de respuesta de una red real (para ver estados de carga). */
private const val RETARDO_MS = 600L

/** Prefijo de todas las rutas: es la parte de ruta de BASE_URL ("/api/"). */
private const val API = "/api"

fun createFakeApiEngine(): HttpClientEngine = MockEngine { request ->
    delay(RETARDO_MS)

    val path = request.url.encodedPath   // p. ej. "/api/eventos/3"
    val method = request.method

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
private fun MockRequestHandleScope.respondJson(
    json: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = json,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)
