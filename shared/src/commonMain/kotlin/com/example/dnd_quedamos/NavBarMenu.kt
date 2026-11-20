package com.example.dnd_quedamos

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dnd_quedamos.resources.NVAEvents
import com.example.dnd_quedamos.resources.NVMain
import com.example.dnd_quedamos.resources.NVMap
import com.example.dnd_quedamos.resources.NVMessages
import com.example.dnd_quedamos.resources.NVProfile
import com.example.dnd_quedamos.resources.Res
import org.jetbrains.compose.resources.painterResource

@Composable
fun NavBarMenu(
    navController : NavController,

) {
    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets,
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
    ) {
        BottomNavButton(image = painterResource(Res.drawable.NVAEvents), label = "AddEvento") {
            navController.navigate(Route.CreateEvent) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        BottomNavButton(image = painterResource(Res.drawable.NVMessages), label = "Partidas",
        ) {
            navController.navigate(Route.Chats) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        BottomNavButton(image = painterResource(Res.drawable.NVMain), label = "Inicio") {
            navController.navigate(Route.Home) {
                popUpTo(Route.Home) { inclusive = true }
                launchSingleTop = true
            }
        }
        BottomNavButton(image = painterResource(Res.drawable.NVMap), label = "Mapa") {
            navController.navigate(Route.MainMap) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        BottomNavButton(image = painterResource(Res.drawable.NVProfile), label = "Personajes") {
            navController.navigate(Route.UserProfile(-1)) {
                popUpTo(Route.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}

@Composable
fun RowScope.BottomNavButton(
    image: Painter,
    label: String,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        icon = {
            Icon(
                painter = image,
                contentDescription = label,
                tint = Color.Unspecified,
                modifier = Modifier.size(164.dp),
            )
        },
        label = { Text(label) },                    // optional, see Bug 3
        alwaysShowLabel = false,
    )
}