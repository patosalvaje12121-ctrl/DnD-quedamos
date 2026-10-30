package com.example.dnd_quedamos.features.usuario.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.usecase.ManagerUsuarioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsuarioUiState(
    val dataUsuario: DataUsuario = DataUsuario(-2, "", 0, "", listOf()),
    val loading: Boolean = false
)


class UsuarioViewModel(
    private val userID : Int,
    private val managerUsuarioUseCase: ManagerUsuarioUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioUiState() )
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()



    fun inicializar(){

        viewModelScope.launch {
            _uiState.update{it.copy(loading=true)}
            managerUsuarioUseCase.MostrarUsuario(userID)
                .onSuccess {
                        ret ->
                    _uiState.update {
                        it.copy(dataUsuario = ret, loading = false)
                    }
                }
                .onFailure { exception -> /*throw exception*/ }
        }

    }

}