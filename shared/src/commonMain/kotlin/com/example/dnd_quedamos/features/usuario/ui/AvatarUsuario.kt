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
import com.example.dnd_quedamos.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// imagenId 0 = sin foto
val avataresUsuario: Map<Int, DrawableResource> = mapOf(
    1 to Res.drawable.icono_perfil_1,
    2 to Res.drawable.icono_perfil_3,
    3 to Res.drawable.icono_perfil_4,
    4 to Res.drawable.icono_perfil_2,
    5 to Res.drawable.icono_perfil_5,
    6 to Res.drawable.icono_perfil_6,
    7 to Res.drawable.icono_perfil_7,
)

@Composable
fun AvatarUsuario(
    imagenId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
) {
    val recurso = avataresUsuario[imagenId]
    val forma = modifier.size(size).clip(CircleShape)

    if (recurso != null) {
        Image(
            painter = painterResource(recurso),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = forma
        )
    } else
    {
        Box(forma.background(MaterialTheme.colorScheme.inversePrimary ))
    }

}
