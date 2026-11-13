package com.fitnessmarket.bootcamptodo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.fitnessmarket.bootcamptodo.domain.ThemeMode

/** Whether this mode means a dark UI right now; [ThemeMode.SYSTEM] tracks the device setting. */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
