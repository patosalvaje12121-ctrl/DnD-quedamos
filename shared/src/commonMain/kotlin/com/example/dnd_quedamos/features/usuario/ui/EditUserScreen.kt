package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.features.usuario.presentation.EditUserAction
import com.example.dnd_quedamos.features.usuario.presentation.EditUserUiState

@Composable
fun EditUserScreen(state: EditUserUiState, onAction: (EditUserAction) -> Unit, onVolver: () -> Unit,
                   modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Editar usuario", style = MaterialTheme.typography.headlineSmall)
//        Text("TODO: implementar imagenes")
        OutlinedTextField(value = state.nombre,
            onValueChange = { onAction(EditUserAction.CambiarNombre(it)) },
            label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = state.descripcion,
            onValueChange = { onAction(EditUserAction.CambiarDescripcion(it)) },
            label = { Text("Descripción") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { onAction(EditUserAction.Guardar) },
            enabled = !state.guardando && state.nombre.isNotBlank() && state.nombre.length <= 50 && state.descripcion.length <= 500,
            modifier = Modifier.fillMaxWidth()) { Text(if (state.guardando) "Guardando..." else "Guardar cambios") }
        OutlinedButton(onClick = onVolver, enabled = !state.guardando, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}
