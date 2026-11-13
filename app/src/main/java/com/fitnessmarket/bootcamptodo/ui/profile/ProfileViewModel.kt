package com.fitnessmarket.bootcamptodo.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fitnessmarket.bootcamptodo.di.AppContainer
import com.fitnessmarket.bootcamptodo.domain.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(private val settings: SettingsRepository) : ViewModel() {

    /** null = the user hasn't picked yet, so the screen shows the system setting. */
    val darkTheme: StateFlow<Boolean?> = settings.darkTheme

    fun onDarkThemeChange(enabled: Boolean) {
        settings.setDarkTheme(enabled)
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProfileViewModel(container.settings) }
        }
    }
}
