package com.fitnessmarket.bootcamptodo

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.domain.ThemeMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The feature as the user sees it: the Profile theme choice recolours the whole app, and the choice survives
 * the activity being recreated. Checks real pixels, so it catches the theme not being applied at all.
 */
@RunWith(AndroidJUnit4::class)
class DarkThemeTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val settings get() = (rule.activity.application as WorkoutApp).container.settings
    private val prefs get() = rule.activity.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private var savedPrefs: Map<String, *> = emptyMap<String, Any>()

    private fun str(id: Int) = rule.activity.getString(id)

    // Option rows merge their texts, so match on the label as a substring.
    private fun option(label: Int) = rule.onNodeWithText(str(label), substring = true)

    @Before
    fun setUp() {
        // This uses the app's real settings, so put the user's choice back afterwards.
        savedPrefs = prefs.all.toMap()
        rule.runOnUiThread { settings.setThemeMode(ThemeMode.LIGHT) }
        rule.onNodeWithText(str(R.string.tab_profile)).performClick()
    }

    @After
    fun tearDown() {
        prefs.edit().clear().apply {
            savedPrefs.forEach { (key, value) -> if (value is Boolean) putBoolean(key, value) }
        }.commit()
    }

    @Test
    fun pickingDark_makesTheAppDark_andLightMakesItLightAgain() {
        option(R.string.theme_light).assertIsSelected()
        assertTrue("expected a light screen", screenBackground().luminance() > 0.5f)

        option(R.string.dark_theme).performClick()

        option(R.string.dark_theme).assertIsSelected()
        assertTrue("expected a dark screen", screenBackground().luminance() < 0.5f)

        option(R.string.theme_light).performClick()

        option(R.string.theme_light).assertIsSelected()
        assertTrue("expected a light screen", screenBackground().luminance() > 0.5f)
    }

    @Test
    fun pickingSystem_followsTheDeviceSetting() {
        option(R.string.theme_system).performClick()

        option(R.string.theme_system).assertIsSelected()
        val nightMask = rule.activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val deviceIsDark = nightMask == Configuration.UI_MODE_NIGHT_YES
        assertEquals(deviceIsDark, screenBackground().luminance() < 0.5f)
    }

    @Test
    fun choice_survivesActivityRecreation() {
        option(R.string.dark_theme).performClick()

        rule.activityRule.scenario.recreate()

        option(R.string.dark_theme).assertIsSelected()
        assertTrue("expected a dark screen", screenBackground().luminance() < 0.5f)
    }

    @Test
    fun choice_isSavedToDisk() {
        option(R.string.dark_theme).performClick()
        assertTrue(prefs.getBoolean("dark_theme", false))

        option(R.string.theme_system).performClick()
        assertFalse(prefs.contains("dark_theme"))
    }

    /** A pixel at the left edge, mid-height: inside the side gutter, so it's the screen background, not a card. */
    private fun screenBackground(): Color {
        val pixels = rule.onRoot().captureToImage().toPixelMap()
        return pixels[2, pixels.height / 2]
    }
}
