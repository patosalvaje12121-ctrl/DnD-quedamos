package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SeleccionFotoDialog(
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit,
    onCerrar: () -> Unit
) {
    Dialog(onDismissRequest = onCerrar) {
        Card {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(avataresUsuario.keys.toList()) { id ->
                    AvatarUsuario(
                        imagenId = id,
                        size = 80.dp,
                        modifier = Modifier
                            .border(
                                width = 3.dp,
                                color = if (id == seleccionada) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSeleccionar(id).also { onCerrar() }  }
                    )
                }
            }
        }
    }
}
