package com.example.dnd_quedamos.core.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Botón de texto cuya acción es opcional: sin callback queda deshabilitado.
 * Centraliza el patrón `enabled = onX != null` / `onX?.invoke()`.
 */

@Composable
fun SeparatorOr(thickness: Dp = 2.dp, spaced: Dp = 12.dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spaced),
    ) {
        HorizontalDivider(Modifier.weight(1f), thickness)
        Text("o")
        HorizontalDivider(Modifier.weight(1f), thickness)
    }
}

@Composable
fun Separator(thickness: Dp = 2.dp, spaced: Dp = 12.dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spaced),
    ) {
        HorizontalDivider(Modifier.weight(1f), thickness)
    }
}

@Composable
fun SeparatorDouble(thickness: Dp = 2.dp, spaced: Dp = 12.dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spaced),
    ) {
        HorizontalDivider(Modifier.weight(1f), thickness = thickness)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), thickness = thickness)
    }
}