
package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.usuario.presentation.UsuarioViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ViewUserRoot(
    iD: Int,
    modifier: Modifier = Modifier,
    onRegistrarse: (() -> Unit)? = null,
    onIniciarSesion: (() -> Unit)? = null,
    onEditarUsuario: (() -> Unit)? = null,
    viewModel: UsuarioViewModel = koinViewModel {
        parametersOf(iD)
    }
) {
    LaunchedEffect(viewModel) {
        viewModel.inicializar()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UserDetailsScreen(
        viewModel = uiState,
        onRegistrarse = onRegistrarse,
        onIniciarSesion = onIniciarSesion,
        onEditarUsuario = onEditarUsuario,
        modifier = modifier
    )
}
