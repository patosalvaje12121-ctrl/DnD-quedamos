package com.example.dnd_quedamos.di

import com.example.dnd_quedamos.core.network.createFakeApiEngine
import com.example.dnd_quedamos.core.network.createHttpClient
import com.example.dnd_quedamos.features.evento.data.remote.EventoApi
import com.example.dnd_quedamos.features.evento.data.repository.RepositoryEvento
import com.example.dnd_quedamos.features.evento.domain.repository.IRepositoryEvento
import com.example.dnd_quedamos.features.evento.domain.usecase.ManagerEventoUseCase
import com.example.dnd_quedamos.features.evento.presentation.EventoViewModel
import com.example.dnd_quedamos.core.session.CurrentUserProvider
import com.example.dnd_quedamos.core.session.FakeCurrentUserProvider
import com.example.dnd_quedamos.features.chat.data.remote.ChatApi
import com.example.dnd_quedamos.features.chat.data.repository.RepositoryChat
import com.example.dnd_quedamos.features.chat.domain.repository.IRepositoryChat
import com.example.dnd_quedamos.features.chat.domain.usecase.ManagerChatUseCase
import com.example.dnd_quedamos.features.chat.presentation.ChatListViewModel
import com.example.dnd_quedamos.features.chat.presentation.ChatViewModel
import com.example.dnd_quedamos.features.chat.presentation.NuevoChatViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * true  -> las peticiones las contesta el servidor falso (core/network/FakeApiEngine.kt)
 * false -> las peticiones van a la API real (BASE_URL en core/network/HttpClientFactory.kt)
 */
const val USE_FAKE_API = true

// Red: un único HttpClient compartido por todas las Api de la app
val networkModule = module {
    single<HttpClient> {
        if (USE_FAKE_API) createHttpClient(createFakeApiEngine())
        else createHttpClient()
    }
}

val eventoModule = module {
    singleOf(::EventoApi)                                       // recibe el HttpClient de networkModule
    singleOf(::RepositoryEvento) bind IRepositoryEvento::class // una única instancia, expuesta como interfaz
    factoryOf(::ManagerEventoUseCase)                          // nueva instancia en cada petición
    viewModelOf(::EventoViewModel)                             // ligado al ciclo de vida de la pantalla
}

// Sesión: quién es el usuario actual (falso hasta que exista login)
val sessionModule = module {
    single<CurrentUserProvider> { FakeCurrentUserProvider() }
}

val chatModule = module {
    singleOf(::ChatApi)
    singleOf(::RepositoryChat) bind IRepositoryChat::class
    factoryOf(::ManagerChatUseCase)
    viewModelOf(::ChatListViewModel)
    viewModelOf(::NuevoChatViewModel)
    // ChatViewModel necesita el chatId: se pasa con koinViewModel { parametersOf(chatId) }
    viewModel { (chatId: Int) -> ChatViewModel(chatId, get(), get()) }
}

// Añadir aquí mapaModule y usuarioModule cuando sigan el mismo patrón
val appModules = listOf(networkModule, sessionModule, eventoModule, chatModule)
