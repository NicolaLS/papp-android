package xyz.lilsus.papp.di

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class PappApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    val appDependencies by lazy {
        AppDependencies(applicationContext, applicationScope)
    }
}