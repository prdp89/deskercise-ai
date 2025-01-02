package com.ai.app.move.deskercise.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.ai.app.move.deskercise.di.appModule
import com.ai.app.move.deskercise.di.networkModule
import com.ai.app.move.deskercise.di.repositoryModule
import com.ai.app.move.deskercise.di.useCaseModule
import com.ai.app.move.deskercise.di.viewModelModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

@ExperimentalCoroutinesApi
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(appModule, useCaseModule, viewModelModule, networkModule, repositoryModule)
        }
        // DISABLE DARK THEME
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
