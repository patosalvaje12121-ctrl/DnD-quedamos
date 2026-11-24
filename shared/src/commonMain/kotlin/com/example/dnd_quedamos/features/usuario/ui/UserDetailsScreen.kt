package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.dnd_quedamos.core.ui.componentes.DndPanel
import com.example.dnd_quedamos.core.ui.skins.DndSkins
import com.example.dnd_quedamos.features.usuario.presentation.ViewUserUiState
import com.example.dnd_quedamos.core.ui.componentes.AccionBoton
import com.example.dnd_quedamos.core.ui.componentes.Separator
import com.example.dnd_quedamos.core.ui.componentes.SeparatorDouble
import com.example.dnd_quedamos.core.ui.componentes.SeparatorOr
import org.maplibre.compose.expressions.value.TextUnitValue

@Composable
fun UserDetailsScreen(
    viewModel: ViewUserUiState,
    onRegistrarse: (() -> Unit)? = null,
    onIniciarSesion: (() -> Unit)? = null,
    onEditarUsuario: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            viewModel.loading -> CircularProgressIndicator()

            viewModel.sinSesion -> AccesoUsuario(
                onRegistrarse = onRegistrarse,
                onIniciarSesion = onIniciarSesion,
            )

            viewModel.error != null -> Text(text = viewModel.error)

            viewModel.dataUsuario != null -> CartaUsuario(
                nombre = viewModel.dataUsuario.nombre,
                descripcion = viewModel.dataUsuario.descBio,
                esPerfilPropio = viewModel.esPerfilPropio,
                esAdminPerfil = viewModel.esAdminDelPerfil(),
                onEditarUsuario = onEditarUsuario,
                onCerrarSesion = onCerrarSesion,
            )

            else -> Text("No hay información del usuario")
        }
    }
}

/** Si el perfil mostrado es el propio, el flag viene de la sesión; si no, del perfil ajeno. */
private fun ViewUserUiState.esAdminDelPerfil(): Boolean =
    (if (esPerfilPropio) esAdmin else esAdminPerfil) == true

@Composable
private fun AccesoUsuario(
    onRegistrarse: (() -> Unit)?,
    onIniciarSesion: (() -> Unit)?,
) {
    DndPanel(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column (
            Modifier.weight(1.1f)
        ) {
            Text(
                text = "Bienvenido a DnD Quedamos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1.4f)
            )

            Text(
                "Powered by SalDK S.A."
            )

            Text(
                "TODO: Logo"
            )
        }


        AccionBoton(
            texto = "Iniciar sesión",
            onClick = onIniciarSesion,
            modifier = Modifier.fillMaxWidth(),
            skin = DndSkins.Botones.terciario,
        )

        SeparatorOr();

        AccionBoton(
            texto = "Registrarse",
            onClick = onRegistrarse,
            modifier = Modifier.fillMaxWidth().paddingFromBaseline(bottom = 150.dp),
            skin = DndSkins.Botones.terciario,
        )
    }
}

@Composable
private fun CartaUsuario(
    nombre: String,
    descripcion: String,
    esPerfilPropio: Boolean,
    esAdminPerfil: Boolean,
    onEditarUsuario: (() -> Unit)?,
    onCerrarSesion: (() -> Unit)?,
) {
    DndPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (esPerfilPropio) {
            AccionBoton(
                texto = "Editar usuario",
                onClick = onEditarUsuario,
                modifier = Modifier.align(Alignment.Start),
                skin = DndSkins.Botones.texto,
            )
        }

        // TODO: imagen de usuario (avatar)

        Text(
            text = nombre,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        if (esAdminPerfil) {
            Text(
                text = "Administrador",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (descripcion.isNotBlank()) {
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }

        if (esPerfilPropio) {
            Spacer(Modifier.height(4.dp))
            AccionBoton(
                texto = "Cerrar sesión",
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                skin = DndSkins.Botones.secundario,
            )
        }
    }
}