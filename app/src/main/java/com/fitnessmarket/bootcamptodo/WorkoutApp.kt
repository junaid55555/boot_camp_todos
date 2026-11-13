package com.fitnessmarket.bootcamptodo

import android.app.Application
import com.fitnessmarket.bootcamptodo.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WorkoutApp : Application() {
    lateinit var container: AppContainer
        private set

    /** Work that should outlive any screen; lives as long as the process. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch { container.cleanUpOrphanPhotos() }
    }
}
