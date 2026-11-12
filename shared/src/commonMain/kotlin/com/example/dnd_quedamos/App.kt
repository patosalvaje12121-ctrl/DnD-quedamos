package com.example.dnd_quedamos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import androidx.navigation.toRoute
import com.example.dnd_quedamos.features.chat.ui.ChatDirectoResolver
import com.example.dnd_quedamos.features.chat.ui.ChatEventoResolver
import com.example.dnd_quedamos.features.chat.ui.ChatListScreen
import com.example.dnd_quedamos.features.chat.ui.ChatScreen
import com.example.dnd_quedamos.features.evento.ui.HomeEventViewRoot
import com.example.dnd_quedamos.features.mapa.presentation.MapaViewModel
import com.example.dnd_quedamos.features.mapa.ui.VistaDePruebaMapa
import kotlinx.coroutines.selects.select

data class Objeto(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

// Lista fija de ejemplo, con pocos elementos
val objetosDeEjemplo = (1..30).map { i ->
    Objeto(id = i, nombre = "Objeto $i", descripcion = "Descripción de ejemplo $i")
}

@Composable
fun BottomNavButton(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(label, fontSize = 12.sp, color = color)
    }
}
@Composable
fun App() {
    val navController = rememberNavController();
    val destinoActual = navController.currentBackStackEntryAsState().value?.destination
    fun estaEn(vararg rutas: kotlin.reflect.KClass<out Route>) =
        rutas.any { destinoActual?.hasRoute(it) == true }

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }

    MaterialTheme(
        colorScheme = darkColorScheme() // DnD = dark fantasy vibe
    ) {
        Scaffold(
            modifier =Modifier
                .fillMaxWidth(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "DnD Quedamos",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            },

            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BottomNavButton(emoji = "🏠", label = "Inicio", selected = estaEn(Route.Home::class)) {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Home) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    BottomNavButton(emoji = "🧙", label = "Personajes", selected = false) { }
                    BottomNavButton(
                        emoji = "📜",
                        label = "Partidas",
                        selected = estaEn(Route.Chats::class, Route.Chat::class, Route.ChatEvento::class, Route.ChatWith::class),
                    ) {
                        navController.navigate(Route.Chats) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    BottomNavButton(emoji = "\uD83C\uDF0E", label = "Mapa", selected = estaEn(Route.MainMap::class)) {
                        navController.navigate(Route.MainMap) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    BottomNavButton(emoji = "⚙️", label = "Ajustes", selected = false) { }
                }
            }



        ) { innerPadding ->
            NavHost(navController = navController,
                startDestination = Route.Home,
                modifier = Modifier.padding(innerPadding)) {
                composable<Route.Home> {
                    HomeEventViewRoot(
                        onAbrirChat = { eventoId -> navController.navigate(Route.ChatEvento(eventoId)) }
                    )
                }
                composable<Route.MainMap> {
                    VistaDePruebaMapa()
                }

                // ---------- Chat ----------
                composable<Route.Chats> {
                    ChatListScreen(onAbrirChat = { chatId -> navController.navigate(Route.Chat(chatId)) })
                }
                composable<Route.Chat> { entry ->
                    val ruta = entry.toRoute<Route.Chat>()
                    ChatScreen(chatId = ruta.chatId, onBack = { navController.popBackStack() })
                }
                // Pantallas puente: resuelven el id del chat y se sustituyen por Route.Chat,
                // así "atrás" vuelve a donde estabas (Inicio, perfil...) y no a la pantalla de carga.
                composable<Route.ChatEvento> { entry ->
                    val ruta = entry.toRoute<Route.ChatEvento>()
                    ChatEventoResolver(
                        eventoId = ruta.eventoId,
                        onResuelto = { chatId ->
                            navController.navigate(Route.Chat(chatId)) {
                                popUpTo<Route.ChatEvento> { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<Route.ChatWith> { entry ->
                    val ruta = entry.toRoute<Route.ChatWith>()
                    ChatDirectoResolver(
                        userId = ruta.userID,
                        onResuelto = { chatId ->
                            navController.navigate(Route.Chat(chatId)) {
                                popUpTo<Route.ChatWith> { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
            }

        }
    }


}