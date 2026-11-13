package com.fitnessmarket.bootcamptodo.ui.profile

import com.fitnessmarket.bootcamptodo.MainDispatcherRule
import com.fitnessmarket.bootcamptodo.domain.SettingsRepository
import com.fitnessmarket.bootcamptodo.domain.ThemeMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    private val settings = mockk<SettingsRepository> {
        every { themeMode } returns themeModeFlow
        every { setThemeMode(any()) } returns Unit
    }
    private val viewModel = ProfileViewModel(settings)

    @Test
    fun `theme follows the system until the user picks one`() {
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun `theme follows the saved setting`() {
        themeModeFlow.value = ThemeMode.DARK

        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)
    }

    @Test
    fun `picking dark saves it`() {
        viewModel.onThemeModeChange(ThemeMode.DARK)

        verify(exactly = 1) { settings.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `picking light saves it`() {
        viewModel.onThemeModeChange(ThemeMode.LIGHT)

        verify(exactly = 1) { settings.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `picking system saves it`() {
        viewModel.onThemeModeChange(ThemeMode.SYSTEM)

        verify(exactly = 1) { settings.setThemeMode(ThemeMode.SYSTEM) }
    }
}
