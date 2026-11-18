@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.notifications

import android.content.Context
import com.example.dnd_quedamos.Route
import com.example.dnd_quedamos.core.notificaciones.RecordatorioEventos
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/** Cuánto antes del evento se avisa. */
private val ANTELACION_AVISO = 2.hours

/**
 * Recordatorios de eventos en Android: una notificación [ANTELACION_AVISO] antes de que empiece.
 * Al tocarla se abre el detalle del evento.
 */
class RecordatorioEventosAndroid(private val context: Context) : RecordatorioEventos {

    override fun programar(evento: DataEvento) {
        val momentoAviso = evento.fecha - ANTELACION_AVISO
        val segundosHastaAviso = (momentoAviso - Clock.System.now()).inWholeSeconds

        // Ya ha pasado el momento del aviso (falta menos de ANTELACION_AVISO o el evento ya empezó):
        // no se avisa. Se cancela por si quedaba uno antiguo con otra fecha (p. ej. tras editarlo).
        if (segundosHastaAviso <= 0) {
            cancelar(evento.id)
            return
        }

        NotificationScheduler.scheduleNotification(
            context = context,
            uniqueName = nombreUnico(evento.id),
            title = "¡Tu quedada empieza en 2 horas!",
            message = evento.nombre,
            delaySeconds = segundosHastaAviso,
            toScreen = Route.Home,
        )
    }

    override fun cancelar(eventoId: Int) {
        NotificationScheduler.cancelNotification(context, nombreUnico(eventoId))
    }

    // Un nombre fijo por evento: programar otra vez el mismo evento sustituye su aviso (REPLACE)
    private fun nombreUnico(eventoId: Int) = "recordatorio_evento_$eventoId"
}

/** Sustituye a RecordatorioEventosVacio de appModules. Se carga en DndApplication. */
val notificacionesAndroidModule = module {
    single<RecordatorioEventos> { RecordatorioEventosAndroid(androidContext()) }
}
