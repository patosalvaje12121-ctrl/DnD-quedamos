package com.example.dnd_quedamos.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.example.dnd_quedamos.resources.MedievalSharp_Book
import com.example.dnd_quedamos.resources.Res
import org.jetbrains.compose.resources.Font

/**
 * Tipografía de la app: MedievalSharp en títulos y cabeceras, la de sistema en el texto normal.
 * Dentro de las pantallas se usa con MaterialTheme.typography.titleMedium, bodySmall, etc.
 * en lugar de poner fontSize a mano.
 */
@Composable
fun dndTipografia(): Typography {
    val medieval = FontFamily(Font(Res.font.MedievalSharp_Book))
    val base = Typography()
    return base.copy(
        headlineLarge = base.headlineLarge.copy(fontFamily = medieval),
        headlineMedium = base.headlineMedium.copy(fontFamily = medieval),
        headlineSmall = base.headlineSmall.copy(fontFamily = medieval),
        titleLarge = base.titleLarge.copy(fontFamily = medieval),
        titleMedium = base.titleMedium.copy(fontFamily = medieval),
        titleSmall = base.titleSmall.copy(fontFamily = medieval),
    )
}
