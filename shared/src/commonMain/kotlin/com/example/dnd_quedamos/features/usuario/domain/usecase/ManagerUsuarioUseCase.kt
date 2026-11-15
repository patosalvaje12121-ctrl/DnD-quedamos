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
}