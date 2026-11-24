package com.example.dnd_quedamos.features.chat.ui

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.example.dnd_quedamos.core.ui.componentes.DndPanel
import com.example.dnd_quedamos.features.chat.presentation.ChatViewModel
import com.example.dnd_quedamos.features.chat.ui.ChatListScreen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ChatScreenRoot(
    onBack: () -> Unit,
    onNuevoChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator()
    val coroutine = rememberCoroutineScope()
    val viewModel = koinViewModel<ChatViewModel>(parameters = { parametersOf() })

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
                        modifier = modifier
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
                        onBack = onBack,
                    )
                else
                    Text("Uh, oh. There was a problem loading the chat")
            }
        }
    )

}