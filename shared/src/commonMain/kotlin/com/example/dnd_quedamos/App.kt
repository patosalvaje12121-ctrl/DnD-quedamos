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
import androidx.navigation.toRoute
import com.example.dnd_quedamos.di.AppContainer
import com.example.dnd_quedamos.di.DefaultAppContainer
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import com.example.dnd_quedamos.features.evento.ui.HomeEventViewRoot
import com.example.dnd_quedamos.features.mapa.presentation.MapaViewModel
import com.example.dnd_quedamos.features.mapa.ui.VistaDePruebaMapa
import com.example.dnd_quedamos.features.usuario.ui.ViewUserRoot
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
fun App( appContainer: AppContainer = DefaultAppContainer() ) {
    val navController = rememberNavController();

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
                    BottomNavButton(emoji = "🏠", label = "Inicio", selected = true) {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Home) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    BottomNavButton(emoji = "🧙", label = "Personajes", selected = false) { }
                    BottomNavButton(emoji = "📜", label = "Partidas", selected = false) { }
                    BottomNavButton(emoji = "\uD83C\uDF0E", label = "Mapa", selected = false) {
                        navController.navigate(Route.MainMap) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    BottomNavButton(emoji = "⚙️", label = "Ajustes", selected = false) {
                        navController.navigate(Route.UserProfile(-1)) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }



        ) { innerPadding ->
            NavHost(navController = navController,
                startDestination = Route.Home,
                modifier = Modifier.padding(innerPadding)) {
                composable<Route.Home> {
                    HomeEventViewRoot()
                }
                composable<Route.MainMap> {
                    VistaDePruebaMapa()
                }
                composable<Route.UserProfile> { it ->
                    val route: Route.UserProfile = it.toRoute()
                    ViewUserRoot(route.userID)
                }
            }

        }
    }


}