package com.fitnessmarket.bootcamptodo.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.domain.ThemeMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPrefsSettingsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val prefs get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Its own file, so the user's real settings are never touched.
    private fun newRepository() = SharedPrefsSettingsRepository(context, fileName = PREFS)

    // Clear rather than delete the file: Android caches the prefs in memory per process.
    @Before
    @After
    fun clear() {
        prefs.edit().clear().commit()
    }

    @Test
    fun nothingSaved_meansFollowTheSystem() {
        assertEquals(ThemeMode.SYSTEM, newRepository().themeMode.value)
    }

    @Test
    fun setThemeMode_updatesTheFlowImmediately() {
        val repository = newRepository()

        repository.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.themeMode.value)
    }

    @Test
    fun savedChoice_isReadByTheNextInstance() {
        ThemeMode.entries.forEach { mode ->
            newRepository().setThemeMode(mode)

            assertEquals(mode, newRepository().themeMode.value)
        }
    }

    @Test
    fun choosingSystem_removesTheSavedChoice() {
        newRepository().setThemeMode(ThemeMode.DARK)

        newRepository().setThemeMode(ThemeMode.SYSTEM)

        assertFalse(prefs.contains(KEY))
    }

    @Test
    fun valueSavedByTheOldDarkThemeSwitch_isStillRead() {
        prefs.edit().putBoolean(KEY, true).commit()
        assertEquals(ThemeMode.DARK, newRepository().themeMode.value)

        prefs.edit().putBoolean(KEY, false).commit()
        assertEquals(ThemeMode.LIGHT, newRepository().themeMode.value)
    }

    private companion object {
        const val PREFS = "settings-test"
        const val KEY = "dark_theme"
    }
}
