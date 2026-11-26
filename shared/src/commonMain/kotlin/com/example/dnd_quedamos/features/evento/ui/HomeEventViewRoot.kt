package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.isSpecified
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
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
    onAbrirDesc: (eventoId: Int) -> Unit = {},
    onAbrirChat: (eventoId: Int) -> Unit = {},
    viewModel: EventoViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) { viewModel.inicializar() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarFiltro by remember { mutableStateOf(false) }

    if (mostrarFiltro) {
        // reutilizamos el formulario de crear evento para filtrar
        Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = { mostrarFiltro = false }) { Text("← Volver") }
            Formulario(
                original = null,
                error = null,
                onGuardar = { campos ->
                    viewModel.filtrar(campos)
                    mostrarFiltro = false
                },
                titulo = "Filtrar eventos",
                textoBoton = "Filtrar",
            )
        }
        return
    }

    val bgImage = imageResource(DndSkins.fondoMosaico)
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
            columns = StaggeredGridCells.Adaptive(minSize = 448.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val dy = scrollOffset.mod(tileHeightPx)
                    translate(top = (-dy)) {
                        drawRect(
                            brush = tileBrush,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height + tileHeightPx),
                        )
                    }
                },
            contentPadding = PaddingValues(16.dp),
            verticalItemSpacing = 12.dp
        ) {
            items(
                items = uiState.dataEventos,
                key = { it.id },
            ) { evento ->
                EventoCard(evento = evento, onAbrirDesc = onAbrirDesc, onAbrirChat = onAbrirChat)
            }
        }
    }
}
private const val LINEAS_TITULO = 2
private const val LINEAS_DESCRIPCION = 4

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventoCard(
    evento: DataEvento,
    onAbrirDesc: (eventoId: Int) -> Unit = {},
    onAbrirChat: (eventoId: Int) -> Unit = {},
) {
    val painter = painterResource(DndSkins.panelMapa.res)
    val ratioFondo = painter.intrinsicSize.let { s ->
        if (s.isSpecified && s.width > 0f && s.height > 0f) s.width / s.height else 1f
    }

    // Aspect ratio of the loaded photo; only used inside the card, never to size it
    var ratioImagen by remember(evento.id) { mutableStateOf(1f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onClick( onClick = { onAbrirDesc(evento.id) }),
        colors = CardColors(
            containerColor = Color.Transparent,
            contentColor = CardDefaults.cardColors().contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratioFondo)                                 // card shape = background shape
                .paint(painter, contentScale = ContentScale.FillBounds), // same ratio, so nothing is distorted
        ) {
            val scale = maxWidth.value / BASE_CARD_WIDTH_DP
            val titleSize = (BASE_TITLE_SP * scale).sp
            val bodySize = (BASE_BODY_SP * scale).sp
            val paddingH = (BASE_PADDING_H * scale).dp
            val paddingV = (BASE_PADDING_V * scale).dp
            val gap = (BASE_GAP * scale).dp

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
                    color = MaterialTheme.colorScheme.background,
                    maxLines = LINEAS_TITULO,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(gap))
                Text(
                    text = evento.descripcion,
                    fontSize = bodySize,
                    lineHeight = 1.3.em,
                    color = MaterialTheme.colorScheme.background,
                    maxLines = LINEAS_DESCRIPCION,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(gap))

                // Takes all the remaining height; the photo is fitted inside it
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data("https://picsum.photos/id/1/${evento.id_image}/${evento.id_image}")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen de Evento",
                        contentScale = ContentScale.Fit,
                        onSuccess = { estado ->
                            val s = estado.painter.intrinsicSize
                            if (s.isSpecified && s.height > 0f) ratioImagen = s.width / s.height
                        },
                        modifier = Modifier
                            .aspectRatio(ratioImagen)   // largest size with the photo's own ratio that fits
                            .clip(RoundedCornerShape(2.dp)),
                    )
                }

                Spacer(Modifier.height(gap * 0.5f))
                TextButton(onClick = { onAbrirChat(evento.id) }) {
                    Text("💬 Chat", fontSize = bodySize)
                }
            }
        }
    }
}