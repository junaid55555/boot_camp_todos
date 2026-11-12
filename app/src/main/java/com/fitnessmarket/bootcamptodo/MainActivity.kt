package com.fitnessmarket.bootcamptodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fitnessmarket.bootcamptodo.ui.WorkoutNavHost
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as WorkoutApp).container
        setContent {
            BootCampTodoTheme {
                WorkoutNavHost(container)
            }
        }
    }
}
