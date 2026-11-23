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
        // Gold
        primary = Color(0xFFD9A32F),
        onPrimary = Color(0xFF3A0C15),
        primaryContainer = Color(0xFF7A1521),
        onPrimaryContainer = Color(0xFFF6D88A),
        inversePrimary = Color(0xFF9C6410),

        // Rose
        secondary = Color(0xFFD08A94),
        onSecondary = Color(0xFF42091B),
        secondaryContainer = Color(0xFF6A2640),
        onSecondaryContainer = Color(0xFFF4D3DA),

        // Violet
        tertiary = Color(0xFFB9A0F0),
        onTertiary = Color(0xFF2A0F3F),
        tertiaryContainer = Color(0xFF55307A),
        onTertiaryContainer = Color(0xFFE6D6FF),

        background = Color(0xFF16080F),
        onBackground = Color(0xFFEFE2C0),

        surface = Color(0xFF1E0F17),
        onSurface = Color(0xFFF3E9CC),
        surfaceVariant = Color(0xFF50303F),
        onSurfaceVariant = Color(0xFFDCC4CC),
        surfaceTint = Color(0xFFE4B54A),

        inverseSurface = Color(0xFFF1E4B8),
        inverseOnSurface = Color(0xFF2E0E1D),

        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),

        outline = Color(0xFFA3818E),
        outlineVariant = Color(0xFF5A3C4A),
        scrim = Color(0xFF000000),

        surfaceDim = Color(0xFF1A0B12),
        surfaceBright = Color(0xFF42202F),
        surfaceContainerLowest = Color(0xFF110509),
        surfaceContainerLow = Color(0xFF220F19),
        surfaceContainer = Color(0xFF2A141F),
        surfaceContainerHigh = Color(0xFF351B28),
        surfaceContainerHighest = Color(0xFF412333),

        // Fixed: gold
        primaryFixed = Color(0xFFFFE19A),
        primaryFixedDim = Color(0xFFE6B94F),
        onPrimaryFixed = Color(0xFF2B0710),
        onPrimaryFixedVariant = Color(0xFF7D2A12),

        // Fixed: orchid / purple-leaning
        secondaryFixed = Color(0xFFE8CBF2),
        secondaryFixedDim = Color(0xFFCFA3DE),
        onSecondaryFixed = Color(0xFF3A0F4A),
        onSecondaryFixedVariant = Color(0xFF6A3A7E),

        // Fixed: violet
        tertiaryFixed = Color(0xFFEFE5FF),
        tertiaryFixedDim = Color(0xFFCDB8F5),
        onTertiaryFixed = Color(0xFF1F0A33),
        onTertiaryFixedVariant = Color(0xFF4B2D78),
    )
