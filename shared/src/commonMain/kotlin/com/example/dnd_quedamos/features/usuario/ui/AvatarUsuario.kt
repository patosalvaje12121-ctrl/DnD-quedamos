package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.resources.Res
import com.example.dnd_quedamos.resources.avatar_1
import com.example.dnd_quedamos.resources.avatar_2
import com.example.dnd_quedamos.resources.avatar_3
import com.example.dnd_quedamos.resources.avatar_4
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// imagenId 0 = sin foto
val avataresUsuario: Map<Int, DrawableResource> = mapOf(
    1 to Res.drawable.avatar_1,
    2 to Res.drawable.avatar_2,
    3 to Res.drawable.avatar_3,
    4 to Res.drawable.avatar_4,
)

@Composable
fun AvatarUsuario(
    imagenId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp
) {
    val recurso = avataresUsuario[imagenId]
    val forma = modifier.size(size).clip(CircleShape)
    if (recurso != null) {
        Image(
            painter = painterResource(recurso),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = forma
        )
    } else {
        Box(forma.background(MaterialTheme.colorScheme.primaryContainer))
    }
}
