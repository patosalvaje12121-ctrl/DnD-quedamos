package com.example.dnd_quedamos.di

import com.example.dnd_quedamos.features.evento.data.repository.RepositoryEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val eventoModule = module {
    singleOf(::RepositoryEvento) bind IRepositoryEvento::class // una única instancia, expuesta como interfaz
    factoryOf(::ManagerEventoUseCase)                          // nueva instancia en cada petición
    viewModelOf(::EventoViewModel)                             // ligado al ciclo de vida de la pantalla
}

// Añadir aquí chatModule, mapaModule y usuarioModule cuando sigan el mismo patrón
val appModules = listOf(eventoModule)
