package com.example.dnd_quedamos.features.usuario.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.usecase.EditUserUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditUserUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val error: String? = null
)

sealed interface EditUserAction {
    data class CambiarNombre(val valor: String): EditUserAction
    data class CambiarDescripcion(val valor: String): EditUserAction
    data object Guardar: EditUserAction
}

class EditUserViewModel(private val editUserUseCase: EditUserUseCase): ViewModel() {
    private val _uiState = MutableStateFlow(EditUserUiState())
    val uiState = _uiState.asStateFlow()
    private var usuarioOriginal: DataUsuario? = null

    fun preparar(usuario: DataUsuario) {
        usuarioOriginal = usuario
        _uiState.value = EditUserUiState(nombre = usuario.nombre, descripcion = usuario.descBio)
    }
    fun onAction(action: EditUserAction) {
        when(action) {
            is EditUserAction.CambiarNombre -> _uiState.update { it.copy(nombre = action.valor) }
            is EditUserAction.CambiarDescripcion -> _uiState.update { it.copy(descripcion = action.valor) }
            EditUserAction.Guardar -> guardar()
        }
    }
    private fun guardar() {
        val estado = _uiState.value
        val original = usuarioOriginal ?: return
        if (estado.guardando) return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, guardado = false, error = null) }
            try {
                editUserUseCase.editar(original, estado.nombre, estado.descripcion)
                _uiState.update { it.copy(guardando = false, guardado = true) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(guardando = false, error = e.message) } }
        }
    }
}
