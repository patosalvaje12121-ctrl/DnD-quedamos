
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
import com.example.dnd_quedamos.features.usuario.domain.usecase.UserPermissionsUseCase

data class UsuarioUiState(
    val dataUsuario: DataUsuario? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val esAdmin: Boolean? = null,
    val loadingPermisos: Boolean = false
)

class UsuarioViewModel(
    private val userID: Int,
    private val managerUsuarioUseCase: ManagerUsuarioUseCase,
    private val userPermissionsUseCase: UserPermissionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    fun inicializar() {
        cargarUsuario(userID)
        comprobarPermisos()
    }

    fun onAction(action: UsuarioAction) {
        when (action) {
            is UsuarioAction.Mostrar -> cargarUsuario(action.id)
            is UsuarioAction.Crear -> crearUsuario(action.usuario)
            is UsuarioAction.Editar -> editarUsuario(action.usuario)
            is UsuarioAction.Eliminar -> eliminarUsuario(action.id)
        }
    }

    private fun comprobarPermisos() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    esAdmin = null,
                    loadingPermisos = true
                )
            }

            //Ponemos esAdmin a null mientras no haya un resultado valido
            //para evitar conservar permisos anteriores


            try {
                val esAdministrador = userPermissionsUseCase.esAdmin()

                _uiState.update {
                    it.copy(
                        esAdmin = esAdministrador,
                        loadingPermisos = false
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        esAdmin = null,
                        loadingPermisos = false
                    )
                }
            }
        }
    }


    private fun cargarUsuario(id: Int) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    loading = true,
                    error = null,
                    mensaje = null,
                    dataUsuario = null
                )
            }

            try {
                managerUsuarioUseCase.MostrarUsuario(id)
                    .onSuccess { usuario ->
                        _uiState.update {
                            it.copy(
                                dataUsuario = usuario,
                                loading = false
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                loading = false,
                                error = exception.message
                            )
                        }
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun crearUsuario(usuario: DataUsuario) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    loading = true,
                    error = null,
                    mensaje = null
                )
            }

            try {
                managerUsuarioUseCase.CrearUsuario(usuario)
                    .onSuccess { usuarioCreado ->
                        _uiState.update {
                            it.copy(
                                dataUsuario = usuarioCreado,
                                loading = false,
                                mensaje = "Usuario creado correctamente"
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                loading = false,
                                error = exception.message
                            )
                        }
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun editarUsuario(usuario: DataUsuario) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    loading = true,
                    error = null,
                    mensaje = null
                )
            }

            try {
                managerUsuarioUseCase.EditarUsuario(usuario)
                    .onSuccess { usuarioEditado ->
                        _uiState.update {
                            it.copy(
                                dataUsuario = usuarioEditado,
                                loading = false,
                                mensaje = "Usuario editado correctamente"
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                loading = false,
                                error = exception.message
                            )
                        }
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun eliminarUsuario(id: Int) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    loading = true,
                    error = null,
                    mensaje = null
                )
            }

            try {
                managerUsuarioUseCase.EliminarUsuario(id)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                dataUsuario = null,
                                loading = false,
                                mensaje = "Usuario eliminado correctamente"
                            )
                        }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(
                                loading = false,
                                error = exception.message
                            )
                        }
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}
