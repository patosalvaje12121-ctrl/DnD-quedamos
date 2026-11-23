package com.example.dnd_quedamos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.dnd_quedamos.core.ui.componentes.DndBotonIcono
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import org.jetbrains.compose.resources.DrawableResource
import kotlin.reflect.KClass

/** Cada pestaña: icono, descripción (solo accesibilidad), ruta destino y tipo de ruta para saber si está activa. */
private data class DestinoNav(
    val icono: DrawableResource,
    val descripcion: String,
    val ruta: Any,
    val tipoRuta: KClass<*>,
    val esInicio: Boolean = false,
)

private val destinos = listOf(
    DestinoNav(DndSkins.Iconos.navCrearEvento, "Crear evento", Route.CreateEvent, Route.CreateEvent::class),
    DestinoNav(DndSkins.Iconos.navMensajes, "Partidas", Route.Chats, Route.Chats::class),
    DestinoNav(DndSkins.Iconos.navInicio, "Inicio", Route.Home, Route.Home::class, esInicio = true),
    DestinoNav(DndSkins.Iconos.navMapa, "Mapa", Route.MainMap, Route.MainMap::class),
    DestinoNav(DndSkins.Iconos.navPerfil, "Perfil", Route.UserProfile(-1), Route.UserProfile::class),
)

// ---- Medidas ----
// ---- Medidas ----
private val AnchoMaximoBarra = 1000.dp  // PC: los botones pueden separarse más
private val TamanoIconoMin = 36.dp
private val TamanoIconoMax = 128.dp     // techo en pantallas grandes
private const val FraccionIcono = 0.75f // parte del hueco que ocupa el icono
private val Relleno = 8.dp
@Composable
fun NavBarMenu(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val destinoActual = navController.currentBackStackEntryAsState().value?.destination

    // El fondo ocupa todo el ancho y queda bajo la barra del sistema;
    // el padding de los insets va después para que el contenido no se solape con ella.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(modifier = Modifier.widthIn(max = AnchoMaximoBarra)) {
            // El icono crece con el ancho disponible, entre un mínimo y un máximo.
            val tamanoIcono = (maxWidth / destinos.size * FraccionIcono)
                .coerceIn(TamanoIconoMin, TamanoIconoMax)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Relleno),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinos.forEach { destino ->
                    ItemNav(
                        destino = destino,
                        seleccionado = destinoActual?.hierarchy?.any { it.hasRoute(destino.tipoRuta) } == true,
                        tamanoIcono = tamanoIcono,
                        onClick = { navController.irA(destino) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemNav(
    destino: DestinoNav,
    seleccionado: Boolean,
    tamanoIcono: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (seleccionado) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                )
                .padding(Relleno),
        ) {
            DndBotonIcono(
                icono = destino.icono,
                descripcion = destino.descripcion, // sin etiqueta visible: esto es lo que lee TalkBack
                onClick = onClick,
                tamano = tamanoIcono,
            )
        }
    }
}

private fun NavController.irA(destino: DestinoNav) {
    navigate(destino.ruta) {
        if (destino.esInicio) {
            popUpTo(Route.Home) { inclusive = true }
            launchSingleTop = true
        } else {
            popUpTo(Route.Home) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}