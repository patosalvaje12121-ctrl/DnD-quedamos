package com.example.dnd_quedamos.features.usuario.domain.usecase

import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.core.session.MutableCurrentUserProvider
import com.example.dnd_quedamos.features.usuario.data.repository.RepositoryUsuario
import com.example.dnd_quedamos.features.usuario.domain.model.DataUsuario
import com.example.dnd_quedamos.features.usuario.domain.repository.IRepositoryUsuario
import kotlinx.coroutines.CancellationException

class ManagerUsuarioUseCase (
    private val repositoryUsuario: IRepositoryUsuario,
    private val currentUserProvider: MutableCurrentUserProvider
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

        val limpio = usuario.limpio()

        validar(limpio)?.let {
            return Result.failure(it)
        }

        return try {

            val nuevoUsuario = limpio.copy(id = 0)

            Result.success(
                repositoryUsuario.crearUsuario(nuevoUsuario)
            )

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun EditarUsuario(usuario: DataUsuario): Result<DataUsuario> {

        val limpio = usuario.limpio()

        validar(limpio)?.let {
            return Result.failure(it)
        }

        return try {

            val usuarioActual = currentUserProvider.currentUser()
                ?: throw IllegalStateException(
                    "No hay ningún usuario con sesión iniciada"
                )

            if (limpio.id != usuarioActual.id) {
                throw IllegalStateException(
                    "No puedes modificar el perfil de otro usuario"
                )
            }

            val usuarioGuardado =
                repositoryUsuario.obtenerUsuario(limpio.id)
                    ?: throw NoSuchElementException(
                        "El usuario no existe"
                    )

            val usuarioEditado = usuarioGuardado.copy(
                name = limpio.name,
                descBio = limpio.descBio,
                imageID = limpio.imageID
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

            currentUserProvider.cerrarSesion()

            Result.success(true)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private fun DataUsuario.limpio(): DataUsuario =
        copy(
            name = name.trim(),
            descBio = descBio.trim()
        )


    private fun validar(usuario: DataUsuario): Exception? {

        val error = when {
            usuario.name.isEmpty() ->
                "El nombre del usuario no puede estar vacío"

            usuario.name.length > 50 ->
                "El nombre no puede superar los 50 caracteres"

            usuario.descBio.length > 500 ->
                "La biografía no puede superar los 500 caracteres"

            else -> return null
        }

        return IllegalArgumentException(error)
    }


}