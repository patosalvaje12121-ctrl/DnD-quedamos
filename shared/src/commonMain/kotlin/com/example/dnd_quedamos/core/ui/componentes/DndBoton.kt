package com.example.dnd_quedamos.core.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.core.ui.fondo.Fondo
import com.example.dnd_quedamos.core.ui.fondo.fondo
import com.example.dnd_quedamos.core.ui.fondo.paddingInterno
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource

/**
 * Aspecto de un botón en cada estado. Si un estado no tiene arte propio, se reutiliza
 * [normal] (para pulsado, normalmente con Fondo.conTinte(Fondo.OSCURECER)).
 *
 * [alfaDeshabilitado] se aplica al botón entero cuando está deshabilitado; ponlo a 1f
 * si [deshabilitado] ya tiene un aspecto apagado propio.
 */
@Immutable
data class SkinBoton(
    val normal: Fondo,
    val pulsado: Fondo = normal,
    val deshabilitado: Fondo = normal,
    val colorContenido: Color,
    val padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    val alfaDeshabilitado: Float = 0.5f,
)

/**
 * Botón de la app. Sustituye a Button / OutlinedButton / TextButton de Material:
 * el aspecto lo decide [skin] (ver DndSkins.Botones), no la pantalla.
 *
 * El contenido es libre (texto, icono + texto…) y toma el color y el estilo de texto del
 * botón sin indicarlos. El área táctil mínima es de 48 dp aunque el dibujo sea más pequeño.
 */
@Composable
fun DndBoton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    skin: SkinBoton = DndSkins.Botones.principal,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interaccion = remember { MutableInteractionSource() }
    val pulsado by interaccion.collectIsPressedAsState()
    val fondo = when {
        !enabled -> skin.deshabilitado
        pulsado -> skin.pulsado
        else -> skin.normal
    }

    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .alpha(if (enabled) 1f else skin.alfaDeshabilitado)
            .defaultMinSize(minWidth = 64.dp, minHeight = 40.dp)
            .fondo(fondo)
            .clickable(
                interactionSource = interaccion,
                indication = null, // el cambio de fondo al pulsar ya es la respuesta visual
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(fondo.paddingInterno())
            .padding(skin.padding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides skin.colorContenido,
            LocalTextStyle provides MaterialTheme.typography.labelLarge,
        ) {
            content()
        }
    }
}

/**
 * Botón que es solo una imagen (iconos de la barra inferior, cerrar, etc.).
 * Al pulsarlo se oscurece; deshabilitado se ve a medio tono.
 */
@Composable
fun DndBotonIcono(
    icono: DrawableResource,
    descripcion: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tamano: Dp = 48.dp,
    filterQuality: FilterQuality = FilterQuality.Medium,
) {
    val interaccion = remember { MutableInteractionSource() }
    val pulsado by interaccion.collectIsPressedAsState()

    Image(
        bitmap = imageResource(icono),
        filterQuality = filterQuality,
        contentDescription = descripcion,
        contentScale = ContentScale.Fit,
        colorFilter = if (pulsado) Fondo.OSCURECER else null,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(tamano)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                interactionSource = interaccion,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
    )
}
