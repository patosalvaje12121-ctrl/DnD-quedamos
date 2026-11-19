package com.example.dnd_quedamos.features.usuario.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.features.usuario.domain.usecase.LoginUserUseCase
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUserUiState(
    val idTexto: String = "", val password: String = "", val loading: Boolean = false,
    val completado: Boolean = false, val error: String? = null
)
sealed interface LoginUserAction {
    data class CambiarId(val valor: String): LoginUserAction
    data class CambiarPassword(val valor: String): LoginUserAction
    data object IniciarSesion: LoginUserAction
}
class LoginUserViewModel(private val useCase: LoginUserUseCase): ViewModel() {
    private val _uiState = MutableStateFlow(LoginUserUiState())
    val uiState = _uiState.asStateFlow()
    fun preparar() { _uiState.value = LoginUserUiState() }
    fun onAction(action: LoginUserAction) {
        when(action) {
            is LoginUserAction.CambiarId -> _uiState.update { it.copy(idTexto = action.valor) }
            is LoginUserAction.CambiarPassword -> _uiState.update { it.copy(password = action.valor) }
            LoginUserAction.IniciarSesion -> iniciar()
        }
    }
    private fun iniciar() {
        val estado = _uiState.value
        if (estado.loading) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, completado = false, error = null) }
            try {
                useCase.iniciarSesion(estado.idTexto.toIntOrNull() ?: 0, estado.password)
                _uiState.update { it.copy(loading = false, completado = true, password = "") }
            } catch (e: CancellationException) { throw e }
            catch (e: ClientRequestException) {
                val mensaje = if (e.response.status == HttpStatusCode.Unauthorized)
                    "ID o contraseña incorrectos" else "Error al iniciar sesión"
                _uiState.update { it.copy(loading = false, error = mensaje) }
            } catch (e: Exception) { _uiState.update { it.copy(loading = false, error = e.message) } }
        }
    }
}
