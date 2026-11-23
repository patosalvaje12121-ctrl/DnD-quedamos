package com.example.dnd_quedamos.core.ui.componentes

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dnd_quedamos.core.ui.skins.DndSkins

@Composable
fun AccionBoton(
    texto: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    skin: SkinBoton = DndSkins.Botones.principal,
) {
    DndBoton(
        onClick = { onClick?.invoke() },
        modifier = modifier,
        skin = skin,
        enabled = onClick != null,
    ) {
        Text(texto)
    }
}
