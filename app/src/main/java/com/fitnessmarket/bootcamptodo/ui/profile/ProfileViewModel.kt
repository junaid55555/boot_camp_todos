package com.fitnessmarket.bootcamptodo.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fitnessmarket.bootcamptodo.di.AppContainer
import com.fitnessmarket.bootcamptodo.domain.SettingsRepository
import com.fitnessmarket.bootcamptodo.domain.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(private val settings: SettingsRepository) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeMode

    fun onThemeModeChange(mode: ThemeMode) {
        settings.setThemeMode(mode)
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProfileViewModel(container.settings) }
        }
    }
}
