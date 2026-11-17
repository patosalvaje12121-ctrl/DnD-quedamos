package com.example.dnd_quedamos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.dnd_quedamos.notifications.NotificationScheduler

private const val NOTIF_VUELVE = "vuelve"

class MainActivity : ComponentActivity() {

    // Desde Android 13 el permiso de notificaciones se pide en tiempo de ejecución
    // (no basta con el Manifest). Se registra aquí, antes de que la Activity arranque.
    private val pedirPermisoNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            // Si se deniega no pasa nada: el Worker se ejecuta igual, pero Android no muestra la notificación
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        comprobarPermisoNotificaciones()
        setContent {
            App()
        }
    }

    override fun onStart() {
        super.onStart()
        // Si el usuario vuelve antes de que salte, ya no hace falta el "¡Vuelve!"
        NotificationScheduler.cancelNotification(applicationContext, NOTIF_VUELVE)
    }

    override fun onStop() {
        super.onStop()
        // Notificación de prueba: al salir de la app, llega a los 5 segundos.
        // Con nombre único: salir varias veces (o girar la pantalla) no acumula notificaciones.
        NotificationScheduler.scheduleNotification(
            applicationContext, NOTIF_VUELVE, "DnD Quedamos", "¡Vuelve a entrar!", 5
        )
    }

    /**
     * Muestra el diálogo del sistema si aún no se ha concedido el permiso.
     * Si el usuario lo deniega dos veces, Android deja de mostrar el diálogo:
     * habría que activarlo desde los Ajustes de la app.
     */
    private fun comprobarPermisoNotificaciones() {
        val concedido = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!concedido) {
            pedirPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}