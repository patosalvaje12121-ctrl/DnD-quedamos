package com.example.dnd_quedamos.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Arranca Koin una sola vez por proceso. Cada plataforma lo llama desde su punto de entrada. */
fun initKoin(platformConfig: KoinAppDeclaration = {}) {
    startKoin {
        platformConfig() // Android añade androidContext() aquí
        modules(appModules)
    }
}
