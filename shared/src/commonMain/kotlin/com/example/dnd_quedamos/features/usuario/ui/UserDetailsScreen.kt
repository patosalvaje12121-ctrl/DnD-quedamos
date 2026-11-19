package com.example.dnd_quedamos.features.usuario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.dnd_quedamos.features.usuario.presentation.ViewUserUiState

@Composable
fun UserDetailsScreen(
    viewModel: ViewUserUiState,
    onRegistrarse: (() -> Unit)? = null,
    onIniciarSesion: (() -> Unit)? = null,
    onEditarUsuario: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {

            viewModel.loading -> {
                CircularProgressIndicator()
            }

            // Ajustes sin sesión
            viewModel.sinSesion -> {
                AccesoUsuario(
                    onRegistrarse = onRegistrarse,
                    onIniciarSesion = onIniciarSesion
                )
            }


            // Error al cargar el perfil
            viewModel.error != null -> {
                Text(text = viewModel.error)
            }

            // Perfil cargado
            viewModel.dataUsuario != null -> {
                val esAdministradorPerfil =
                    if (viewModel.esPerfilPropio) {
                        viewModel.esAdmin == true
                    } else {
                        viewModel.esAdminPerfil == true
                    }

                CartaUsuario(
                    nombre = viewModel.dataUsuario.nombre,
                    descripcion = viewModel.dataUsuario.descBio,
                    esPerfilPropio = viewModel.esPerfilPropio,
                    esAdminPerfil = esAdministradorPerfil,
                    onEditarUsuario = onEditarUsuario,
                    onCerrarSesion = onCerrarSesion
                )
            }

            else -> {
                Text("No hay información del usuario")
            }
        }
    }
}

@Composable
private fun AccesoUsuario(
    onRegistrarse: (() -> Unit)?,
    onIniciarSesion: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bienvenido a DnD Quedamos",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onRegistrarse?.invoke() },
            enabled = onRegistrarse != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f)
            )

            Text("o")

            HorizontalDivider(
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedButton(
            onClick = { onIniciarSesion?.invoke() },
            enabled = onIniciarSesion != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }
    }
}

@Composable
private fun CartaUsuario(
    nombre: String,
    descripcion: String,
    esPerfilPropio: Boolean,
    esAdminPerfil: Boolean,
    onEditarUsuario: (() -> Unit)?,
    onCerrarSesion: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (esPerfilPropio) {
                OutlinedButton(
                    onClick = { onEditarUsuario?.invoke() },
                    enabled = onEditarUsuario != null,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = "Editar usuario",
                        fontSize = 12.sp
                    )
                }
            }

//            // TODO: implementar imágenes de usuario
//            Box(
//                modifier = Modifier
//                    .size(110.dp)
//                    .background(
//                        color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
//                        shape = CircleShape
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "TODO:\nimplementar\nimagenes",
//                    fontSize = 11.sp,
//                    textAlign = TextAlign.Center
//                )
//            }

            Text(
                text = nombre,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (esAdminPerfil) {
                Text(
                    text = "Administrador",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = descripcion.ifBlank {
                    ""
                },
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )


            if (esPerfilPropio) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { onCerrarSesion?.invoke() },
                    enabled = onCerrarSesion != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }
            }

        }
    }
}