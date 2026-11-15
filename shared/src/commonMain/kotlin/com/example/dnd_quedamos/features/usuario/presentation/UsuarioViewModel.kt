
package com.example.dnd_quedamos.features.usuario.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.usecase.ManagerUsuarioUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UsuarioUiState(
    val dataUsuario: DataUsuario? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class UsuarioViewModel(
    private val userID: Int,
    private val managerUsuarioUseCase: ManagerUsuarioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    fun inicializar() {

        viewModelScope.launch {

            // Comenzamos a cargar el usuario
            _uiState.update {
                it.copy(
                    loading = true,
                    error = null,
                    dataUsuario = null
                )
            }

            try {
                val resultado = managerUsuarioUseCase.MostrarUsuario(userID)

                resultado
                    .onSuccess { usuario ->
                        _uiState.update {
                            it.copy(
                                dataUsuario = usuario,
                                loading = false,
                                error = null
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                dataUsuario = null,
                                loading = false,
                                error = exception.message
                                    ?: "Error al obtener el usuario"
                            )
                        }
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        dataUsuario = null,
                        loading = false,
                        error = e.message
                            ?: "Error inesperado"
                    )
                }
            }
        }
    }
}
