
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
        onAction = viewModel::onAction,
        modifier = modifier
    )
}
