package com.example.dnd_quedamos.core.ui.skins

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.core.ui.componentes.SkinBoton
import com.example.dnd_quedamos.core.ui.componentes.SkinPanel
import com.example.dnd_quedamos.core.ui.fondo.Borde
import com.example.dnd_quedamos.core.ui.fondo.Corte
import com.example.dnd_quedamos.core.ui.fondo.Fondo
import com.example.dnd_quedamos.core.ui.fondo.conTinte
import com.example.dnd_quedamos.core.ui.theme.DndColores
import com.example.dnd_quedamos.resources.Res
import com.example.dnd_quedamos.resources.fondo_mosaico
import com.example.dnd_quedamos.resources.icono_nav_crear_evento
import com.example.dnd_quedamos.resources.icono_nav_inicio
import com.example.dnd_quedamos.resources.icono_nav_mapa
import com.example.dnd_quedamos.resources.icono_nav_mensajes
import com.example.dnd_quedamos.resources.icono_nav_perfil
import com.example.dnd_quedamos.resources.panel_fila_chat
import com.example.dnd_quedamos.resources.panel_mapa
import com.example.dnd_quedamos.resources.panel_marco
import org.jetbrains.compose.resources.DrawableResource

/**
 * ÚNICO sitio que conoce las imágenes de composeResources/drawable.
 * Las pantallas piden "el panel de fila de chat", no "MSPanel.png": si cambia el arte,
 * solo se toca este archivo.
 *
 * Nombres de los PNG por tipo: fondo_*, panel_*, boton_*, icono_*.
 *
 * PNG de varios tamaños: los cortes van en fracciones (Corte.px mide sobre la imagen
 * original), así que puedes:
 *  - cambiar un PNG por otro más pequeño con el mismo dibujo sin tocar nada aquí, o
 *  - añadir variantes por densidad con el MISMO nombre en drawable-mdpi, -hdpi, -xhdpi,
 *    -xxhdpi, -xxxhdpi. Compose elige la variante según la pantalla; la carpeta
 *    drawable/ (sin sufijo) se usa cuando no hay una variante que encaje.
 *    Todas las variantes de una imagen deben tener la misma proporción.
 */
object DndSkins {

    // ---------- Paneles 9-slice (se estiran sin deformar el borde) ----------

    /** Marco de pergamino con borde marrón; para tarjetas y diálogos. Borde de ~11 dp. */
    val panelMarco = Fondo.NineSlice(
        Res.drawable.panel_marco,
        Corte.px(izq = 90, arriba = 115, der = 90, abajo = 115, ancho = 2048, alto = 2048),
        anchoDiseno = 250.dp,
    )

    /** Fila de la lista de chats; la caja oscura de la derecha (hora) no se deforma. */
    val panelFilaChat = Fondo.NineSlice(
        Res.drawable.panel_fila_chat,
        Corte.px(izq = 50, arriba = 50, der = 490, abajo = 60, ancho = 2048, alto = 233),
        anchoDiseno = 512.dp,
    )
    // ---------- Imágenes enteras ----------

    /** Mapa de fondo de la tarjeta de evento (se recorta, no se estira). */
    val panelMapa = Fondo.Imagen(Res.drawable.panel_mapa, ContentScale.Crop)

    // ---------- Fondos de pantalla ----------

    /** Textura que se repite en vertical en el feed de eventos. */
    val fondoMosaico: DrawableResource = Res.drawable.fondo_mosaico

    // ---------- Skins de paneles (para DndPanel) ----------

    /** Panel por defecto: pergamino con marco, texto color vino. */
    val panel = SkinPanel(
        fondo = panelMarco,
        colorContenido = DndColores.onPrimary,
        padding = PaddingValues(8.dp),
    )

    /** Fila de la lista de chats. */
    val filaChat = SkinPanel(
        fondo = panelFilaChat,
        colorContenido = DndColores.onPrimary,
    )

    // ---------- Skins de botones (para DndBoton) ----------
    // Aún no hay PNG de botones: cuando los haya, se cambia el Fondo aquí y ninguna pantalla se toca.

    object Botones {
        private val forma = RoundedCornerShape(8.dp)

        /** Acción principal de la pantalla (Guardar, Apuntarme, Iniciar sesión…). */
        val principal = SkinBoton(
            normal = Fondo.Solido(DndColores.primary, borde = Borde.Linea(DndColores.inversePrimary)),
            pulsado = Fondo.Solido(DndColores.inversePrimary, borde = Borde.Linea(DndColores.inversePrimary)),
            colorContenido = DndColores.onPrimary,
        )

        /** Acción secundaria: el mismo marco de pergamino, con el borde más fino. */
        val secundario: SkinBoton = panelMarco.copy(anchoDiseno = 110.dp).let { marcoFino ->
            SkinBoton(
                normal = marcoFino,
                pulsado = marcoFino.conTinte(Fondo.OSCURECER),
                colorContenido = DndColores.onPrimary,
                padding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        /** Acción terciaria: destacar. */
        val terciario: SkinBoton = SkinBoton(
            normal = Fondo.Gradient(DndColores.primary, DndColores.surfaceTint, forma = forma, borde = Borde.Doble(DndColores.inverseSurface)),
            pulsado = Fondo.Solido(DndColores.inversePrimary, forma = forma, borde = Borde.Doble(DndColores.inverseSurface)),
            colorContenido = DndColores.inverseSurface,
        )


        /** Acción discreta (Cancelar, Volver, Editar en una lista…): solo texto. */
        val texto = SkinBoton(
            normal = Fondo.Solido(Color.Transparent, forma),
            pulsado = Fondo.Solido(DndColores.primary.copy(alpha = 0.12f), forma),
            colorContenido = DndColores.tertiary,
            padding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        )
    }

    // ---------- Iconos ----------

    object Iconos {
        val navCrearEvento: DrawableResource = Res.drawable.icono_nav_crear_evento
        val navMensajes: DrawableResource = Res.drawable.icono_nav_mensajes
        val navInicio: DrawableResource = Res.drawable.icono_nav_inicio
        val navMapa: DrawableResource = Res.drawable.icono_nav_mapa
        val navPerfil: DrawableResource = Res.drawable.icono_nav_perfil
    }
}
