@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.domain.usecase


import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ManagerEventoUseCase(
    private val repositoryEvento: IRepositoryEvento,
    private val currentUserProvider: CurrentUserProvider,
) {

    /**
     * Lista de todos los eventos.
     * TODO: devolver la lista vacía como éxito en vez de NoSuchElementException. Ahora el ViewModel
     *  recibe un error cuando simplemente no hay eventos y no puede distinguir "no hay quedadas" de
     *  "sin conexión". Al cambiarlo, la UI debe mostrar un mensaje tipo "No hay quedadas".
     */
    suspend fun MostrarEvento(): Result<List<DataEvento>> = runSafe {
        val de = repositoryEvento.bbddEvento()
        if (de.isEmpty()) throw NoSuchElementException()
        de
    }

    /** Un evento concreto, con sus participantes (para la pantalla de detalle). */
    suspend fun ObtenerEvento(eventoId: Int): Result<DataEvento> =
        runSafe { repositoryEvento.obtenerEvento(eventoId) }

    /**
     * Crea un evento nuevo.
     * - El id lo pone el servidor, así que el que traiga [evento] se ignora.
     * - El organizador es SIEMPRE el usuario actual: se sobrescribe lo que venga de la UI para que
     *   nadie pueda crear eventos en nombre de otro.
     * - Los participantes los gestiona el servidor (apunta al organizador al crearlo).
     */
    suspend fun CrearEvento(evento: DataEvento): Result<DataEvento> {
        val limpio = evento.limpio()
        validar(limpio)?.let { return Result.failure(it) }
        return runSafe {
            val nuevo = limpio.copy(organizadorId = usuarioActual(), participantes = emptyList())
            repositoryEvento.crearEvento(nuevo)
        }
    }

    /**
     * Guarda los cambios de un evento existente. Solo puede hacerlo su organizador.
     * Pasa la misma validación que al crear, así que un evento que ya ha pasado no se puede editar
     * (su fecha está en el pasado) y no se le puede poner una fecha pasada a uno futuro.
     */
    suspend fun EditarEvento(evento: DataEvento): Result<DataEvento> {
        val limpio = evento.limpio()
        validar(limpio)?.let { return Result.failure(it) }
        return runSafe {
            comprobarOrganizador(limpio.id)
            repositoryEvento.editarEvento(limpio)
        }
    }

    /** Cancela (elimina) un evento. Solo puede hacerlo su organizador. */
    suspend fun CancelarEvento(eventoId: Int): Result<Unit> = runSafe {
        comprobarOrganizador(eventoId)
        repositoryEvento.cancelarEvento(eventoId)
    }

    /** Ids de los usuarios apuntados al evento. */
    suspend fun ListarParticipantes(eventoId: Int): Result<List<Int>> =
        runSafe { repositoryEvento.participantesEvento(eventoId) }

    /**
     * Apunta al usuario actual al evento.
     * TODO: comprobar plazas libres cuando DataEvento tenga el máximo de participantes.
     */
    suspend fun UnirseEvento(eventoId: Int): Result<Unit> = runSafe {
        val yo = usuarioActual()
        if (yo in repositoryEvento.participantesEvento(eventoId)) {
            throw IllegalStateException("Ya estás apuntado a este evento")
        }
        repositoryEvento.unirseEvento(eventoId, yo)
    }

    /** Desapunta al usuario actual del evento. */
    suspend fun AbandonarEvento(eventoId: Int): Result<Unit> = runSafe {
        val yo = usuarioActual()
        if (yo !in repositoryEvento.participantesEvento(eventoId)) {
            throw IllegalStateException("No estás apuntado a este evento")
        }
        repositoryEvento.abandonarEvento(eventoId, yo)
    }

    // ---------- Ayudantes ----------

    private suspend fun usuarioActual(): Int =
        currentUserProvider.currentUser()?.id
            ?: throw IllegalStateException("No hay ningún usuario con la sesión iniciada")

    /**
     * Lanza una excepción si el usuario actual no es el organizador del evento.
     *
     * El organizador se pide al servidor en vez de leerlo del DataEvento que llega a EditarEvento:
     * ese objeto viene de la UI y podría estar desactualizado o tener otro organizadorId puesto.
     *
     * Esto solo sirve para que la app no deje intentarlo y muestre un error claro. La protección
     * de verdad tiene que estar en el servidor real (con login sabrá quién hace la petición),
     * porque cualquiera podría llamar a la API sin pasar por la app.
     *
     * TODO: un usuario con rol MODERADOR (el "Administrador" del documento de diseño) debería poder
     *  editar y cancelar cualquier evento, igual que su organizador. Habría que cambiar:
     *   - DataUsuario: añadir el rol (p. ej. enum RolUsuario { USUARIO, MODERADOR }), y lo mismo en
     *     su DTO y mapper (un rol desconocido -> USUARIO, nunca dar más permisos por error).
     *   - CurrentUserProvider: devolver el usuario con su rol (ahora solo tiene id y nombre).
     *   - Esta función: dejar pasar si yo == organizadorId O si soy MODERADOR; y renombrarla a algo
     *     como comprobarPuedeGestionar, porque ya no comprobará solo al organizador.
     *   - Cuando un moderador edita, el organizadorId NO debe cambiar a él (el PUT del servidor
     *     falso ya conserva el organizador original).
     *   - Servidor (falso y real): aplicar la misma regla, y quizá avisar al organizador de que un
     *     moderador ha modificado o cancelado su evento.
     *   - UI: mostrar los botones de editar/cancelar también a los moderadores.
     *   - Datos falsos: algún usuario de ejemplo con rol MODERADOR para poder probarlo.
     */
    private suspend fun comprobarOrganizador(eventoId: Int) {
        val yo = usuarioActual()
        if (repositoryEvento.obtenerEvento(eventoId).organizadorId != yo) {
            throw IllegalStateException("Solo el organizador puede modificar o cancelar este evento")
        }
    }

    private fun DataEvento.limpio(): DataEvento =
        copy(nombre = nombre.trim(), descripcion = descripcion.trim())

    /**
     * Devuelve el error de validación, o null si el evento es válido.
     * Solo comprueba los datos en sí (no hace falta red), por eso va antes de runSafe.
     * Los mensajes están pensados para enseñarlos tal cual en el formulario.
     */
    private fun validar(evento: DataEvento): Exception? {
        val u = evento.ubicacion
        val error = when {
            evento.nombre.isEmpty() -> "El evento necesita un nombre"
            evento.fecha <= Clock.System.now() -> "La fecha del evento tiene que ser futura"
            evento.maxParticipantes <= 0 -> "Tiene que haber al menos una plaza"
            u.latitud !in -90.0..90.0 || u.longitud !in -180.0..180.0 -> "La ubicación no es válida"
            else -> return null
        }
        return IllegalArgumentException(error)
    }

    // Igual que en ManagerChatUseCase: errores -> Result.failure, la cancelación se deja pasar
    private inline fun <T> runSafe(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
