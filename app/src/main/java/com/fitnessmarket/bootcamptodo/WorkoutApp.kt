package com.fitnessmarket.bootcamptodo

import android.app.Application
import com.fitnessmarket.bootcamptodo.di.AppContainer

class WorkoutApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
