@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/*
 * TODO: añadir un campo "estado" (enum EstadoEvento { ACTIVO, INACTIVO }).
 *  Ahora cancelar un evento lo BORRA del servidor: desaparece de la lista, su chat se queda
 *  huérfano y los apuntados no tienen forma de saber que se canceló. Con un estado, cancelar
 *  sería marcarlo como INACTIVO y el evento seguiría existiendo.
 *  Sitios que habría que cambiar:
 *   - EventoDto + EventoMapper: campo "estado" como texto; un valor desconocido -> ACTIVO.
 *   - EventoApi / RepositoryEvento: cancelar pasa de DELETE /eventos/{id} a una ruta propia
 *     (p. ej. POST /eventos/{id}/cancelar) o a un PUT con estado = INACTIVO.
 *   - FakeEventoServer: DELETE (o la nueva ruta) marca el evento como INACTIVO en vez de quitarlo,
 *     y algún evento de ejemplo debería empezar INACTIVO para poder probarlo.
 *   - ManagerEventoUseCase:
 *       · CancelarEvento -> cambia el estado en vez de borrar.
 *       · UnirseEvento y EditarEvento -> fallar si el evento está INACTIVO.
 *       · MostrarEvento -> decidir si se ocultan los INACTIVOS o se devuelven para mostrarlos
 *         como "Cancelado" (lo segundo es más claro para quien estaba apuntado).
 *   - UI: etiqueta "Cancelado" en la tarjeta y en el detalle, sin botón de apuntarse.
 *   - Mapa: no pintar chinchetas de eventos INACTIVOS.
 *   - Chat del evento: avisar o dejarlo en solo lectura cuando el evento está INACTIVO.
 *
 * TODO: añadir etiquetas al evento (val etiquetas: List<String>, p. ej. "Principiantes",
 *  "D&D 5e", "Aire libre", "+18").
 *  No sustituyen a tipoActividad: el tipo es UNA categoría fija (ROL, CARTAS...) y las etiquetas
 *  son varias y libres, para matizar. Habría que tocar:
 *   - EventoDto + EventoMapper: lista de textos; null -> lista vacía; limpiar espacios y repetidas.
 *   - FakeEventoServer: etiquetas en los datos de ejemplo.
 *   - CrearEvento / EditarEvento: límite de cuántas y de lo largas que pueden ser.
 *   - Búsqueda y filtros del feed y del mapa (filtrar por etiqueta).
 *   - Usuario: cruzar las etiquetas con los intereses del perfil para recomendar eventos.
 */
data class DataEvento (
    val id: Int,
    val nombre : String,
    val descripcion: String,
    val fecha: Instant,                 // momento exacto de inicio (la UI lo pasa a la hora local)
    val ubicacion: DataUbicacion,
    val tipoActividad: TipoActividad,
    val organizadorId: Int,
    val maxParticipantes: Int,
    val participantes: List<Int> = emptyList(), // ids de usuario; lo rellena el servidor
) {
    val plazasLibres: Int get() = (maxParticipantes - participantes.size).coerceAtLeast(0)
    val estaCompleto: Boolean get() = plazasLibres == 0
}
