package com.example.dnd_quedamos.features.chat.ui

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Epoch millis -> "HH:mm" en la zona horaria del dispositivo. */
@OptIn(ExperimentalTime::class)
internal fun formatearHora(epochMillis: Long): String {
    val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
}
