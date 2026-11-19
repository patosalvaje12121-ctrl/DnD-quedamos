package com.example.dnd_quedamos.features.usuario.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.usuario.domain.usecase.RegisterUserUseCase
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUserUiState(
    val nombre: String = "", val descripcion: String = "", val password: String = "", val repetirPassword: String = "",
    val guardando: Boolean = false, val completado: Boolean = false, val error: String? = null
)
sealed interface RegisterUserAction {
    data class CambiarNombre(val valor: String): RegisterUserAction
    data class CambiarDescripcion(val valor: String): RegisterUserAction
    data class CambiarPassword(val valor: String): RegisterUserAction
    data class CambiarRepetirPassword(val valor: String): RegisterUserAction
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
            is RegisterUserAction.CambiarPassword -> _uiState.update { it.copy(password = action.valor) }
            is RegisterUserAction.CambiarRepetirPassword -> _uiState.update { it.copy(repetirPassword = action.valor) }
            RegisterUserAction.Registrar -> registrar()
        }
    }
    private fun registrar() {
        val estado = _uiState.value
        if (estado.guardando) return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, completado = false, error = null) }
            try {
                useCase.registrar(estado.nombre, estado.descripcion, estado.password, estado.repetirPassword)
                _uiState.update { it.copy(guardando = false, completado = true, password = "", repetirPassword = "") }
            } catch (e: CancellationException) { throw e }
            catch (e: ClientRequestException) {
                val mensaje = if (e.response.status == HttpStatusCode.Conflict)
                    "Ese nombre de usuario ya está en uso" else "No se pudo completar el registro"
                _uiState.update { it.copy(guardando = false, error = mensaje) }
            }
            catch (e: Exception) { _uiState.update { it.copy(guardando = false, error = e.message) } }
        }
    }
}
