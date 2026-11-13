package com.example.dnd_quedamos.features.evento.data.remote

import com.example.dnd_quedamos.features.evento.data.remote.dto.EventoDto
import com.example.dnd_quedamos.features.evento.data.remote.dto.ParticipanteDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Endpoints de la API relacionados con eventos.
 *
 * - Solo describe las peticiones HTTP: ruta, método y cuerpo.
 * - Trabaja con DTOs (nunca con DataEvento): la conversión la hace el repositorio con los mappers.
 * - No captura errores: con expectSuccess = true (HttpClientFactory) cualquier 4xx/5xx o fallo
 *   de red llega como excepción y la gestiona la capa de arriba (use case -> Result).
 *
 * Las rutas son relativas a BASE_URL: "eventos" -> https://tu-api.com/api/eventos
 */
class EventoApi(
    private val client: HttpClient,
) {

    /** GET /eventos -> lista de todos los eventos */
    suspend fun getEventos(): List<EventoDto> =
        client.get("eventos").body()

    /** GET /eventos/{id} -> un evento concreto */
    suspend fun getEvento(id: Int): EventoDto =
        client.get("eventos/$id").body()

    /** POST /eventos -> crea un evento y devuelve el creado (normalmente ya con su id del servidor) */
    suspend fun crearEvento(evento: EventoDto): EventoDto =
        client.post("eventos") {
            contentType(ContentType.Application.Json)
            setBody(evento)
        }.body()

    /** PUT /eventos/{id} -> reemplaza un evento existente */
    suspend fun actualizarEvento(evento: EventoDto): EventoDto =
        client.put("eventos/${evento.id}") {
            contentType(ContentType.Application.Json)
            setBody(evento)
        }.body()

    /** DELETE /eventos/{id} -> borra un evento (sin cuerpo de respuesta) */
    suspend fun borrarEvento(id: Int) {
        client.delete("eventos/$id")
    }

    /** GET /eventos/{id}/participantes -> ids de los usuarios apuntados */
    suspend fun getParticipantes(eventoId: Int): List<Int> =
        client.get("eventos/$eventoId/participantes").body()

    /** POST /eventos/{id}/participantes -> apunta a un usuario al evento */
    suspend fun unirseEvento(eventoId: Int, usuarioId: Int) {
        client.post("eventos/$eventoId/participantes") {
            contentType(ContentType.Application.Json)
            setBody(ParticipanteDto(usuarioId))
        }
    }

    /** DELETE /eventos/{id}/participantes/{usuarioId} -> desapunta a un usuario del evento */
    suspend fun abandonarEvento(eventoId: Int, usuarioId: Int) {
        client.delete("eventos/$eventoId/participantes/$usuarioId")
    }
}
