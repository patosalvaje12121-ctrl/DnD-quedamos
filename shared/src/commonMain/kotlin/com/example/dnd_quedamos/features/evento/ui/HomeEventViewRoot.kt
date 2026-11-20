package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.dnd_quedamos.customScheme
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import com.example.dnd_quedamos.resources.BG
import com.example.dnd_quedamos.resources.Map
import com.example.dnd_quedamos.resources.Res
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

private const val BASE_CARD_WIDTH_DP = 220f
private const val BASE_TITLE_SP      = 8f
private const val BASE_BODY_SP       = 6f
private const val BASE_PADDING_H     = 10f
private const val BASE_PADDING_V     = 14f
private const val BASE_GAP           = 8f

@Composable
fun HomeEventViewRoot(
    modifier: Modifier = Modifier,
    onAbrirChat: (eventoId: Int) -> Unit = {},
    viewModel: EventoViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) { viewModel.inicializar() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val bgImage = imageResource(Res.drawable.BG)
    val gridState = rememberLazyStaggeredGridState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .drawBehind
            {
                drawRect(
                    color = Color(0x6b1119),
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                )
            }
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

        val tileHeightPx = bgImage.height * scale

        val scrollOffset by remember {
            derivedStateOf {
                val info = gridState.layoutInfo
                val first = info.visibleItemsInfo.firstOrNull() ?: return@derivedStateOf 0f
                val avgHeight = if (info.visibleItemsInfo.isNotEmpty()) {
                    info.visibleItemsInfo.sumOf { it.size.height }.toFloat() /
                            info.visibleItemsInfo.size
                } else 0f
                first.index * avgHeight - first.offset.y
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 512.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val dy = scrollOffset.mod(tileHeightPx)
                    translate(top = (-dy).toFloat()) {
                        drawRect(
                            brush = tileBrush,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height + tileHeightPx),
                        )
                    }
                },
            contentPadding = PaddingValues(16.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = uiState.dataEventos,
                key = { it.id },
            ) { evento ->
                EventoCard(evento = evento, onAbrirChat = onAbrirChat)
            }
        }
    }
}

@Composable
private fun EventoCard(
    evento: DataEvento,
    onAbrirChat: (eventoId: Int) -> Unit = {},
) {
    val painter = painterResource(Res.drawable.Map)

    val imageRatio = painter.intrinsicSize.let { s ->
        if (s.width > 0f && s.height > 0f) s.width / s.height else 1f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardColors(
            containerColor = Color.Transparent,
            contentColor = CardDefaults.cardColors().contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(imageRatio)
                .paint(painter = painter, contentScale = ContentScale.Crop),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val scale = maxWidth.value / BASE_CARD_WIDTH_DP

                val titleSize = (BASE_TITLE_SP   * scale).sp
                val bodySize  = (BASE_BODY_SP    * scale).sp
                val paddingH  = (BASE_PADDING_H  * scale).dp
                val paddingV  = (BASE_PADDING_V  * scale).dp
                val gap       = (BASE_GAP        * scale).dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = paddingH, vertical = paddingV),
                ) {
                    Text(
                        text = evento.nombre,
                        fontSize = titleSize,
                        lineHeight = 1.2.em,
                        fontWeight = FontWeight.Bold,
                        color = customScheme().background,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(gap))
                    Text(
                        text = evento.descripcion,
                        fontSize = bodySize,
                        lineHeight = 1.3.em,
                        color = customScheme().background,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(gap))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data("https://picsum.photos/id/${evento.id_image}/1200/1200")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen de Evento",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                    Spacer(Modifier.height(gap * 0.5f))
                    TextButton(onClick = { onAbrirChat(evento.id) }) {
                        Text("💬 Chat", fontSize = bodySize)
                    }
                }
            }
        }
    }
}