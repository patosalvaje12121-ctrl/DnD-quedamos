package com.example.dnd_quedamos.features.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.chat.domain.model.DataContacto
import com.example.dnd_quedamos.features.chat.domain.usecase.ManagerChatUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NuevoChatUiState(
    val contactos: List<DataContacto> = emptyList(), // todos (sin mí)
    val busqueda: String = "",
    val cargando: Boolean = true,
    val error: String? = null,
) {
    /** Lo que se ve en la lista: los contactos cuyo nombre contiene el texto buscado. */
    val filtrados: List<DataContacto>
        get() = contactos.filter { it.nombre.contains(busqueda.trim(), ignoreCase = true) }
}

/** Pantalla "Nuevo chat": carga los usuarios una vez y filtra por nombre en local. */
class NuevoChatViewModel(
    private val useCase: ManagerChatUseCase,
    private val currentUser: CurrentUserProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevoChatUiState())
    val uiState: StateFlow<NuevoChatUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }

            val yo = currentUser.currentUser()?.id
            if (yo == null) {
                _uiState.update { it.copy(cargando = false, error = "Inicia sesión para empezar un chat") }
                return@launch
            }
            useCase.getContactos(yo)
                .onSuccess { lista -> _uiState.update { it.copy(contactos = lista, cargando = false) } }
                .onFailure { _uiState.update { it.copy(cargando = false, error = "No se pudieron cargar los usuarios") } }
        }
    }

    fun onBusquedaChange(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
    }
}
