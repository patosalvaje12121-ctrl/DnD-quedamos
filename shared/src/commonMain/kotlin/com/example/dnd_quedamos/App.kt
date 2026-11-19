package com.example.dnd_quedamos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
import coil3.compose.ImagePainter
import com.example.dnd_quedamos.features.chat.ui.ChatDirectoResolver
import com.example.dnd_quedamos.features.chat.ui.ChatEventoResolver
import com.example.dnd_quedamos.features.chat.ui.ChatListScreen
import com.example.dnd_quedamos.features.chat.ui.NuevoChatScreen
import com.example.dnd_quedamos.features.chat.ui.ChatScreen
import com.example.dnd_quedamos.features.usuario.ui.ViewUserRoot
import com.example.dnd_quedamos.features.evento.ui.HomeEventViewRoot
import com.example.dnd_quedamos.features.evento.ui.PantallaFormularioEvento
import com.example.dnd_quedamos.features.evento.ui.PantallaGestion
import com.example.dnd_quedamos.features.evento.ui.PantallaViewEvent
import com.example.dnd_quedamos.features.mapa.presentation.MapaViewModel
import com.example.dnd_quedamos.features.mapa.ui.VistaDePruebaMapa
import com.example.dnd_quedamos.features.mapa.ui.VistaMainMapa
import com.example.dnd_quedamos.resources.NVAEvents
import com.example.dnd_quedamos.resources.NVMain
import com.example.dnd_quedamos.resources.NVMap
import com.example.dnd_quedamos.resources.NVMessages
import com.example.dnd_quedamos.resources.NVProfile
import com.example.dnd_quedamos.resources.Res
import kotlinx.coroutines.selects.select
import org.jetbrains.compose.resources.painterResource

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
    scope: RowScope,
    image: Painter,
    label: String,
    onClick: () -> Unit
) {
    scope.NavigationBarItem(
        false,
        onClick = onClick,
        icon = {
            Icon(
                image,
                contentDescription = label,
                tint = Color.Unspecified,
            )
        },
        modifier = Modifier,
        enabled = true,
        alwaysShowLabel = false,
    )
}

@Composable
fun App(
    toScreen: Route? = null
) {
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
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(8.dp),

                ) {
                    BottomNavButton(scope = this, image = painterResource(Res.drawable.NVAEvents), label = "AddEvento") { }
                    BottomNavButton(scope = this,
                        image = painterResource(Res.drawable.NVMessages),
                        label = "Partidas",
                        //ass),
                    ) {
                        navController.navigate(Route.Chats) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    BottomNavButton(scope = this, image = painterResource(Res.drawable.NVMain), label = "Inicio") {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Home) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    BottomNavButton(scope = this, image = painterResource(Res.drawable.NVMap), label = "Mapa") {
                        navController.navigate(Route.MainMap) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    BottomNavButton(scope = this, image = painterResource(Res.drawable.NVProfile), label = "Personajes") { }
                }
            }



        ) {
            innerPadding ->

            // Navegar a una pantalla cuando toScreen vale algo, por ejemplo con una notificacion
            LaunchedEffect(toScreen) {
                toScreen?.let { navController.navigate(it) { launchSingleTop = true } }
            }


            NavHost(navController = navController,
                startDestination = Route.Home,
                modifier = Modifier.padding(innerPadding)) {
                composable<Route.Home> {
                    HomeEventViewRoot(
                        onVerMas = { eventoId -> navController.navigate(Route.InspectEvent(eventoId)) }
                    )
                }
                composable<Route.InspectEvent> { entry ->
                    val ruta = entry.toRoute<Route.InspectEvent>()
                    PantallaViewEvent(
                        eventoId = ruta.eventID,
                        onBack = { navController.popBackStack() },
                        onAbrirChat = { eventoId -> navController.navigate(Route.ChatEvento(eventoId)) },
                        onEditar = { eventoId -> navController.navigate(Route.EditEvent(eventoId)) },
                    )
                }

                // ---------- Gestión de eventos ----------
                composable<Route.Gestion> {
                    PantallaGestion(
                        onCrear = { navController.navigate(Route.CreateEvent) },
                        onEditar = { eventoId -> navController.navigate(Route.EditEvent(eventoId)) },
                    )
                }
                composable<Route.CreateEvent> {
                    PantallaFormularioEvento(
                        eventoId = -1, // -1 = crear uno nuevo
                        onBack = { navController.popBackStack() },
                        onGuardado = { navController.popBackStack() },
                    )
                }
                composable<Route.EditEvent> { entry ->
                    val ruta = entry.toRoute<Route.EditEvent>()
                    PantallaFormularioEvento(
                        eventoId = ruta.eventoId,
                        onBack = { navController.popBackStack() },
                        onGuardado = { navController.popBackStack() },
                    )
                }
                composable<Route.MainMap> {
                    VistaMainMapa()
                }

                // ---------- Usuario ----------
                composable<Route.UserProfile> { entry ->
                    val ruta = entry.toRoute<Route.UserProfile>()
                    ViewUserRoot(iD = ruta.userID)
                }

                // ---------- Chat ----------
                composable<Route.Chats> {
                    ChatListScreen(
                        onAbrirChat = { chatId -> navController.navigate(Route.Chat(chatId)) },
                        onNuevoChat = { navController.navigate(Route.NuevoChat) },
                    )
                }
                composable<Route.NuevoChat> {
                    NuevoChatScreen(
                        onElegir = { userId ->
                            // Se quita el selector de la pila: "atrás" desde el chat vuelve a la lista
                            navController.navigate(Route.ChatWith(userId)) {
                                popUpTo<Route.NuevoChat> { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
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