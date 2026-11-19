package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.features.usuario.presentation.RegisterUserAction
import com.example.dnd_quedamos.features.usuario.presentation.RegisterUserUiState

@Composable
fun RegisterUserScreen(state: RegisterUserUiState, onAction: (RegisterUserAction) -> Unit,
                       onVolver: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Registrarse", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = state.nombre,
            onValueChange = { onAction(RegisterUserAction.CambiarNombre(it)) },
            label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = state.descripcion,
            onValueChange = { onAction(RegisterUserAction.CambiarDescripcion(it)) },
            label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = state.password,
            onValueChange = { onAction(RegisterUserAction.CambiarPassword(it)) },
            label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = state.repetirPassword,
            onValueChange = { onAction(RegisterUserAction.CambiarRepetirPassword(it)) },
            label = { Text("Repetir contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { onAction(RegisterUserAction.Registrar) },
            enabled = !state.guardando && state.nombre.isNotBlank() && state.nombre.length <= 50 &&
                    state.descripcion.length <= 500 && state.password.isNotBlank() &&
                    state.password == state.repetirPassword,
            modifier = Modifier.fillMaxWidth()) { Text(if (state.guardando) "Registrando..." else "Crear cuenta") }
        OutlinedButton(onClick = onVolver, enabled = !state.guardando) { Text("Cancelar") }
    }
}
