package com.timothy.gogolook

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MainApp:Application() {
    companion object{
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()

        //timber settings
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        appContext = applicationContext
    }
}