package com.example.dnd_quedamos.di

import com.example.dnd_quedamos.features.chat.domain.usecase.ManagerChatUseCase
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.mapa.domain.usecase.ManagerMapaUseCase
import com.example.dnd_quedamos.features.usuario.domain.usecase.ManagerUsuarioUseCase
import com.example.dnd_quedamos.core.session.FakeCurrentUserProvider
import com.example.dnd_quedamos.features.usuario.data.repository.RepositoryUsuario
interface AppContainer {
    val useManagerChatUseCase : ManagerChatUseCase;
    val useManagerEventoUseCase : ManagerEventoUseCase;
    val useManagerMapaUseCase : ManagerMapaUseCase;
    val useManagerUsuarioUseCase : ManagerUsuarioUseCase;
}

class DefaultAppContainer : AppContainer {

    /*
        Declarar variable que tenga acceso a la BD y pasarla a los UseCases
     */

    override val useManagerChatUseCase: ManagerChatUseCase by lazy {
        ManagerChatUseCase()
    }
    override val useManagerEventoUseCase: ManagerEventoUseCase by lazy {
        ManagerEventoUseCase()
    }
    override val useManagerMapaUseCase: ManagerMapaUseCase by lazy {
        ManagerMapaUseCase()
    }

    override val useManagerUsuarioUseCase: ManagerUsuarioUseCase by lazy {
        ManagerUsuarioUseCase(
            repositoryUsuario = RepositoryUsuario(),
            currentUserProvider = FakeCurrentUserProvider()
        )
    }

}