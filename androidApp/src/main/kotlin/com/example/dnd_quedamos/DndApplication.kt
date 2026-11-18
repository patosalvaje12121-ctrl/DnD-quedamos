package com.example.dnd_quedamos

import android.app.Application
import com.example.dnd_quedamos.di.initKoin
import org.koin.android.ext.koin.androidContext

class DndApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@DndApplication) }
    }
}
