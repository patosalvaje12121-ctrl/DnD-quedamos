@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.domain.usecase


import com.example.dnd_quedamos.core.notificaciones.RecordatorioEventos
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento
import com.example.dnd_quedamos.features.usuario.domain.usecase.UserPermissionsUseCase
import kotlinx.coroutines.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ManagerEventoUseCase(
    private val repositoryEvento: IRepositoryEvento,
    private val currentUserProvider: CurrentUserProvider,
    private val recordatorio: RecordatorioEventos, // avisos antes del evento (solo hacen algo en Android)
    private val permisos: UserPermissionsUseCase,  // para saber si el usuario actual es administrador
) {

    /**
     * Lista de todos los eventos.
     * Que no haya eventos NO es un error: se devuelve success con la lista vacía. Así el ViewModel
     * distingue "no hay quedadas" (success vacío) de un fallo real como "sin conexión" (failure).
     *
     * TODO: EventoViewModel y HomeEventViewRoot todavía no aprovechan esta diferencia:
     *  - success con lista vacía -> mostrar un mensaje tipo "No hay quedadas" (ahora sale la
     *    pantalla en blanco).
     *  - failure -> mostrar un mensaje de error con botón de reintentar (ahora el onFailure del
     *    ViewModel está vacío y el error se ignora).
     *  Para eso EventoUiState necesita campos como cargando y error.
     */
    suspend fun MostrarEvento(): Result<List<DataEvento>> = runSafe {
        repositoryEvento.bbddEvento().also { sincronizarRecordatorios(it) }
    }

    /** Un evento concreto, con sus participantes (para la pantalla de detalle). */
    suspend fun ObtenerEvento(eventoId: Int): Result<DataEvento> =
        runSafe { repositoryEvento.obtenerEvento(eventoId) }

    /**
     * "Ver quedadas propias": eventos que organiza el usuario actual, del más próximo al más lejano.
     * Lista vacía = success (no organiza ninguno), igual que MostrarEvento.
     */
    suspend fun MisEventos(): Result<List<DataEvento>> = runSafe {
        val yo = usuarioActual()
        repositoryEvento.bbddEvento()
            .filter { it.organizadorId == yo }
            .sortedBy { it.fecha }
    }

    /**
     * "Ver quedadas a las que pertenezco": eventos a los que el usuario actual está apuntado,
     * del más próximo al más lejano.
     * No incluye los que organiza él (aunque el servidor también lo apunta a esos), para que no se
     * repitan con MisEventos. Si se prefiere que salgan en las dos listas, basta con quitar ese filtro.
     */
    suspend fun EventosApuntado(): Result<List<DataEvento>> = runSafe {
        val yo = usuarioActual()
        repositoryEvento.bbddEvento()
            .filter { yo in it.participantes && it.organizadorId != yo }
            .sortedBy { it.fecha }
    }
    // TODO: MisEventos y EventosApuntado descargan TODOS los eventos y filtran en la app. Vale con
    //  pocos eventos, pero con un servidor real conviene pedir solo los necesarios, p. ej.
    //  GET /eventos?organizadorId={id} y GET /eventos?participanteId={id} (o /usuarios/{id}/eventos).
    //  Habría que añadir esas rutas a EventoApi, IRepositoryEvento, RepositoryEvento y al
    //  FakeEventoServer; estos dos use cases solo cambiarían la llamada al repositorio.

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
            // El servidor apunta al organizador al crearlo, así que también le toca aviso
            repositoryEvento.crearEvento(nuevo).also { recordatorio.programar(it) }
        }
    }

    /**
     * Guarda los cambios de un evento existente. Solo puede hacerlo su organizador o un administrador
     * (el organizador no cambia aunque edite un administrador: lo conserva el servidor).
     * Pasa la misma validación que al crear, así que un evento que ya ha pasado no se puede editar
     * (su fecha está en el pasado) y no se le puede poner una fecha pasada a uno futuro.
     */
    suspend fun EditarEvento(evento: DataEvento): Result<DataEvento> {
        val limpio = evento.limpio()
        validar(limpio)?.let { return Result.failure(it) }
        return runSafe {
            comprobarPuedeGestionar(limpio.id)
            // Reprograma el aviso por si ha cambiado la fecha (sustituye al anterior).
            // Los demás apuntados lo actualizan en su próxima sincronización (MostrarEvento).
            repositoryEvento.editarEvento(limpio).also { recordatorio.programar(it) }
        }
    }

    /** Cancela (elimina) un evento. Solo puede hacerlo su organizador o un administrador. */
    suspend fun CancelarEvento(eventoId: Int): Result<Unit> = runSafe {
        comprobarPuedeGestionar(eventoId)
        repositoryEvento.cancelarEvento(eventoId)
        recordatorio.cancelar(eventoId)
    }

    /**
     * Si el usuario actual puede editar y cancelar el evento (es su organizador o un administrador).
     * Pensado para que la UI decida si enseña esos botones.
     */
    suspend fun PuedeGestionar(eventoId: Int): Result<Boolean> =
        runSafe { puedeGestionar(eventoId) }

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
        // Hace falta el evento completo para saber la fecha y el nombre del aviso
        recordatorio.programar(repositoryEvento.obtenerEvento(eventoId))
    }

    /** Desapunta al usuario actual del evento. */
    suspend fun AbandonarEvento(eventoId: Int): Result<Unit> = runSafe {
        val yo = usuarioActual()
        if (yo !in repositoryEvento.participantesEvento(eventoId)) {
            throw IllegalStateException("No estás apuntado a este evento")
        }
        repositoryEvento.abandonarEvento(eventoId, yo)
        recordatorio.cancelar(eventoId)
    }

    // ---------- Ayudantes ----------

    /**
     * Programa el aviso de todos los eventos a los que está apuntado el usuario actual.
     * Se llama cada vez que se descarga la lista (al abrir la app y al refrescar), así los avisos
     * se recuperan tras reinstalar y se actualizan si otro (el organizador) cambia la fecha.
     * Repetirlo no duplica avisos: cada evento sustituye el suyo.
     *
     * TODO: Recibe toda la lista de eventos y comprueba los que estas tu apuntado, tremendamente ineficiente.
     *  Ademas no cancela avisos de eventos que ya no están en la lista (p. ej. si el organizador lo
     *  cancela desde otro dispositivo). Haría falta guardar qué eventos tienen aviso programado, o
     *  que el servidor avise (cuando el evento tenga estado INACTIVO, ver DataEvento).
     *
     *
     *
     */
    private suspend fun sincronizarRecordatorios(eventos: List<DataEvento>) {
        val yo = currentUserProvider.currentUser()?.id ?: return
        eventos.filter { yo in it.participantes }.forEach { recordatorio.programar(it) }
    }

    private suspend fun usuarioActual(): Int =
        currentUserProvider.currentUser()?.id
            ?: throw IllegalStateException("No hay ningún usuario con la sesión iniciada")

    /**
     * true si el usuario actual es el organizador del evento o un administrador.
     *
     * El organizador se pide al servidor en vez de leerlo del DataEvento que llega a EditarEvento:
     * ese objeto viene de la UI y podría estar desactualizado o tener otro organizadorId puesto.
     *
     * Se mira primero el organizador para no pedir los permisos en el caso normal. Si la consulta
     * de permisos falla (p. ej. sin conexión) se trata como "no es admin": nunca se dan permisos
     * por un error.
     *
     * Esto solo sirve para que la app no deje intentarlo y muestre un error claro. La protección
     * de verdad tiene que estar en el servidor (FakeEventoServer aplica la misma regla), porque
     * cualquiera podría llamar a la API sin pasar por la app.
     *
     * TODO: avisar al organizador cuando un administrador modifique o cancele su evento (necesita
     *  notificaciones push desde el servidor).
     */
    private suspend fun puedeGestionar(eventoId: Int): Boolean {
        val yo = usuarioActual()
        if (repositoryEvento.obtenerEvento(eventoId).organizadorId == yo) return true
        return try {
            permisos.esAdmin()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    /** Lanza una excepción si el usuario actual no puede editar ni cancelar el evento. */
    private suspend fun comprobarPuedeGestionar(eventoId: Int) {
        if (!puedeGestionar(eventoId)) {
            throw IllegalStateException(
                "Solo el organizador o un administrador puede modificar o cancelar este evento"
            )
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
