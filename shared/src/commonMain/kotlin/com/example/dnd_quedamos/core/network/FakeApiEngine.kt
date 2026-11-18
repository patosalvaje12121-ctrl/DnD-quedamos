package com.example.dnd_quedamos.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
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
 * Cada parte de la API tiene su propio "servidor" en memoria:
 *  - FakeChatServer.kt   -> chats, mensajes, usuarios y eventos/{id}/chat
 *  - FakeEventoServer.kt -> eventos y participantes (y los datos de ejemplo, eventosFalsos)
 *
 * Para cambiar entre falso y real: USE_FAKE_API en di/AppModule.kt.
 */

/** Simula el tiempo de respuesta de una red real (para ver estados de carga). */
private const val RETARDO_MS = 600L

/** Prefijo de todas las rutas: es la parte de ruta de BASE_URL ("/api/"). */
internal const val API = "/api"

fun createFakeApiEngine(): HttpClientEngine = MockEngine { request ->
    delay(RETARDO_MS)

    // Cada servidor devuelve null si la petición no es suya. El chat va primero porque
    // también contesta una ruta bajo /eventos (eventos/{id}/chat).
    FakeChatServer.manejar(this, request)
        ?: FakeEventoServer.manejar(this, request)
        // Cualquier otra ruta: 404 (igual que un servidor real con una ruta que no existe)
        ?: respondError(HttpStatusCode.NotFound)
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
