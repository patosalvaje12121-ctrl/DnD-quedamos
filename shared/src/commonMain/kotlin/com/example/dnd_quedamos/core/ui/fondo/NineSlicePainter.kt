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
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
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

        // Cortes en píxeles del PNG cargado (sea del tamaño que sea)
        val srcIzq = (corte.izq * imagen.width).roundToInt()
        val srcDer = (corte.der * imagen.width).roundToInt()
        val srcArriba = (corte.arriba * imagen.height).roundToInt()
        val srcAbajo = (corte.abajo * imagen.height).roundToInt()

        // Misma escala en X e Y para que las esquinas no se deformen.
        // Si los bordes no caben, se reducen en proporción.
        val escala = anchoDiseno.toPx() / imagen.width
        var izq = srcIzq * escala
        var der = srcDer * escala
        var arriba = srcArriba * escala
        var abajo = srcAbajo * escala
        if (izq + der > ancho) {
            val f = ancho / (izq + der); izq *= f; der *= f
        }
        if (arriba + abajo > alto) {
            val f = alto / (arriba + abajo); arriba *= f; abajo *= f
        }

        // Líneas de corte enteras, calculadas una sola vez: los trozos vecinos
        // comparten borde y no quedan huecos de 1 px.
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

                // Esquinas (índices 0 y 2): un único trozo. Centro (índice 1): se repite.
                val tileW = if (col == 1) srcW * escala else dstW.toFloat()
                val tileH = if (fila == 1) srcH * escala else dstH.toFloat()

                dibujarRepetido(
                    srcX = srcXs[col], srcY = srcYs[fila], srcW = srcW, srcH = srcH,
                    dstX = xs[col], dstY = ys[fila], dstW = dstW, dstH = dstH,
                    tileW = tileW, tileH = tileH,
                )
            }
        }
    }

    /**
     * Rellena la región destino repitiendo el trozo de origen con baldosas de [tileW]×[tileH] px.
     * La última baldosa de cada eje se recorta en origen (no se estira).
     */
    private fun DrawScope.dibujarRepetido(
        srcX: Int, srcY: Int, srcW: Int, srcH: Int,
        dstX: Int, dstY: Int, dstW: Int, dstH: Int,
        tileW: Float, tileH: Float,
    ) {
        // Baldosa degenerada (arte diminuto o escala ~0): mejor un trozo estirado que miles de dibujos.
        if (tileW < 1f || tileH < 1f) {
            dibujarTrozo(srcX, srcY, srcW, srcH, dstX, dstY, dstW, dstH)
            return
        }

        val columnas = ceil(dstW / tileW).toInt()
        val filas = ceil(dstH / tileH).toInt()

        for (f in 0 until filas) {
            // Los bordes se redondean desde la posición acumulada (no por baldosa):
            // así no se acumula error y no hay costuras entre baldosas.
            val y0 = (f * tileH).roundToInt()
            val y1 = min(((f + 1) * tileH).roundToInt(), dstH)
            val h = y1 - y0
            if (h <= 0) continue
            val sh = if (y1 - y0 >= tileH.roundToInt()) srcH
            else max(1, (srcH * h / tileH).roundToInt())

            for (c in 0 until columnas) {
                val x0 = (c * tileW).roundToInt()
                val x1 = min(((c + 1) * tileW).roundToInt(), dstW)
                val w = x1 - x0
                if (w <= 0) continue
                val sw = if (x1 - x0 >= tileW.roundToInt()) srcW
                else max(1, (srcW * w / tileW).roundToInt())

                dibujarTrozo(srcX, srcY, sw, sh, dstX + x0, dstY + y0, w, h)
            }
        }
    }

    private fun DrawScope.dibujarTrozo(
        srcX: Int, srcY: Int, srcW: Int, srcH: Int,
        dstX: Int, dstY: Int, dstW: Int, dstH: Int,
    ) {
        drawImage(
            image = imagen,
            srcOffset = IntOffset(srcX, srcY),
            srcSize = IntSize(srcW, srcH),
            dstOffset = IntOffset(dstX, dstY),
            dstSize = IntSize(dstW, dstH),
            colorFilter = colorFilter,
            filterQuality = filterQuality,
        )
    }
}