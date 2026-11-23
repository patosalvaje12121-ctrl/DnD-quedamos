package com.example.dnd_quedamos.core.ui.fondo

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * Bordes del 9-slice como FRACCIÓN de la imagen (0..1), no en píxeles.
 * Así el mismo Corte vale para el PNG de 2048 px y para una versión de 256 px,
 * o para las variantes drawable-mdpi / -xhdpi / -xxhdpi de composeResources.
 *
 * Lo normal es crearlo con [Corte.px], midiendo en la imagen que tengas a mano.
 */
data class Corte(val izq: Float, val arriba: Float, val der: Float, val abajo: Float) {
    companion object {
        /** Bordes medidos en píxeles sobre una imagen de [ancho] x [alto] (la que hayas medido). */
        fun px(izq: Int, arriba: Int, der: Int, abajo: Int, ancho: Int, alto: Int) = Corte(
            izq = izq.toFloat() / ancho,
            arriba = arriba.toFloat() / alto,
            der = der.toFloat() / ancho,
            abajo = abajo.toFloat() / alto,
        )

        /** Mismo borde en los 4 lados, medido en píxeles sobre una imagen de [ancho] x [alto]. */
        fun px(todos: Int, ancho: Int, alto: Int) = px(todos, todos, todos, todos, ancho, alto)
    }
}

/**
 * Pinta [imagen] como 9-slice: las 4 esquinas mantienen su forma, los lados se estiran
 * en una sola dirección y el centro se estira en las dos.
 *
 * [anchoDiseno] = el ancho que tendría la imagen ENTERA en pantalla. Fija el tamaño de los
 * bordes sin depender de los píxeles del PNG. Ejemplo: borde del 5 % y anchoDiseno 240.dp
 * -> el borde mide 12 dp, tanto si el PNG mide 2048 px como si mide 256 px.
 *
 * No tiene tamaño propio: ocupa lo que mida el contenedor.
 */
class NineSlicePainter(
    private val imagen: ImageBitmap,
    private val corte: Corte,
    private val anchoDiseno: Dp,
    private val filterQuality: FilterQuality = FilterQuality.Medium,
) : Painter() {

    override val intrinsicSize: Size = Size.Unspecified

    // Lo pone Modifier.paint(colorFilter = …); se aplica a cada trozo al dibujar
    private var colorFilter: ColorFilter? = null

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        val ancho = size.width
        val alto = size.height
        if (ancho <= 0f || alto <= 0f || imagen.width <= 0 || imagen.height <= 0) return

        // Cortes en píxeles del PNG que se haya cargado (sea del tamaño que sea)
        val srcIzq = (corte.izq * imagen.width).roundToInt()
        val srcDer = (corte.der * imagen.width).roundToInt()
        val srcArriba = (corte.arriba * imagen.height).roundToInt()
        val srcAbajo = (corte.abajo * imagen.height).roundToInt()

        // Bordes en pantalla (px). Misma escala en X e Y para que las esquinas no se deformen.
        // Si no caben, se reducen en proporción.
        val pxPorPixel = anchoDiseno.toPx() / imagen.width
        var izq = srcIzq * pxPorPixel
        var der = srcDer * pxPorPixel
        var arriba = srcArriba * pxPorPixel
        var abajo = srcAbajo * pxPorPixel
        if (izq + der > ancho) {
            val f = ancho / (izq + der); izq *= f; der *= f
        }
        if (arriba + abajo > alto) {
            val f = alto / (arriba + abajo); arriba *= f; abajo *= f
        }

        // Líneas de corte en origen y destino. Todo en enteros, calculado una sola vez,
        // para que trozos vecinos compartan el mismo borde y no queden huecos de 1 px.
        val anchoDst = ancho.roundToInt()
        val altoDst = alto.roundToInt()
        val xs = intArrayOf(0, izq.roundToInt(), anchoDst - der.roundToInt(), anchoDst)
        val ys = intArrayOf(0, arriba.roundToInt(), altoDst - abajo.roundToInt(), altoDst)
        val srcXs = intArrayOf(0, srcIzq, imagen.width - srcDer, imagen.width)
        val srcYs = intArrayOf(0, srcArriba, imagen.height - srcAbajo, imagen.height)

        for (fila in 0..2) {
            for (col in 0..2) {
                val srcW = srcXs[col + 1] - srcXs[col]
                val srcH = srcYs[fila + 1] - srcYs[fila]
                val dstW = xs[col + 1] - xs[col]
                val dstH = ys[fila + 1] - ys[fila]
                if (srcW <= 0 || srcH <= 0 || dstW <= 0 || dstH <= 0) continue
                drawImage(
                    image = imagen,
                    srcOffset = IntOffset(srcXs[col], srcYs[fila]),
                    srcSize = IntSize(srcW, srcH),
                    dstOffset = IntOffset(xs[col], ys[fila]),
                    dstSize = IntSize(dstW, dstH),
                    colorFilter = colorFilter,
                    filterQuality = filterQuality,
                )
            }
        }
    }
}
