package com.example.dnd_quedamos.features.usuario.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.usuario.domain.usecase.RegisterUserUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUserUiState(
    val nombre: String = "", val descripcion: String = "",
    val guardando: Boolean = false, val completado: Boolean = false, val error: String? = null
)
sealed interface RegisterUserAction {
    data class CambiarNombre(val valor: String): RegisterUserAction
    data class CambiarDescripcion(val valor: String): RegisterUserAction
    data object Registrar: RegisterUserAction
}
class RegisterUserViewModel(private val useCase: RegisterUserUseCase): ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUserUiState())
    val uiState = _uiState.asStateFlow()
    fun preparar() { _uiState.value = RegisterUserUiState() }
    fun onAction(action: RegisterUserAction) {
        when(action) {
            is RegisterUserAction.CambiarNombre -> _uiState.update { it.copy(nombre = action.valor) }
            is RegisterUserAction.CambiarDescripcion -> _uiState.update { it.copy(descripcion = action.valor) }
            RegisterUserAction.Registrar -> registrar()
        }
    }
    private fun registrar() {
        val estado = _uiState.value
        if (estado.guardando) return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, completado = false, error = null) }
            try {
                useCase.registrar(estado.nombre, estado.descripcion)
                _uiState.update { it.copy(guardando = false, completado = true) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(guardando = false, error = e.message) } }
        }
    }
}
