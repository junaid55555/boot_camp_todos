package com.fitnessmarket.bootcamptodo.data

import android.content.Context
import androidx.core.content.edit
import com.fitnessmarket.bootcamptodo.domain.SettingsRepository
import com.fitnessmarket.bootcamptodo.domain.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Read once at startup (synchronously, so the first frame already has the right theme),
 * then kept in memory; writes go to disk in the background via `apply()`.
 * Tests pass their own [fileName] so they never touch the user's settings.
 */
class SharedPrefsSettingsRepository(
    context: Context,
    fileName: String = "settings",
) : SettingsRepository {

    private val prefs = context.applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    // Same on-disk format as the old dark theme switch (true / false / absent = system),
    // so a choice saved by an earlier version still applies without a migration.
    private val _themeMode = MutableStateFlow(
        when {
            !prefs.contains(KEY_DARK_THEME) -> ThemeMode.SYSTEM
            prefs.getBoolean(KEY_DARK_THEME, false) -> ThemeMode.DARK
            else -> ThemeMode.LIGHT
        },
    )
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit {
            when (mode) {
                ThemeMode.SYSTEM -> remove(KEY_DARK_THEME)
                ThemeMode.LIGHT -> putBoolean(KEY_DARK_THEME, false)
                ThemeMode.DARK -> putBoolean(KEY_DARK_THEME, true)
            }
        }
        _themeMode.value = mode
    }

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
    }
}
