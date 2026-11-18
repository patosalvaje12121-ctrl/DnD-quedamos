package com.example.dnd_quedamos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dnd_quedamos.MainActivity

// Claves de los datos que NotificationScheduler le pasa al Worker
const val KEY_TITLE = "title"
const val KEY_MESSAGE = "message"
const val KEY_ROUTE = "route"

// Clave del extra del Intent con la Route (en JSON) que MainActivity debe abrir
const val EXTRA_TO_SCREEN = "toScreen"

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
        val toScreenJson = inputData.getString(KEY_ROUTE)

        showNotification(title, message, toScreenJson)

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

    private fun showNotification(title: String, message: String, toScreenJson: String?) {
        val channelId = "CANAL_PRUEBA"
        createNotificationChannel(channelId, "Pruebas")

        val notificationId = System.currentTimeMillis().toInt()

        // Intent explícito: abre MainActivity y le pasa la pantalla destino.
        // NEW_TASK porque se lanza desde fuera de una Activity; CLEAR_TOP + singleTop (Manifest)
        // reutiliza la MainActivity abierta (llega por onNewIntent) o crea una nueva (llega por onCreate).
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TO_SCREEN, toScreenJson)
        }

        // PendingIntent: permiso para que Android ejecute ese Intent más tarde en nombre de la app
        // (cuando el usuario toque la notificación, aunque la app esté cerrada)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // obligatorio
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent) // qué pasa al tocarla
            .setAutoCancel(true) // se quita al tocarla
            .build()

        applicationContext
            .getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }
}
