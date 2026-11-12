package com.fitnessmarket.bootcamptodo.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitnessmarket.bootcamptodo.di.AppContainer
import com.fitnessmarket.bootcamptodo.ui.add.AddWorkoutScreen
import com.fitnessmarket.bootcamptodo.ui.add.AddWorkoutViewModel
import com.fitnessmarket.bootcamptodo.ui.list.WorkoutListScreen
import com.fitnessmarket.bootcamptodo.ui.list.WorkoutListViewModel

private object Routes {
    const val LIST = "list"
    const val ADD = "add"
}

@Composable
fun WorkoutNavHost(container: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) { entry ->
            WorkoutListScreen(
                viewModel = viewModel(factory = WorkoutListViewModel.factory(container)),
                onAddClick = { if (entry.isResumed()) navController.navigate(Routes.ADD) },
            )
        }
        composable(Routes.ADD) { entry ->
            AddWorkoutScreen(
                viewModel = viewModel(factory = AddWorkoutViewModel.factory(container)),
                onBack = { if (entry.isResumed()) navController.popBackStack() },
            )
        }
    }
}

/** Ignores repeat taps while a navigation transition is already in progress. */
private fun NavBackStackEntry.isResumed() = lifecycle.currentState == Lifecycle.State.RESUMED
