@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Ayudantes que usan varias pantallas de eventos (detalle, gestión y formulario)

private fun dos(n: Int) = n.toString().padStart(2, '0') // 5 -> "05"

/** Instant -> "dd/MM/yyyy" en hora local. */
fun textoFecha(fecha: Instant): String {
    val t = fecha.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dos(t.day)}/${dos(t.month.number)}/${t.year}"
}

/** Instant -> "HH:mm" en hora local. */
fun textoHora(fecha: Instant): String {
    val t = fecha.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dos(t.hour)}:${dos(t.minute)}"
}

/** Instant -> "dd/MM/yyyy HH:mm" en hora local. */
fun formatearFecha(fecha: Instant): String = "${textoFecha(fecha)} ${textoHora(fecha)}"

/**
 * Comprueba la fecha y la hora escritas en el formulario.
 * Devuelve el mensaje del PRIMER error que encuentra, o null si están bien.
 * Cada fallo tiene su propio mensaje, para que se note cuál hay que arreglar.
 */
fun errorFechaHora(fecha: String, hora: String): String? {
    val partesFecha = fecha.trim().split("/").map { it.toIntOrNull() }
    val partesHora = hora.trim().split(":").map { it.toIntOrNull() }

    if (partesFecha.size != 3 || null in partesFecha) return "La fecha tiene que ser dd/MM/yyyy (p. ej. 25/12/2026)"
    if (partesHora.size != 2 || null in partesHora) return "La hora tiene que ser HH:mm (p. ej. 18:30)"

    val (dia, mes, _) = partesFecha
    val (h, m) = partesHora
    if (mes!! !in 1..12) return "El mes tiene que estar entre 1 y 12"
    if (dia!! !in 1..31) return "El día tiene que estar entre 1 y 31"
    if (h!! !in 0..23) return "La hora tiene que estar entre 0 y 23"
    if (m!! !in 0..59) return "Los minutos tienen que estar entre 0 y 59"
    if (leerFecha(fecha, hora) == null) return "Ese día no existe en ese mes" // p. ej. 31/02

    return null
}

/** "dd/MM/yyyy" + "HH:mm" -> Instant. Devuelve null si el texto no es una fecha válida. */
fun leerFecha(fecha: String, hora: String): Instant? = runCatching {
    val (dia, mes, anio) = fecha.trim().split("/").map { it.toInt() }
    val (h, m) = hora.trim().split(":").map { it.toInt() }
    LocalDateTime(anio, mes, dia, h, m).toInstant(TimeZone.currentSystemDefault())
}.getOrNull()

/** Diálogo "¿Seguro?" antes de borrar un evento. */
@Composable
fun ConfirmarBorrado(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿Borrar el evento?") },
        text = { Text("No se puede deshacer.") },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("Borrar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}
