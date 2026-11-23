package com.example.dnd_quedamos.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.example.dnd_quedamos.resources.Indulta_SemiSerif_Bold
import com.example.dnd_quedamos.resources.Indulta_SemiSerif_Bold_Italic
import com.example.dnd_quedamos.resources.Res
import com.example.dnd_quedamos.resources.VinqueAntique_Bold
import com.example.dnd_quedamos.resources.VinqueAntique_Bold_Italic
import com.example.dnd_quedamos.resources.Vinque_Regular
import org.jetbrains.compose.resources.Font

/**
 * Tipografía de la app: MedievalSharp en títulos y cabeceras, la de sistema en el texto normal.
 * Dentro de las pantallas se usa con MaterialTheme.typography.titleMedium, bodySmall, etc.
 * en lugar de poner fontSize a mano.
 */
@Composable
fun dndTipografia(): Typography {
    val indulta = FontFamily(Font(Res.font.Indulta_SemiSerif_Bold))
    val indultaItalic = FontFamily(Font(Res.font.Indulta_SemiSerif_Bold_Italic))
    val vinqueBold = FontFamily(Font(Res.font.VinqueAntique_Bold))
    val vinque = FontFamily(Font(Res.font.Vinque_Regular))

    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = vinqueBold),
        displayMedium = base.displayMedium.copy(fontFamily = vinqueBold),
        displaySmall = base.displaySmall.copy(fontFamily = vinqueBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = vinqueBold),
        headlineMedium = base.headlineMedium.copy(fontFamily = vinqueBold),
        headlineSmall = base.headlineSmall.copy(fontFamily = vinqueBold),
        titleLarge = base.titleLarge.copy(fontFamily = vinque),
        titleMedium = base.titleMedium.copy(fontFamily = vinque),
        titleSmall = base.titleSmall.copy(fontFamily = vinque),
        bodyLarge = base.bodyLarge.copy(fontFamily = indulta),
        bodyMedium = base.bodyMedium.copy(fontFamily = indulta),
        bodySmall = base.bodySmall.copy(fontFamily = indulta),
        labelLarge = base.labelLarge.copy(fontFamily = indultaItalic),
        labelMedium = base.labelMedium.copy(fontFamily = indultaItalic),
        labelSmall = base.labelSmall.copy(fontFamily = indultaItalic),
    )
}
