@file:OptIn(ExperimentalTime::class)

package com.example.dnd_quedamos.features.evento.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.model.TipoActividad
import com.example.dnd_quedamos.features.evento.presentation.CamposEvento
import com.example.dnd_quedamos.features.evento.presentation.FormularioEventoViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.ExperimentalTime

/**
 * Formulario para crear (eventoId = -1) o editar un evento.
 * Al guardar bien llama a [onGuardado] (normalmente, volver atrás).
 */
@Composable
fun PantallaFormularioEvento(
    eventoId: Int,
    onBack: () -> Unit,
    onGuardado: () -> Unit,
    viewModel: FormularioEventoViewModel = koinViewModel { parametersOf(eventoId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Volver") }

        if (uiState.error != null && !uiState.listo) {
            Text("Error: ${uiState.error}")
        } else if (!uiState.listo) {
            Text("Cargando...")
        } else {
            Formulario(
                original = uiState.original,
                error = uiState.error,
                onGuardar = { campos -> viewModel.guardar(campos, onGuardado) },
            )
        }
    }
}

/**
 * Los campos del formulario. Si [original] no es null, empiezan rellenos con sus datos.
 * Tambien se usa en el feed para filtrar (con titulo y textoBoton cambiados).
 */
@Composable
fun Formulario(
    original: DataEvento?,
    error: String?,
    onGuardar: (CamposEvento) -> Unit,
    titulo: String = if (original == null) "Crear evento" else "Editar evento",
    textoBoton: String = "Guardar",
) {
    var nombre by remember { mutableStateOf(original?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(original?.descripcion ?: "") }
    var tipo by remember { mutableStateOf(original?.tipoActividad ?: TipoActividad.ROL) }
    var fecha by remember { mutableStateOf(original?.let { textoFecha(it.fecha) } ?: "") }
    var hora by remember { mutableStateOf(original?.let { textoHora(it.fecha) } ?: "") }
    var direccion by remember { mutableStateOf(original?.ubicacion?.direccion ?: "") }
    var latitud by remember { mutableStateOf(original?.ubicacion?.latitud?.toString() ?: "") }
    var longitud by remember { mutableStateOf(original?.ubicacion?.longitud?.toString() ?: "") }
    var plazas by remember { mutableStateOf(original?.maxParticipantes?.toString() ?: "") }

    Text(titulo)

    OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

    Text("Tipo:")
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TipoActividad.entries.forEach { t ->
            FilterChip(selected = tipo == t, onClick = { tipo = t }, label = { Text(t.name) })
        }
    }

    OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha (dd/MM/yyyy)") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(hora, { hora = it }, label = { Text("Hora (HH:mm)") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(direccion, { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(latitud, { latitud = it }, label = { Text("Latitud") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(longitud, { longitud = it }, label = { Text("Longitud") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(plazas, { plazas = it }, label = { Text("Plazas máximas") }, modifier = Modifier.fillMaxWidth())

    error?.let { Text("Error: $it") }

    Button(
        onClick = { onGuardar(CamposEvento(nombre, descripcion, tipo, fecha, hora, direccion, latitud, longitud, plazas)) },
        modifier = Modifier.fillMaxWidth(),
    ) { Text(textoBoton) }
}
