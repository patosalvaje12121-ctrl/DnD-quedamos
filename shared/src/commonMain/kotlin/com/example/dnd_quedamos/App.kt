package com.example.dnd_quedamos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import com.example.dnd_quedamos.core.ui.theme.DndTheme
import com.example.dnd_quedamos.features.chat.ui.ChatDirectoResolver
import com.example.dnd_quedamos.features.chat.ui.ChatEventoResolver
import com.example.dnd_quedamos.features.chat.ui.ChatListScreen
import com.example.dnd_quedamos.features.chat.ui.ChatScreen
import com.example.dnd_quedamos.features.chat.ui.ChatScreenRoot
import com.example.dnd_quedamos.features.chat.ui.NuevoChatScreen
import com.example.dnd_quedamos.features.evento.ui.HomeEventViewRoot
import com.example.dnd_quedamos.features.evento.ui.PantallaFormularioEvento
import com.example.dnd_quedamos.features.evento.ui.PantallaGestion
import com.example.dnd_quedamos.features.evento.ui.PantallaViewEvent
import com.example.dnd_quedamos.features.mapa.ui.VistaMainMapa
import com.example.dnd_quedamos.features.usuario.ui.ViewUserRoot

@Composable
fun App(
    toScreen: Route? = null
) {
    val navController = rememberNavController();
    val destinoActual = navController.currentBackStackEntryAsState().value?.destination
//    fun estaEn(vararg rutas: kotlin.reflect.KClass<out Route>) =
//        rutas.any { destinoActual?.hasRoute(it) == true }

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }


    DndTheme {
        Scaffold(
            modifier =Modifier
                .fillMaxWidth(),
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    title = {
                        Text(
                            "DnD Quedamos",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            },

            bottomBar = {
                NavBarMenu(navController);
            }

        ) {
            innerPadding ->

            // Navegar a una pantalla cuando toScreen vale algo, por ejemplo con una notificacion
            LaunchedEffect(toScreen) {
                toScreen?.let { navController.navigate(it) { launchSingleTop = true } }
            }


            NavHost(navController = navController,
                startDestination = Route.Home,
                modifier = Modifier.padding(innerPadding)
                    //.background(darkColor)
            ) {
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
                composable<Route.ChatList> {
                    ChatScreenRoot(
                        onBack = { navController.popBackStack() },
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