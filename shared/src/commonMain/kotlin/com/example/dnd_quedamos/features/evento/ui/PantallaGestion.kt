package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.border
import kotlin.time.ExperimentalTime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.dnd_quedamos.core.ui.componentes.DndBoton
import com.example.dnd_quedamos.core.ui.componentes.DndPanel
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import com.example.dnd_quedamos.core.ui.theme.DndColores
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.presentation.GestionViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PantallaGestion(
    onAbrirChat: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GestionViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.cargar() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var borrarId by remember { mutableStateOf<Int?>(null) }
    var editandoId by remember { mutableStateOf<Int>(-1) }

    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val scope = rememberCoroutineScope()

    val value = navigator.scaffoldValue;
    val isList = (value[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded)

    fun abrirVer(id: Int) {
        editandoId = -1
        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id) }
    }

    fun abrirFormulario(id: Int) {
        editandoId = id
        if (navigator.currentDestination?.contentKey != id) {
            scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id) }
        }
    }

    fun cerrarDetalle() {
        editandoId = -1
        scope.launch { navigator.navigateBack() }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                ListaGestion(
                    eventos = uiState.eventos,
                    error = uiState.error,
                    onCrear = { abrirFormulario(-1) },
                    onAbrir = { abrirVer(it) },
                    onBorrar = { borrarId = it },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val id = navigator.currentDestination?.contentKey
                when {
                    id == null -> {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(0.4f).border(4.dp, DndColores.tertiary,
                                    RoundedCornerShape(4.dp),
                                ),
                                contentAlignment = Alignment.Center,
                            ) { Text("Selecciona un evento o crea uno nuevo") }
                        }
                    }

                    id == -1 || editandoId == id -> {
                        EventDetailScope(id) {
                            PantallaFormularioEvento(
                                eventoId = id,
                                onBack = {
                                    if (id == -1) cerrarDetalle() else editandoId = -1
                                },
                                onGuardado = {
                                    viewModel.cargar()
                                    if (id == -1) cerrarDetalle() else editandoId = -1
                                },
                            )
                        }
                    }

                    else -> {
                        EventDetailScope(id) {
                            PantallaViewEvent(
                                eventoId = id,
                                onBack = { cerrarDetalle() },
                                onAbrirChat = { onAbrirChat(id) },
                                onEditar = { abrirFormulario(id) },
                                showBack = !isList
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier,
    )

    borrarId?.let { id ->
        ConfirmarBorrado(
            onConfirmar = {
                viewModel.borrar(id)
                if (navigator.currentDestination?.contentKey == id) cerrarDetalle()
                borrarId = null
            },
            onCancelar = { borrarId = null },
        )
    }
}

/**
 * Gives the detail pane a **fresh ViewModelStore + fresh composition subtree**
 * for each `contentKey`. Without this, `koinViewModel { parametersOf(id) }`
 * inside the pane returns the same cached VM forever, because
 * `ListDetailPaneScaffold` does not scope ViewModels per destination.
 */
@Composable
private fun EventDetailScope(
    id: Int,
    content: @Composable () -> Unit,
) {
    key(id) {
        val owner = remember(id) {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }
        DisposableEffect(owner) {
            onDispose { owner.viewModelStore.clear() }
        }
        CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            content()
        }
    }
}

// ListaGestion / EventCard unchanged from your current version

@Composable
private fun ListaGestion(
    eventos: List<DataEvento>,
    error: String?,
    onCrear: () -> Unit,
    onAbrir: (Int) -> Unit,
    onBorrar: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = onCrear, modifier = Modifier.fillMaxWidth()) {
            Text("+ Crear evento")
        }

        error?.let { Text("Error: $it") }
        if (eventos.isEmpty()) Text("No tienes eventos que gestionar")

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(eventos, key = { it.id }) { evento ->
                EventCard(
                    evento = evento,
                    onAbrir = { onAbrir(evento.id) },
                    onBorrar = { onBorrar(evento.id) },
                )
            }
        }
    }
}

@Composable
private fun EventCard(
    evento: DataEvento,
    onAbrir: () -> Unit,
    onBorrar: () -> Unit,
) {
    DndPanel(
        skin = DndSkins.filaChat,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAbrir), // tap anywhere on the card to open the view
        trailing = {
            DndBoton(
                onClick = onBorrar,
                skin = DndSkins.Botones.principal,
            ) {
                Text("Borrar")
            }
        },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(evento.nombre, fontWeight = FontWeight.Bold)
            Text(formatearFecha(evento.fecha))
        }
    }
}