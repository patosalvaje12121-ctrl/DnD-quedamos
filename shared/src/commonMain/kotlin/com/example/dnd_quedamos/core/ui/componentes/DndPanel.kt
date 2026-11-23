package com.example.dnd_quedamos.core.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    content: @Composable ColumnScope.() -> Unit,
) {
    val pulsable = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier

    Column(
        modifier = modifier
            .fondo(skin.fondo)
            .then(pulsable)
            .padding(skin.fondo.paddingInterno())
            .padding(padding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        CompositionLocalProvider(LocalContentColor provides skin.colorContenido) {
            content()
        }
    }
}
