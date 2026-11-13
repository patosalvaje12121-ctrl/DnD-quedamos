package com.example.dnd_quedamos.features.evento.domain.usecase


import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento
import kotlinx.coroutines.CancellationException

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

    /** Crea un evento nuevo. El id lo pone el servidor, así que el que traiga [evento] se ignora. */
    suspend fun CrearEvento(evento: DataEvento): Result<DataEvento> {
        val limpio = evento.limpio()
        validar(limpio)?.let { return Result.failure(it) }
        return runSafe { repositoryEvento.crearEvento(limpio) }
    }

    /**
     * Guarda los cambios de un evento existente.
     * TODO: comprobar que el usuario actual es el organizador cuando DataEvento tenga ese campo.
     */
    suspend fun EditarEvento(evento: DataEvento): Result<DataEvento> {
        val limpio = evento.limpio()
        validar(limpio)?.let { return Result.failure(it) }
        return runSafe { repositoryEvento.editarEvento(limpio) }
    }

    /**
     * Cancela (elimina) un evento.
     * TODO: comprobar que el usuario actual es el organizador cuando DataEvento tenga ese campo.
     */
    suspend fun CancelarEvento(eventoId: Int): Result<Unit> =
        runSafe { repositoryEvento.cancelarEvento(eventoId) }

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

    private fun DataEvento.limpio(): DataEvento =
        copy(nombre = nombre.trim(), descripcion = descripcion.trim())

    /** Devuelve el error de validación, o null si el evento es válido. */
    private fun validar(evento: DataEvento): Exception? =
        if (evento.nombre.isEmpty()) IllegalArgumentException("El evento necesita un nombre")
        else null

    // Igual que en ManagerChatUseCase: errores -> Result.failure, la cancelación se deja pasar
    private inline fun <T> runSafe(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
