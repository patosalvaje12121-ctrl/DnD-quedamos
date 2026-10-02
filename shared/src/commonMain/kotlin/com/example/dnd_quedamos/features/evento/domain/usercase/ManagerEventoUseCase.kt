package com.example.dnd_quedamos.features.evento.domain.usercase


import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento

class ManagerEventoUseCase(
    private val repositoryEvento: IRepositoryEvento
) {

    suspend fun MostrarEvento(id: int): DataEvento{

        val de = DataEvento(1, "h", "hola")

        if(de == null){

        }

        return de
    }



}