package com.example.dnd_quedamos.features.evento.domain.usercase


import com.example.dnd_quedamos.features.evento.data.repository.RepositoryEvento
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento

class ManagerEventoUseCase(
    private val repositoryEvento: IRepositoryEvento = RepositoryEvento()
) {

    suspend fun MostrarEvento(): Result<List<DataEvento>>{

        val de:List<DataEvento> = repositoryEvento.bbddEvento()

        if(de.isEmpty()){
            return Result.failure(NoSuchElementException())
        }

        return Result.success(de)
    }



}