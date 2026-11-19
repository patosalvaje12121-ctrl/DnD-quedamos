package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.features.usuario.presentation.LoginUserAction
import com.example.dnd_quedamos.features.usuario.presentation.LoginUserUiState

@Composable
fun LoginUserScreen(state: LoginUserUiState, onAction: (LoginUserAction) -> Unit,
                    onVolver: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = state.idTexto,
            onValueChange = { onAction(LoginUserAction.CambiarId(it)) },
            label = { Text("ID de usuario") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = state.password,
            onValueChange = { onAction(LoginUserAction.CambiarPassword(it)) },
            label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { onAction(LoginUserAction.IniciarSesion) },
            enabled = !state.loading && (state.idTexto.toIntOrNull() ?: 0) > 0 && state.password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()) { Text(if (state.loading) "Entrando..." else "Iniciar sesión") }
        OutlinedButton(onClick = onVolver, enabled = !state.loading) { Text("Cancelar") }
    }
}
