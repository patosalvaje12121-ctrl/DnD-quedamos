package com.example.dnd_quedamos.features.evento.domain.usercase


import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento

class ManagerEventoUseCase(
    private val repositoryEvento: IRepositoryEvento
) {

    suspend fun MostrarEvento(): List<DataEvento>{

        val de:List<DataEvento> = repositoryEvento.bbddEvento()

        /*if(de == null){
            return Result.failure (IllegalStateException("QUESO"))
        }*/

        return de
    }



}