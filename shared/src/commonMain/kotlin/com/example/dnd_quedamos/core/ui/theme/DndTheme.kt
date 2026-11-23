package com.example.dnd_quedamos.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Medidas comunes de la app. En las pantallas se usan con DndTheme.dimens.m, etc.
 * en lugar de escribir 8.dp, 12.dp, 16.dp… sueltos.
 */
@Immutable
data class DndDimens(
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

private val LocalDndDimens = staticCompositionLocalOf { DndDimens() }

/** Tema de la app: colores, tipografía y medidas. Envuelve todo el contenido en App.kt. */
@Composable
fun DndTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDndDimens provides DndDimens()) {
        MaterialTheme(
            colorScheme = DndColores,
            typography = dndTipografia(),
            content = content,
        )
    }
}

/** Acceso a lo que añade DndTheme por encima de MaterialTheme. */
object DndTheme {
    val dimens: DndDimens
        @Composable @ReadOnlyComposable
        get() = LocalDndDimens.current
}
