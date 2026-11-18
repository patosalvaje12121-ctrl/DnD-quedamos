package com.example.dnd_quedamos.core.notificaciones

import com.example.dnd_quedamos.features.evento.domain.model.DataEvento

/**
 * Avisa al usuario un rato antes de los eventos a los que está apuntado.
 *
 * El código común solo conoce esta interfaz; cada plataforma registra su implementación en Koin:
 *  - Android: RecordatorioEventosAndroid (androidApp), que programa una notificación con WorkManager.
 *  - Escritorio y web: RecordatorioEventosVacio, que no hace nada.
 */
interface RecordatorioEventos {

    /**
     * Programa (o reprograma, si ya existía) el aviso de [evento].
     * Llamarlo varias veces con el mismo evento no duplica avisos: se sustituye el anterior.
     */
    fun programar(evento: DataEvento)

    /** Anula el aviso pendiente del evento (si no había ninguno, no hace nada). */
    fun cancelar(eventoId: Int)
}

/** Implementación por defecto para plataformas sin notificaciones (escritorio y web). */
class RecordatorioEventosVacio : RecordatorioEventos {
    override fun programar(evento: DataEvento) {}
    override fun cancelar(eventoId: Int) {}
}
