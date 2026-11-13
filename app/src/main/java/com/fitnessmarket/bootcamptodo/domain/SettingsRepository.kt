package com.fitnessmarket.bootcamptodo.domain

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    /** The user's theme choice; [ThemeMode.SYSTEM] until they pick one. */
    val themeMode: StateFlow<ThemeMode>

    fun setThemeMode(mode: ThemeMode)
}
