package com.example.dnd_quedamos.di

import com.example.dnd_quedamos.core.network.createHttpClient
import com.example.dnd_quedamos.features.evento.data.remote.EventoApi
import com.example.dnd_quedamos.features.evento.data.repository.RepositoryEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Red: un único HttpClient compartido por todas las Api de la app
val networkModule = module {
    single<HttpClient> { createHttpClient() }
}

val eventoModule = module {
    singleOf(::EventoApi)                                       // recibe el HttpClient de networkModule
    singleOf(::RepositoryEvento) bind IRepositoryEvento::class // una única instancia, expuesta como interfaz
    factoryOf(::ManagerEventoUseCase)                          // nueva instancia en cada petición
    viewModelOf(::EventoViewModel)                             // ligado al ciclo de vida de la pantalla
}

// Añadir aquí chatModule, mapaModule y usuarioModule cuando sigan el mismo patrón
val appModules = listOf(networkModule, eventoModule)
