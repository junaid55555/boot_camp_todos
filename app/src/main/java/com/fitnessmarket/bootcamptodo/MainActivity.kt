package com.fitnessmarket.bootcamptodo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessmarket.bootcamptodo.ui.WorkoutNavHost
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import com.fitnessmarket.bootcamptodo.ui.theme.isDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as WorkoutApp).container
        setContent {
            val themeMode by container.settings.themeMode.collectAsStateWithLifecycle()
            val darkTheme = themeMode.isDark()

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
