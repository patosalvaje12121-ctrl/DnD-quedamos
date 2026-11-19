package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.usuario.presentation.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ViewUserRoot(
    iD: Int,
    modifier: Modifier = Modifier,
    viewModel: ViewUserViewModel = koinViewModel { parametersOf(iD) },
    editViewModel: EditUserViewModel = koinViewModel(),
    registerViewModel: RegisterUserViewModel = koinViewModel(),
    loginViewModel: LoginUserViewModel = koinViewModel()
) {
    val perfil by viewModel.uiState.collectAsStateWithLifecycle()
    val edicion by editViewModel.uiState.collectAsStateWithLifecycle()
    val registro by registerViewModel.uiState.collectAsStateWithLifecycle()
    val login by loginViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.inicializar() }
    LaunchedEffect(edicion.guardado) {
        if (edicion.guardado && perfil.pagina == UserPage.EDITAR) viewModel.inicializar()
    }
    LaunchedEffect(registro.completado) {
        if (registro.completado && perfil.pagina == UserPage.REGISTRO) viewModel.inicializar()
    }
    LaunchedEffect(login.completado) {
        if (login.completado && perfil.pagina == UserPage.LOGIN) viewModel.inicializar()
    }

    when (perfil.pagina) {
        UserPage.EDITAR -> EditUserScreen(
            state = edicion,
            onAction = editViewModel::onAction,
            onVolver = viewModel::volverPerfil,
            modifier = modifier
        )
        UserPage.REGISTRO -> RegisterUserScreen(
            state = registro,
            onAction = registerViewModel::onAction,
            onVolver = viewModel::volverPerfil,
            modifier = modifier
        )
        UserPage.LOGIN -> LoginUserScreen(
            state = login,
            onAction = loginViewModel::onAction,
            onVolver = viewModel::volverPerfil,
            modifier = modifier
        )
        UserPage.PERFIL -> UserDetailsScreen(
            viewModel = perfil,
            onRegistrarse = { registerViewModel.preparar(); viewModel.abrirRegistro() },
            onIniciarSesion = { loginViewModel.preparar(); viewModel.abrirLogin() },
            onEditarUsuario = {
                perfil.dataUsuario?.let { usuario ->
                    editViewModel.preparar(usuario)
                    viewModel.abrirEdicion()
                }
            },
            modifier = modifier
        )
    }
}
