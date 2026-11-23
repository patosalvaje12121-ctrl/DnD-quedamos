package com.example.dnd_quedamos.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta de la app (tema oscuro "pergamino y vino").
 * Dentro de las pantallas se lee SIEMPRE con MaterialTheme.colorScheme, nunca con esta val:
 * así, si algún día hay tema claro o variantes, ninguna pantalla cambia.
 */
val DndColores: ColorScheme =
    ColorScheme(
        primary = Color(0xFFD09D2B),
        onPrimary = Color(0xFF350B13),
        primaryContainer = Color(0xFF6B1119),
        onPrimaryContainer = Color(0xFFF0CD7B),
        inversePrimary = Color(0xFFBC7C23),

        secondary = Color(0xFFC58A8F),
        onSecondary = Color(0xFF3B0A18),
        secondaryContainer = Color(0xFF5A2235),
        onSecondaryContainer = Color(0xFFF2E7B6),

        tertiary = Color(0xFFC7B3E6),
        onTertiary = Color(0xFF24102E),
        tertiaryContainer = Color(0xFF4A2A5C),
        onTertiaryContainer = Color(0xFFEADDFF),

        background = Color(0xFF190A13),
        onBackground = Color(0xFFF2E7B6),

        surface = Color(0xFF211018),
        onSurface = Color(0xFFF2E7B6),
        surfaceVariant = Color(0xFF4A2A38),
        onSurfaceVariant = Color(0xFFD9C2C9),
        surfaceTint = Color(0xFFD09D2B),

        inverseSurface = Color(0xFFF2E7B6),
        inverseOnSurface = Color(0xFF2A0B1A),

        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),

        outline = Color(0xFF9A7A86),
        outlineVariant = Color(0xFF4E3541),
        scrim = Color(0xFF000000),

        surfaceBright = Color(0xFF3A1B27),
        surfaceContainer = Color(0xFF26121C),
        surfaceContainerHigh = Color(0xFF311923),
        surfaceContainerHighest = Color(0xFF3C1F2B),
        surfaceContainerLow = Color(0xFF1F0D16),
        surfaceContainerLowest = Color(0xFF14070E),
        surfaceDim = Color(0xFF190A13),

        primaryFixed = Color(0xFFF0CD7B),
        primaryFixedDim = Color(0xFFD09D2B),
        onPrimaryFixed = Color(0xFF350B13),
        onPrimaryFixedVariant = Color(0xFF6B1119),

        secondaryFixed = Color(0xFFF0CDD6),
        secondaryFixedDim = Color(0xFFC58A8F),
        onSecondaryFixed = Color(0xFF3B0A18),
        onSecondaryFixedVariant = Color(0xFF5A2235),

        tertiaryFixed = Color(0xFFEADDFF),
        tertiaryFixedDim = Color(0xFFC7B3E6),
        onTertiaryFixed = Color(0xFF24102E),
        onTertiaryFixedVariant = Color(0xFF4A2A5C),
    )
