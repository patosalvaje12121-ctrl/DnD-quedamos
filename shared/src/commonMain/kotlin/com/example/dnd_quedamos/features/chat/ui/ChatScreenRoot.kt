package com.example.dnd_quedamos.features.chat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.core.ui.componentes.DndPanel
import com.example.dnd_quedamos.features.chat.presentation.ChatListUiState
import com.example.dnd_quedamos.features.chat.presentation.ChatListViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ChatScreenRoot(
    onNuevoChat: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val navigator = rememberListDetailPaneScaffoldNavigator()
    val coroutine = rememberCoroutineScope()


    val value = navigator.scaffoldValue;
    val isSmall = (value[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded) && value[ListDetailPaneScaffoldRole.Detail] != PaneAdaptedValue.Expanded


    LaunchedEffect(Unit) { viewModel.cargar() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var initialNavigationDone by rememberSaveable { mutableStateOf(false) }

    if(!isSmall)
    LaunchedEffect(uiState) {
        if (!initialNavigationDone && uiState is ChatListUiState.Listo && (uiState as ChatListUiState.Listo).chats.isNotEmpty()) {
            initialNavigationDone = true
            coroutine.launch {
                navigator.navigateTo(
                    ListDetailPaneScaffoldRole.Detail,
                    (uiState as ChatListUiState.Listo).chats.first().id,
                )
            }
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                DndPanel (
                    modifier = modifier
                ) {

                    ChatListScreen(
                        onAbrirChat = { newChatId ->
                            coroutine.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, newChatId)
                            }
                        },
                        onNuevoChat = onNuevoChat,
                        modifier = modifier,
                        viewModel = viewModel,
                    )
                }
            }
        },
        detailPane = {
            AnimatedPane {
                val id = navigator.currentDestination?.contentKey;
                if(id != null)
                    ChatView(
                        chatId = id as Int,
                        showBack = !isSmall,
                        onBack = { coroutine.launch {
                            navigator.navigateBack()
                        } },
                    )
                else
                {
                    Box (
                        Modifier.fillMaxSize()
                    ) {

                        Text("Chat is loading ...", modifier.align(Alignment.Center))
                    }

                }
            }
        }
    )

}
// System Usability Scale
// UX questioner (S)
/// NASA TLX