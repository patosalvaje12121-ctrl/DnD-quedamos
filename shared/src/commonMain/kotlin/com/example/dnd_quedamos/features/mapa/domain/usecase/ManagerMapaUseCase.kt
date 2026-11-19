package com.example.dnd_quedamos.features.mapa.domain.usecase

import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.features.evento.domain.model.DataEvento
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase

class ManagerMapaUseCase(
    private val managerEventoUseCase: ManagerEventoUseCase,
    private val currentUserProvider: CurrentUserProvider
) {
    /**
     * Obtiene los eventos disponibles para pintarlos en el mapa.
     * TODO: filtrar por distancia (ej. a menos de 20km del usuario)
     * o por tipo de juego (rol, cartas), se hace aquí.
     */
    suspend fun obtenerEventos(): Result<List<DataEvento>> =
        managerEventoUseCase.MostrarEvento()

    suspend fun usuarioActualId(): Int? =
        currentUserProvider.currentUser()?.id

    suspend fun unirseEvento(eventoId: Int): Result<Unit> =
        managerEventoUseCase.UnirseEvento(eventoId)

    suspend fun abandonarEvento(eventoId: Int): Result<Unit> =
        managerEventoUseCase.AbandonarEvento(eventoId)
}