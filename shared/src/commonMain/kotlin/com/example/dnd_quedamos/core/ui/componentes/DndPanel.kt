package com.example.dnd_quedamos.core.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.core.ui.fondo.Fondo
import com.example.dnd_quedamos.core.ui.fondo.fondo
import com.example.dnd_quedamos.core.ui.fondo.paddingInterno
import com.example.dnd_quedamos.core.ui.skins.DndSkins

/**
 * Aspecto de un panel: qué fondo lleva, de qué color va lo de dentro (texto, iconos)
 * y cuánto margen deja además del borde del marco.
 * Los paneles concretos de la app están en [DndSkins].
 */
@Immutable
data class SkinPanel(
    val fondo: Fondo,
    val colorContenido: Color,
    val padding: PaddingValues = PaddingValues(0.dp),
)

/**
 * Panel con fondo (color, imagen o 9-slice). Sustituye a Card / Box con .paint(...) a mano.
 *
 * - El contenido queda SIEMPRE dentro del marco: se aplica el borde del 9-slice
 *   y después [padding].
 * - Los Text e Icon de dentro toman [SkinPanel.colorContenido] sin indicar color.
 * - Si [onClick] no es null, el panel entero es pulsable (p. ej. una fila de la lista).
 */
@Composable
fun DndPanel(
    modifier: Modifier = Modifier,
    skin: SkinPanel = DndSkins.panel,
    padding: PaddingValues = skin.padding,
    onClick: (() -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    trailing: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val pulsable = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier

    val inset = skin.fondo.paddingInterno()
    val endInset = inset.calculateEndPadding(LocalLayoutDirection.current)

    Box(
        modifier = modifier
            .fondo(skin.fondo)
            .then(pulsable)
    ) {
        Column(
            modifier = Modifier.padding(inset).padding(padding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
        ) {
            CompositionLocalProvider(LocalContentColor provides skin.colorContenido) {
                content()
            }
        }

        if (trailing != null) {
            // matchParentSize: doesn't affect the panel's size, only overlays it
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
                Box(Modifier.width(endInset), contentAlignment = Alignment.Center) {
                    CompositionLocalProvider(LocalContentColor provides skin.colorContenido) {
                        trailing()
                    }
                }
            }
        }
    }
}