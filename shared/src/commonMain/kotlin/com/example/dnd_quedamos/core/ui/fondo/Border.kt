package com.example.dnd_quedamos.core.ui.fondo

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Borde de un [Fondo]. Se dibuja siempre DENTRO de los límites del fondo
 * y [grosorTotal] se suma al padding interno, así que el contenido nunca lo pisa.
 */
sealed interface Borde {
    val color: Color

    /** Espacio que ocupa el borde de fuera a dentro. */
    val grosorTotal: Dp

    /** Línea continua. */
    data class Linea(
        override val color: Color,
        val grosor: Dp = 2.dp,
    ) : Borde {
        override val grosorTotal get() = grosor
    }

    /** Dos líneas paralelas separadas por [separacion] (hueco visible entre ambas). */
    data class Doble(
        override val color: Color,
        val grosor: Dp = 1.dp,
        val separacion: Dp = 2.dp,
    ) : Borde {
        override val grosorTotal get() = grosor * 2 + separacion
    }

    /** Puntos redondos; [separacion] es el hueco visible entre dos puntos. */
    data class Punteado(
        override val color: Color,
        val grosor: Dp = 2.dp,
        val separacion: Dp = 3.dp,
    ) : Borde {
        override val grosorTotal get() = grosor
    }

    /** Rayas de [longitud] separadas por [separacion]. */
    data class Discontinuo(
        override val color: Color,
        val grosor: Dp = 2.dp,
        val longitud: Dp = 8.dp,
        val separacion: Dp = 4.dp,
    ) : Borde {
        override val grosorTotal get() = grosor
    }
}

/** Dibuja [borde] siguiendo [forma]. No hace nada si es null. */
internal fun Modifier.borde(borde: Borde?, forma: Shape): Modifier =
    if (borde == null) this else drawBehind { dibujarBorde(borde, forma) }

private fun DrawScope.dibujarBorde(borde: Borde, forma: Shape) {
    when (borde) {
        is Borde.Linea ->
            trazo(forma, borde.color, borde.grosor.toPx(), desplazamiento = 0f)

        is Borde.Doble -> {
            val g = borde.grosor.toPx()
            trazo(forma, borde.color, g, desplazamiento = 0f)
            trazo(forma, borde.color, g, desplazamiento = g + borde.separacion.toPx())
        }

        is Borde.Punteado -> {
            val g = borde.grosor.toPx()
            // Con extremo redondo, cada trazo de longitud ~0 es un punto de diámetro [g];
            // el intervalo suma [g] para que [separacion] sea el hueco visible.
            trazo(
                forma, borde.color, g, desplazamiento = 0f,
                cap = StrokeCap.Round,
                efecto = PathEffect.dashPathEffect(floatArrayOf(0.01f, g + borde.separacion.toPx())),
            )
        }

        is Borde.Discontinuo ->
            trazo(
                forma, borde.color, borde.grosor.toPx(), desplazamiento = 0f,
                efecto = PathEffect.dashPathEffect(
                    floatArrayOf(borde.longitud.toPx(), borde.separacion.toPx())
                ),
            )
    }
}

/**
 * Un trazo de [grosor] cuyo borde exterior queda a [desplazamiento] px del límite del fondo.
 * El trazo se centra sobre el contorno, por eso se recorta la mitad del grosor.
 */
private fun DrawScope.trazo(
    forma: Shape,
    color: Color,
    grosor: Float,
    desplazamiento: Float,
    cap: StrokeCap = StrokeCap.Butt,
    efecto: PathEffect? = null,
) {
    inset(desplazamiento + grosor / 2f) {
        if (size.minDimension <= 0f) return@inset
        drawOutline(
            outline = forma.createOutline(size, layoutDirection, this),
            color = color,
            style = Stroke(width = grosor, cap = cap, pathEffect = efecto),
        )
    }
}