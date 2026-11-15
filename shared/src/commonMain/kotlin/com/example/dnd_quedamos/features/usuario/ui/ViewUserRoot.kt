package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.usuario.domain.usecase.ManagerUsuarioUseCase
import com.example.dnd_quedamos.features.usuario.presentation.UsuarioViewModel
import com.example.dnd_quedamos.di.DefaultAppContainer
import androidx.compose.runtime.remember


@Composable
fun ViewUserRoot(
    iD: Int,
    modifier: Modifier = Modifier,
    viewModel: UsuarioViewModel
) {
    LaunchedEffect(viewModel) {
        viewModel.inicializar()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    UserDetailsScreen(
        viewModel = uiState,
        onAction = {},
        modifier = modifier
    )
}
