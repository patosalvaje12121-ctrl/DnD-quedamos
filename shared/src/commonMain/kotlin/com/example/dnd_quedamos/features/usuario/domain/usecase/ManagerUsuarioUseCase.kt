package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.usuario.data.repository.RepositoryUsuario
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario
import kotlinx.coroutines.CancellationException

class ManagerUsuarioUseCase (
    private val repositoryUsuario: IRepositoryUsuario,
    private val currentUserProvider: CurrentUserProvider
) {

    suspend fun MostrarUsuario(id: Int): Result<DataUsuario> {
        return try {

            val idUsuario = if (id == -1) {
                currentUserProvider.currentUser()?.id
                    ?: throw IllegalStateException(
                        "No hay ningún usuario con sesión iniciada"
                    )
            } else {
                id
            }

            require(idUsuario > 0) {
                "El ID del usuario no es válido"
            }

            val usuario = repositoryUsuario.obtenerUsuario(idUsuario)
                ?: throw NoSuchElementException(
                    "No existe el usuario con ID $idUsuario"
                )

            Result.success(usuario)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun CrearUsuario(usuario: DataUsuario): Result<DataUsuario> {

        return try {

            // Validar que el nombre no esté vacío
            val nombre = usuario.name.trim()

            require(nombre.isNotEmpty()) {
                "El nombre del usuario no puede estar vacío"
            }

            // El ID lo asigna el repositorio
            val nuevoUsuario = usuario.copy(
                id = 0,
                name = nombre
            )

            val usuarioGuardado =
                repositoryUsuario.crearUsuario(nuevoUsuario)

            Result.success(usuarioGuardado)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun EditarUsuario(usuario: DataUsuario): Result<DataUsuario> {

        val nombre = usuario.name.trim()

        if (nombre.isEmpty()) {
            return Result.failure(
                IllegalArgumentException(
                    "El nombre del usuario no puede estar vacío"
                )
            )
        }

        return try {

            val usuarioActual = currentUserProvider.currentUser()
                ?: throw IllegalStateException(
                    "No hay ningún usuario con sesión iniciada"
                )

            if (usuario.id != usuarioActual.id) {
                throw IllegalStateException(
                    "No puedes modificar el perfil de otro usuario"
                )
            }

            val usuarioEditado = usuario.copy(
                name = nombre,
                descBio = usuario.descBio.trim()
            )

            Result.success(
                repositoryUsuario.editarUsuario(usuarioEditado)
            )

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun EliminarUsuario(id: Int): Result<Boolean> {

        return try {

            val usuarioActual = currentUserProvider.currentUser()
                ?: throw IllegalStateException(
                    "No hay ningún usuario con sesión iniciada"
                )

            val idUsuario = if (id == -1) {
                usuarioActual.id
            } else {
                id
            }

            require(idUsuario == usuarioActual.id) {
                "No puedes eliminar otro usuario"
            }

            val eliminado = repositoryUsuario.eliminarUsuario(idUsuario)

            if (!eliminado) {
                throw NoSuchElementException(
                    "No existe el usuario con ID $idUsuario"
                )
            }

            Result.success(true)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}