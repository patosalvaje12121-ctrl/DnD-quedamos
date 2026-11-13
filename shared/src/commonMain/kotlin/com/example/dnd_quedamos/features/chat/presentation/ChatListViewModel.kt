package com.example.dnd_quedamos.features.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.chat.domain.model.DataChat
import com.example.dnd_quedamos.features.chat.domain.usecase.ManagerChatUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChatListUiState {
    data object Cargando : ChatListUiState
    data class Listo(val chats: List<DataChat>) : ChatListUiState
    data class Error(val mensaje: String) : ChatListUiState
}

class ChatListViewModel(
    private val useCase: ManagerChatUseCase,
    private val currentUser: CurrentUserProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Cargando)
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private var carga: Job? = null

    /**
     * Carga (o recarga) la lista. Se llama cada vez que la pantalla aparece, para que al volver
     * de un chat la vista previa muestre el último mensaje. Si ya había lista, se recarga
     * "en silencio" sin volver a mostrar el spinner.
     */
    fun cargar() {
        if (carga?.isActive == true) return
        carga = viewModelScope.launch {
            if (_uiState.value !is ChatListUiState.Listo) _uiState.value = ChatListUiState.Cargando

            val userId = currentUser.currentUser()?.id
            if (userId == null) {
                _uiState.value = ChatListUiState.Error("Inicia sesión para ver tus chats")
                return@launch
            }
            useCase.getChats(userId)
                .onSuccess { _uiState.value = ChatListUiState.Listo(it) }
                .onFailure {
                    // Si ya teníamos datos, mejor seguir mostrándolos que tapar la lista con un error
                    if (_uiState.value !is ChatListUiState.Listo) {
                        _uiState.value = ChatListUiState.Error("No se pudieron cargar los chats")
                    }
                }
        }
    }
}
