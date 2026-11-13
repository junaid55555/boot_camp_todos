package com.fitnessmarket.bootcamptodo.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.domain.ThemeMode
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileContentTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var requested: ThemeMode? = null

    private fun str(id: Int) = rule.activity.getString(id)

    private fun setContent(themeMode: ThemeMode) = rule.setContent {
        BootCampTodoTheme {
            ProfileContent(themeMode = themeMode, appVersion = "9.9", onThemeModeChange = { requested = it })
        }
    }

    // Each option row merges its texts, so the System row is found by its label plus description.
    private fun option(label: Int) = rule.onNodeWithText(str(label), substring = true)

    @Test
    fun onlyTheSavedModeIsSelected() {
        setContent(ThemeMode.DARK)

        option(R.string.dark_theme).assertIsSelected()
        option(R.string.theme_light).assertIsNotSelected()
        option(R.string.theme_system).assertIsNotSelected()
    }

    @Test
    fun systemIsSelected_whenFollowingTheSystem() {
        setContent(ThemeMode.SYSTEM)

        option(R.string.theme_system).assertIsSelected()
    }

    @Test
    fun tappingAnOption_requestsThatMode() {
        setContent(ThemeMode.LIGHT)

        option(R.string.dark_theme).performClick()
        assertEquals(ThemeMode.DARK, requested)

        option(R.string.theme_system).performClick()
        assertEquals(ThemeMode.SYSTEM, requested)

        option(R.string.theme_light).performClick()
        assertEquals(ThemeMode.LIGHT, requested)
    }

    @Test
    fun showsTheAppVersion() {
        setContent(ThemeMode.SYSTEM)

        rule.onNodeWithText("9.9").performScrollTo().assertIsDisplayed()
    }
}
