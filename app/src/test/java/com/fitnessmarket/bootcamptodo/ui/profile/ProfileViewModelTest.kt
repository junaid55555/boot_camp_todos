package com.fitnessmarket.bootcamptodo.ui.profile

import com.fitnessmarket.bootcamptodo.MainDispatcherRule
import com.fitnessmarket.bootcamptodo.domain.SettingsRepository
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

    private val darkThemeFlow = MutableStateFlow<Boolean?>(null)
    private val settings = mockk<SettingsRepository> {
        every { darkTheme } returns darkThemeFlow
        every { setDarkTheme(any()) } returns Unit
    }
    private val viewModel = ProfileViewModel(settings)

    @Test
    fun `dark theme is unset until the user picks one`() {
        assertEquals(null, viewModel.darkTheme.value)
    }

    @Test
    fun `dark theme follows the saved setting`() {
        darkThemeFlow.value = true

        assertEquals(true, viewModel.darkTheme.value)
    }

    @Test
    fun `turning dark theme on saves it`() {
        viewModel.onDarkThemeChange(true)

        verify(exactly = 1) { settings.setDarkTheme(true) }
    }

    @Test
    fun `turning dark theme off saves it`() {
        viewModel.onDarkThemeChange(false)

        verify(exactly = 1) { settings.setDarkTheme(false) }
    }
}
