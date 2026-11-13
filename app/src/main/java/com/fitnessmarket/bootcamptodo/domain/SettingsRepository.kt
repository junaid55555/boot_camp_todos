package com.fitnessmarket.bootcamptodo.domain

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    /** The user's dark theme choice, or null when they haven't picked one (follow the system). */
    val darkTheme: StateFlow<Boolean?>

    fun setDarkTheme(enabled: Boolean)
}
