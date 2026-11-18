package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import com.example.dnd_quedamos.resources.BG
import com.example.dnd_quedamos.resources.Map
import com.example.dnd_quedamos.resources.Res
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeEventViewRoot(
    modifier: Modifier = Modifier,
    onAbrirChat: (eventoId: Int) -> Unit = {},
    viewModel: EventoViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) { viewModel.inicializar() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val bgImage = imageResource(Res.drawable.BG)
    val listState = rememberLazyListState()

    val eventBGImage = imageResource(Res.drawable.Map)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .drawBehind(
            //color = Color(0x6b1119))
                {
                    drawRect(
                        color = Color(0x6b1119),
                        topLeft = Offset.Zero,
                        // extra tileHeight guarantees the top isn't left uncovered
                        size = Size(size.width, size.height),
                    )
                }
            )
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val scale = widthPx / bgImage.width

        val tileBrush = remember(bgImage, widthPx) {
            ShaderBrush(
                ImageShader(
                    image = bgImage,
                    tileModeX = TileMode.Clamp,
                    tileModeY = TileMode.Repeated,
                )
            ).apply {
                transform = Matrix().apply { scale(scale, scale) }
            }
        }

        val eventBrush = ShaderBrush( ImageShader( image = eventBGImage));

        // Tile period in pixels after the scale has been applied
        val tileHeightPx = bgImage.height * scale

        val scrollOffset by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val first = info.visibleItemsInfo.firstOrNull() ?: return@derivedStateOf 0f
                val avgSize = if (info.visibleItemsInfo.isNotEmpty()) {
                    info.visibleItemsInfo.sumOf { it.size }.toFloat() /
                            info.visibleItemsInfo.size
                } else 0f
                first.index * avgSize - first.offset
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Wrap into [0, tileHeightPx) so the translate stays small
                    val dy = scrollOffset.mod(tileHeightPx)
                    translate(top = (-dy).toFloat()) {
                        drawRect(
                            brush = tileBrush,
                            topLeft = Offset.Zero,
                            // extra tileHeight guarantees the top isn't left uncovered
                            size = Size(size.width, size.height + tileHeightPx),
                        )
                    }
                },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.dataEventos) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardColors(
                        containerColor = Color.Transparent,
                        contentColor = CardDefaults.cardColors().contentColor,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = with (Modifier) {
                            fillMaxSize()
                                .paint(
                                    // Replace with your image id
                                    painterResource(Res.drawable.Map),
                                    contentScale = ContentScale.FillWidth)
                    }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(evento.nombre, fontWeight = FontWeight.Bold)
                            Text(evento.descripcion, fontSize = 14.sp)
                            TextButton(onClick = { onAbrirChat(evento.id) }) {
                                Text("💬 Chat")
                            }
                        }
                    }
                }
            }
        }
    }
}