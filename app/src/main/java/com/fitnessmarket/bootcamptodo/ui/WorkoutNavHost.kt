package com.fitnessmarket.bootcamptodo.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.di.AppContainer
import com.fitnessmarket.bootcamptodo.ui.add.AddWorkoutScreen
import com.fitnessmarket.bootcamptodo.ui.add.AddWorkoutViewModel
import com.fitnessmarket.bootcamptodo.ui.list.WorkoutListScreen
import com.fitnessmarket.bootcamptodo.ui.list.WorkoutListViewModel
import com.fitnessmarket.bootcamptodo.ui.profile.ProfileScreen
import com.fitnessmarket.bootcamptodo.ui.profile.ProfileViewModel

private object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val ADD = "add"
}

private enum class Tab(val route: String, @StringRes val label: Int, @DrawableRes val icon: Int) {
    HOME(Routes.HOME, R.string.tab_home, R.drawable.ic_home),
    PROFILE(Routes.PROFILE, R.string.tab_profile, R.drawable.ic_person),
}

@Composable
fun WorkoutNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    // The bar shows only on tab destinations; the add screen is full-screen.
    val showBottomBar = Tab.entries.any { it.route == currentDestination?.route }

    Scaffold(
        // Each screen's own Scaffold handles the system bars.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = { navController.navigateToTab(tab) },
                            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            composable(Routes.HOME) { entry ->
                WorkoutListScreen(
                    viewModel = viewModel(factory = WorkoutListViewModel.factory(container)),
                    photoFile = container.photoStore::file,
                    onAddClick = { if (entry.isResumed()) navController.navigate(Routes.ADD) },
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(viewModel = viewModel(factory = ProfileViewModel.factory(container)))
            }
            composable(Routes.ADD) { entry ->
                AddWorkoutScreen(
                    viewModel = viewModel(factory = AddWorkoutViewModel.factory(container)),
                    photoFile = container.photoStore::file,
                    onBack = { if (entry.isResumed()) navController.popBackStack() },
                )
            }
        }
    }
}

/** Standard tab switch: one copy of each tab on the back stack, and each tab keeps its state. */
private fun NavHostController.navigateToTab(tab: Tab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Ignores repeat taps while a navigation transition is already in progress. */
private fun NavBackStackEntry.isResumed() = lifecycle.currentState == Lifecycle.State.RESUMED
