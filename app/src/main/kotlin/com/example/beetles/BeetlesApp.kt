package com.example.beetles

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BeetlesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BeetlesApp)
            modules(appModule)
        }
    }
}