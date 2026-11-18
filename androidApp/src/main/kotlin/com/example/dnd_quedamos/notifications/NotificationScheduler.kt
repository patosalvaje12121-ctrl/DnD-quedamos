package com.example.dnd_quedamos.notifications

import android.content.Context
import com.example.dnd_quedamos.Route
import kotlinx.serialization.json.Json
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Punto de entrada para programar una notificación desde cualquier parte de androidApp.
 * Se llama NotificationScheduler (y no Notification) para no chocar con android.app.Notification.
 */
object NotificationScheduler {

    /**
     * Programa una notificación identificada por [uniqueName].
     * Si ya había una pendiente con ese nombre, se sustituye (REPLACE): así nunca se acumulan
     * varias iguales aunque se llame muchas veces seguidas.
     */
    fun scheduleNotification(
        context: Context,
        uniqueName: String,
        title: String,
        message: String,
        delaySeconds: Long,
        toScreen: Route = Route.Home,
    ) {
        val data = Data.Builder()
            .putString(KEY_TITLE, title)
            .putString(KEY_MESSAGE, message)
            // La pantalla destino viaja como JSON: Route es @Serializable
            .putString(KEY_ROUTE, Json.encodeToString<Route>(toScreen))
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, workRequest)
    }

    /** Anula la notificación pendiente con ese nombre (si ya se mostró o no existe, no hace nada). */
    fun cancelNotification(context: Context, uniqueName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName)
    }
}
