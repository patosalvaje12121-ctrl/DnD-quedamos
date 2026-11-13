package com.example.dnd_quedamos.features.evento.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cuerpo de POST /eventos/{id}/participantes (apuntarse a un evento).
 *
 * JSON enviado:
 * { "usuarioId": 1 }
 *
 * No es parte del JSON de un evento (como UbicacionDto): es el cuerpo completo de otra petición.
 * Los participantes son un sub-recurso con su propia URL (/eventos/{id}/participantes): el id del
 * evento ya va en la URL, así que el cuerpo solo dice QUIÉN se apunta.
 *
 * ¿Por qué no hacer GET del evento, añadir al usuario a "participantes" y hacer PUT del evento entero?
 *  - Apuntarse a la vez: Ana y Luis hacen GET y ven [2, 3]. Ana envía [2, 3, 1] y Luis [2, 3, 4];
 *    el PUT de Luis machaca al de Ana y su inscripción se pierde. Con POST cada uno pide "añádeme"
 *    y el servidor añade a los dos sobre la lista que tiene en ese momento.
 *  - Permisos: el PUT del evento es para que el organizador lo edite. Si apuntarse usara PUT,
 *    cualquier participante podría cambiar también la fecha o el título. Con una ruta aparte el
 *    servidor puede dejar que cualquiera se apunte y que solo el organizador edite.
 *  - Comprobaciones en un solo sitio: "¿está completo?" y "¿ya estaba apuntado?" las decide el
 *    servidor al recibir el POST (responde 409), en vez de fiarse de la lista que mandó la app.
 *
 * Es el mismo patrón que EnviarMensajeDto en el chat: MensajeDto es lo que ES un mensaje,
 * EnviarMensajeDto es solo lo que hace falta para la acción de enviarlo.
 *
 * Nota: con login real, el servidor sabría quién eres por el token y este cuerpo podría
 * desaparecer. Mientras no haya autenticación, mandamos el usuarioId explícitamente.
 */
@Serializable
data class ParticipanteDto(
    @SerialName("usuarioId")
    val usuarioId: Int,
)
