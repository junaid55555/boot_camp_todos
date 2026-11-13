package com.fitnessmarket.bootcamptodo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessmarket.bootcamptodo.ui.WorkoutNavHost
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as WorkoutApp).container
        setContent {
            val savedDarkTheme by container.settings.darkTheme.collectAsStateWithLifecycle()
            val darkTheme = savedDarkTheme ?: isSystemInDarkTheme()

            // Status/navigation bar icons follow the app's theme, not the system's.
            DisposableEffect(darkTheme) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            BootCampTodoTheme(darkTheme = darkTheme) {
                WorkoutNavHost(container)
            }
        }
    }
}
