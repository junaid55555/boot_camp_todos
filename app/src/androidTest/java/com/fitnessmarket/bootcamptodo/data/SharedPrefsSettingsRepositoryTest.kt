package com.fitnessmarket.bootcamptodo.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPrefsSettingsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    // Its own file, so the user's real settings are never touched.
    private fun newRepository() = SharedPrefsSettingsRepository(context, fileName = PREFS)

    // Clear rather than delete the file: Android caches the prefs in memory per process.
    @Before
    @After
    fun clear() {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun nothingSaved_meansFollowTheSystem() {
        assertNull(newRepository().darkTheme.value)
    }

    @Test
    fun setDarkTheme_updatesTheFlowImmediately() {
        val repository = newRepository()

        repository.setDarkTheme(true)

        assertEquals(true, repository.darkTheme.value)
    }

    @Test
    fun savedChoice_isReadByTheNextInstance() {
        newRepository().setDarkTheme(true)
        assertEquals(true, newRepository().darkTheme.value)

        newRepository().setDarkTheme(false)
        assertEquals(false, newRepository().darkTheme.value)
    }

    private companion object {
        const val PREFS = "settings-test"
    }
}
