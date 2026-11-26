package com.example.dnd_quedamos

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

/** Cada pestaña: icono, descripción (solo accesibilidad), ruta destino y tipo de ruta para saber si está activa. */
private data class DestinoNav(
    val icono: DrawableResource,
    val descripcion: String,
    val ruta: Any,
    val tipoRuta: KClass<*>,
    val esInicio: Boolean = false,
)

private val destinosPhone = listOf(
    DestinoNav(DndSkins.Iconos.navCrearEvento, "Crear evento", Route.CreateEvent, Route.CreateEvent::class),
    DestinoNav(DndSkins.Iconos.navMensajes, "Chats", Route.ChatList, Route.ChatList::class),
    DestinoNav(DndSkins.Iconos.navInicio, "Inicio", Route.Home, Route.Home::class, esInicio = true),
    DestinoNav(DndSkins.Iconos.navMapa, "Mapa", Route.MainMap, Route.MainMap::class),
    DestinoNav(DndSkins.Iconos.navPerfil, "Perfil", Route.UserProfile(-1), Route.UserProfile::class),
)

private val destinosDesktop = listOf(
    destinosPhone[2],
    destinosPhone[1],
    destinosPhone[3],
    destinosPhone[0],
    destinosPhone[4],
)

@Composable
fun AppNavigationSuite(
    navController: NavController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val destinoActual = navController.currentBackStackEntryAsState().value?.destination
    val colores = MaterialTheme.colorScheme

    val base = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfoV2())
    val esVertical = base != NavigationSuiteType.NavigationBar &&
            base != NavigationSuiteType.ShortNavigationBarCompact &&
            base != NavigationSuiteType.ShortNavigationBarMedium &&
            base != NavigationSuiteType.None

    var abierto by rememberSaveable { mutableStateOf(false) }

    val layoutType = when {
        !esVertical -> base
        abierto -> NavigationSuiteType.WideNavigationRailExpanded
        else -> NavigationSuiteType.WideNavigationRailCollapsed
    }


    val navColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(indicatorColor = colores.primaryContainer),
        navigationRailItemColors = NavigationRailItemDefaults.colors(indicatorColor = colores.primaryContainer),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(selectedContainerColor = colores.primaryContainer)
    )

    val hoverSource = remember { MutableInteractionSource() }
    val hovered by hoverSource.collectIsHoveredAsState()

    LaunchedEffect(hovered) {
        if (hovered) {
            abierto = true
        } else {
            delay(150.milliseconds)
            abierto = false
        }
    }
    val mod = if(esVertical) modifier.hoverable(hoverSource) else modifier

    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = layoutType,
        navigationSuiteItems = {
            // Mostrar cada elemento ( caso distinto para horizontal )
            if(esVertical)
            {
                item(
                    selected = abierto,
                    onClick = { abierto = !abierto },
                    icon = {
                        Image(
                            painter = if(abierto) painterResource(DndSkins.Textures.navbarClose) else painterResource(DndSkins.Textures.navbarOpen),
                            contentDescription = if(abierto) "Cerrar" else "Cerrar",
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    // Por alguna razon si es el label es null se elimina el padding al estar en horizontal
                    // Esta es la forma que he encontrado para que se alinee con el resto de iconos
                    label = { if(abierto) Text(" ") else null },
                    alwaysShowLabel = abierto,
                    colors = navColors,
                )
            }

            val destinos = if(esVertical) destinosDesktop else destinosPhone;
            destinos.forEach { destino ->
                val label: (@Composable () -> Unit)? =
                    if (esVertical && abierto) { { Text(destino.descripcion) } } else null

                item(
                    selected = destinoActual?.hierarchy?.any { it.hasRoute(destino.tipoRuta) } == true,
                    onClick = { navController.irA(destino) },
                    icon = {
                        Image(
                            painter = painterResource(destino.icono),
                            contentDescription = destino.descripcion,
                            modifier = Modifier.size(if (esVertical) 64.dp else 48.dp),
                        )
                    },
                    label = label,
                    alwaysShowLabel = true,
                    colors = navColors,
                )
            }
        },
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = colores.surfaceContainer,
            navigationRailContainerColor = colores.surfaceContainer,
            navigationDrawerContainerColor = colores.surfaceContainer,
        ),
        content = content,
    )
}

private fun NavController.irA(destino: DestinoNav, save: Boolean = true) {
    navigate(destino.ruta) {
        if (destino.esInicio) {
            popUpTo(Route.Home) { inclusive = true }
            launchSingleTop = true
        } else {
            popUpTo(Route.Home) { saveState = save }
            launchSingleTop = true
            restoreState = true
        }
    }
}