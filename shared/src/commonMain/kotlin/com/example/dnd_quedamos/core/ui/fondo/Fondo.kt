package com.example.dnd_quedamos.core.ui.fondo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource

/**
 * Describe el fondo de un panel o botón. Se aplica siempre con [Modifier.fondo].
 *
 * PNG de varios tamaños: nada de aquí depende de los píxeles de la imagen. Puedes:
 *  - cambiar un PNG por otro más pequeño con el mismo dibujo, sin tocar código, o
 *  - poner variantes por densidad en composeResources/drawable-mdpi, -hdpi, -xhdpi,
 *    -xxhdpi... con el mismo nombre; Compose elige la adecuada para cada pantalla.
 * La única condición es que todas las variantes de una imagen tengan la misma proporción.
 */
sealed interface Fondo {

    /** Color plano, con forma opcional (esquinas redondeadas, etc.). */
    data class Solido(val color: Color, val forma: Shape = RectangleShape, val borde: Borde? = null) : Fondo

    data class Gradient(val color1: Color, val color2: Color, val direccion: Direccion = Direccion.Vertical, val forma: Shape = RectangleShape, val borde: Borde? = null) : Fondo {
        enum class Direccion { Vertical, Horizontal, Diagonal }
    }



        /** Imagen entera escalada al tamaño del contenedor. */
    data class Imagen(
        val res: DrawableResource,
        val escalado: ContentScale = ContentScale.FillBounds,
        val tinte: ColorFilter? = null,
    ) : Fondo

    /**
     * Imagen 9-slice: esquinas fijas, lados y centro estirados. Ver [NineSlicePainter].
     * [corte] en fracciones de la imagen (usa [Corte.px] para crearlo midiendo en píxeles).
     * [anchoDiseno] = ancho que tendría la imagen entera en pantalla; fija el grosor del borde.
     */
    data class NineSlice(
        val res: DrawableResource,
        val corte: Corte,
        val anchoDiseno: Dp,
        val filterQuality: FilterQuality = FilterQuality.Medium,
        val tinte: ColorFilter? = null,
    ) : Fondo

    companion object {
        /** Oscurece la imagen sin tocar sus transparencias. Sirve para el estado "pulsado". */
        val OSCURECER: ColorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.25f), BlendMode.SrcAtop)
    }
}

/**
 * La misma imagen con un tinte (p. ej. [Fondo.OSCURECER] para el estado pulsado de un botón
 * que no tiene arte propio para ese estado). En un color plano no hace nada.
 */
fun Fondo.conTinte(tinte: ColorFilter?): Fondo = when (this) {
    is Fondo.Solido, is Fondo.Gradient -> this
    is Fondo.Imagen -> copy(tinte = tinte)
    is Fondo.NineSlice -> copy(tinte = tinte)
}

/** Pinta [fondo] detrás del contenido. Solo pinta: no cambia el tamaño ni añade padding. */
@Composable
fun Modifier.fondo(fondo: Fondo): Modifier = when (fondo) {
    is Fondo.Solido ->
        this.background(fondo.color, fondo.forma)
            .borde(fondo.borde, fondo.forma)

    is Fondo.Imagen -> this.paint(
        painter = painterResource(fondo.res),
        sizeToIntrinsics = false,
        contentScale = fondo.escalado,
        colorFilter = fondo.tinte,
    )

    is Fondo.NineSlice -> {
        val imagen = imageResource(fondo.res)
        val painter = remember(imagen, fondo) {
            NineSlicePainter(imagen, fondo.corte, fondo.anchoDiseno, fondo.filterQuality)
        }
        this.paint(
            painter = painter,
            sizeToIntrinsics = false,
            contentScale = ContentScale.FillBounds,
            colorFilter = fondo.tinte,
        )
    }

    is Fondo.Gradient -> {
        val colores = listOf(fondo.color1, fondo.color2)
        val brush = when (fondo.direccion) {
            Fondo.Gradient.Direccion.Vertical -> Brush.verticalGradient(colores)
            Fondo.Gradient.Direccion.Horizontal -> Brush.horizontalGradient(colores)
            Fondo.Gradient.Direccion.Diagonal -> Brush.linearGradient(colores)
        }
        this.background(brush, fondo.forma).borde(fondo.borde, fondo.forma)
    }
}

/**
 * Margen para que el contenido quede dentro del marco (solo 9-slice; el resto, 0).
 * Lo usarán DndPanel / DndBoton; también se puede aplicar a mano con Modifier.padding.
 */
@Composable
fun Fondo.paddingInterno(): PaddingValues = when (this) {
    is Fondo.NineSlice -> {
        val imagen = imageResource(res)
        // En web el PNG carga en segundo plano y mientras tanto llega una imagen de 1x1:
        // con ella la proporción sería falsa y el margen saldría enorme. Hasta que cargue, 0.
        if (imagen.width <= 1 || imagen.height <= 1) return PaddingValues(0.dp)
        // Misma cuenta que NineSlicePainter: dp por cada píxel del PNG cargado
        val dpPorPixel = anchoDiseno.value / imagen.width
        PaddingValues(
            start = (corte.izq * imagen.width * dpPorPixel).dp,
            top = (corte.arriba * imagen.height * dpPorPixel).dp,
            end = (corte.der * imagen.width * dpPorPixel).dp,
            bottom = (corte.abajo * imagen.height * dpPorPixel).dp,
        )
    }
    is Fondo.Solido -> PaddingValues(borde?.grosorTotal ?: 0.dp)
    is Fondo.Gradient -> PaddingValues(borde?.grosorTotal ?: 0.dp)
    else -> PaddingValues(0.dp)
}
