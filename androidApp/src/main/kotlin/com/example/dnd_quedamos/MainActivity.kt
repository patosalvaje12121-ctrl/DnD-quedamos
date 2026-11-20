package com.example.dnd_quedamos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.dnd_quedamos.notifications.EXTRA_TO_SCREEN
import com.example.dnd_quedamos.notifications.NotificationScheduler
import kotlinx.serialization.json.Json

private const val NOTIF_VUELVE = "vuelve"

class MainActivity : ComponentActivity() {

    // Pantalla a la que navegar. Es estado de Compose: al cambiar en onNewIntent, App se recompone y navega.
    private var toScreen by mutableStateOf<Route?>(null)

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

        // Pantalla pedida por la notificación si la app estaba cerrada (null = ninguna).
        // Solo si savedInstanceState == null: al girar la pantalla el Intent sigue siendo el mismo y no hay que volver a navegar.
        if (savedInstanceState == null) toScreen = jsonToRoute(intent)

        setContent {
            App(toScreen = toScreen)
        }
    }

    // Con launchMode="singleTop", si MainActivity ya está abierta Android la reutiliza
    // y entrega aquí el Intent de la notificación en vez de crear una Activity nueva.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        toScreen = jsonToRoute(intent)
    }

    private fun jsonToRoute(intent: Intent?): Route? =
        intent?.getStringExtra(EXTRA_TO_SCREEN)
            ?.let { runCatching { Json.decodeFromString<Route>(it) }.getOrNull() }

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
            applicationContext, NOTIF_VUELVE, "DnD Quedamos", "¡Vuelve a entrar!", 172800,
            toScreen = Route.MainMap, // prueba: al tocarla se abre el mapa
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