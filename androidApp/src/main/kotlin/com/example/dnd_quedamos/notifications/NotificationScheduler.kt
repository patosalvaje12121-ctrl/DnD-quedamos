package com.example.dnd_quedamos.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Punto de entrada para programar una notificación desde cualquier parte de androidApp.
 * Se llama NotificationScheduler (y no Notification) para no chocar con android.app.Notification.
 */
object NotificationScheduler {

    fun scheduleNotification(
        context: Context,
        title: String,
        message: String,
        delaySeconds: Long,
    ) {
        val data = Data.Builder()
            .putString(KEY_TITLE, title)
            .putString(KEY_MESSAGE, message)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
