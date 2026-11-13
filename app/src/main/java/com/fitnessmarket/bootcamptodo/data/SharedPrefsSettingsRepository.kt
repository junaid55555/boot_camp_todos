package com.fitnessmarket.bootcamptodo.data

import android.content.Context
import androidx.core.content.edit
import com.fitnessmarket.bootcamptodo.domain.SettingsRepository
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

    private val _darkTheme = MutableStateFlow(
        if (prefs.contains(KEY_DARK_THEME)) prefs.getBoolean(KEY_DARK_THEME, false) else null,
    )
    override val darkTheme: StateFlow<Boolean?> = _darkTheme.asStateFlow()

    override fun setDarkTheme(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DARK_THEME, enabled) }
        _darkTheme.value = enabled
    }

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
    }
}
