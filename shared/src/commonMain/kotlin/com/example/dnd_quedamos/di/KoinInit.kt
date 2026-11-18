package com.example.dnd_quedamos.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Arranca Koin una sola vez por proceso. Cada plataforma lo llama desde su punto de entrada. */
fun initKoin(platformConfig: KoinAppDeclaration = {}) {
    startKoin {
        modules(appModules)
        // Va DESPUÉS de appModules: si dos módulos definen el mismo tipo, gana el último cargado.
        // Así una plataforma puede sustituir una implementación por defecto (p. ej. Android
        // cambia RecordatorioEventosVacio por RecordatorioEventosAndroid). Android también añade
        // androidContext() aquí.
        platformConfig()
    }
}
