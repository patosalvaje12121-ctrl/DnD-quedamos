package com.example.dnd_quedamos.features.usuario.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.usecase.ViewUserUseCase
import com.example.dnd_quedamos.features.usuario.domain.usecase.UserPermissionsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserPage { PERFIL, EDITAR, REGISTRO, LOGIN }

data class ViewUserUiState(
    val dataUsuario: DataUsuario? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val sinSesion: Boolean = false,
    val esPerfilPropio: Boolean = false,
    val esAdmin: Boolean? = null,
    val esAdminPerfil: Boolean? = null, // Consulta pública pendiente
    val pagina: UserPage = UserPage.PERFIL
)

class ViewUserViewModel(
    private val userID: Int,
    private val viewUserUseCase: ViewUserUseCase,
    private val userPermissionsUseCase: UserPermissionsUseCase,
    private val session: CurrentUserProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(ViewUserUiState())
    val uiState = _uiState.asStateFlow()

    fun inicializar() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, pagina = UserPage.PERFIL) }
            try {
                val actual = session.currentUser()
                if (userID == -1 && actual == null) {
                    _uiState.update { it.copy(dataUsuario = null, sinSesion = true,
                        esPerfilPropio = false, esAdmin = null, esAdminPerfil = null, loading = false) }
                    return@launch
                }
                val idPerfil = if (userID == -1) actual?.id ?: return@launch else userID
                val propio = actual?.id == idPerfil
                val perfil = viewUserUseCase.obtenerPerfil(idPerfil)
                _uiState.update { it.copy(dataUsuario = perfil, sinSesion = false,
                    esPerfilPropio = propio, esAdmin = null, esAdminPerfil = null, loading = false) }
                if (propio) {
                    // La consulta de permisos no debe impedir mostrar el perfil.
                    try {
                        val admin = userPermissionsUseCase.esAdmin()
                        _uiState.update { it.copy(esAdmin = admin) }
                    } catch (e: CancellationException) { throw e }
                    catch (_: Exception) { _uiState.update { it.copy(esAdmin = null) } }
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _uiState.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun abrirEdicion() {
        if (_uiState.value.esPerfilPropio && _uiState.value.dataUsuario != null)
            _uiState.update { it.copy(pagina = UserPage.EDITAR) }
    }
    fun abrirRegistro() {
        if (_uiState.value.sinSesion) _uiState.update { it.copy(pagina = UserPage.REGISTRO) }
    }
    fun abrirLogin() {
        if (_uiState.value.sinSesion) _uiState.update { it.copy(pagina = UserPage.LOGIN) }
    }
    fun volverPerfil() { _uiState.update { it.copy(pagina = UserPage.PERFIL) } }
}
