package com.example.dnd_quedamos.features.evento.domain.usecase


import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento
import kotlinx.coroutines.CancellationException

class ManagerEventoUseCase(
    private val repositoryEvento: IRepositoryEvento
) {

    suspend fun MostrarEvento(): Result<List<DataEvento>>{

        // La llamada ahora va por red y puede lanzar excepciones (sin conexión, 404, 500, timeout...).
        // Las convertimos en Result.failure para que el ViewModel las gestione sin que la app se cierre.
        val de: List<DataEvento> = try {
            repositoryEvento.bbddEvento()
        } catch (e: CancellationException) {
            throw e // la cancelación de corrutinas NO es un error: hay que dejarla pasar
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if(de.isEmpty()){
            return Result.failure(NoSuchElementException())
        }

        return Result.success(de)
    }



}