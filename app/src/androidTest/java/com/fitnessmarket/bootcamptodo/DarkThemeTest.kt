package com.fitnessmarket.bootcamptodo

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The feature as the user sees it: the Profile switch recolours the whole app, and the choice survives
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

    @Before
    fun setUp() {
        // This uses the app's real settings, so put the user's choice back afterwards.
        savedPrefs = prefs.all.toMap()
        rule.runOnUiThread { settings.setDarkTheme(false) }
        rule.onNodeWithText(str(R.string.tab_profile)).performClick()
    }

    @After
    fun tearDown() {
        prefs.edit().clear().apply {
            savedPrefs.forEach { (key, value) -> if (value is Boolean) putBoolean(key, value) }
        }.commit()
    }

    @Test
    fun switchingOn_makesTheAppDark_andOffMakesItLightAgain() {
        rule.onNode(isToggleable()).assertIsOff()
        assertTrue("expected a light screen", screenBackground().luminance() > 0.5f)

        rule.onNode(isToggleable()).performClick()

        rule.onNode(isToggleable()).assertIsOn()
        assertTrue("expected a dark screen", screenBackground().luminance() < 0.5f)

        rule.onNode(isToggleable()).performClick()

        rule.onNode(isToggleable()).assertIsOff()
        assertTrue("expected a light screen", screenBackground().luminance() > 0.5f)
    }

    @Test
    fun choice_survivesActivityRecreation() {
        rule.onNode(isToggleable()).performClick()

        rule.activityRule.scenario.recreate()

        rule.onNode(isToggleable()).assertIsOn()
        assertTrue("expected a dark screen", screenBackground().luminance() < 0.5f)
    }

    @Test
    fun choice_isSavedToDisk() {
        rule.onNode(isToggleable()).performClick()

        assertTrue(prefs.getBoolean("dark_theme", false))
    }

    /** A pixel in the empty middle of the screen, below the Profile list item. */
    private fun screenBackground(): Color {
        val pixels = rule.onRoot().captureToImage().toPixelMap()
        return pixels[pixels.width / 2, pixels.height / 2]
    }
}
