package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import com.example.dnd_quedamos.features.usuario.domain.usecase.ManagerUsuarioUseCase
import com.example.dnd_quedamos.features.usuario.presentation.UsuarioUiState
import com.example.dnd_quedamos.features.usuario.presentation.UsuarioViewModel

@Composable
fun UserDetailsScreen(
    viewModel: UsuarioUiState,
    onAction: () -> Unit
) {
   Box {  viewModel.dataUsuario  }
}