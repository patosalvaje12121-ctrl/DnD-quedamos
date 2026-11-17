package com.example.dnd_quedamos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// Claves de los datos que NotificationScheduler le pasa al Worker
const val KEY_TITLE = "title"
const val KEY_MESSAGE = "message"

/**
 * Trabajo que WorkManager ejecuta cuando se cumple el retraso programado.
 * Lee el título y el mensaje, y construye y lanza la notificación.
 */
class NotificationWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Sin Titulo"
        val message = inputData.getString(KEY_MESSAGE) ?: "Sin Mensaje"

        showNotification(title, message)

        return Result.success()
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        val channel = NotificationChannel(
            channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        applicationContext
            .getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "CANAL_PRUEBA"
        createNotificationChannel(channelId, "Pruebas")

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // obligatorio
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true) // se quita al tocarla
            .build()

        applicationContext
            .getSystemService(NotificationManager::class.java)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
