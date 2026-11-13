package com.fitnessmarket.bootcamptodo.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileContentTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var requested: Boolean? = null

    private fun setContent(darkTheme: Boolean) = rule.setContent {
        BootCampTodoTheme { ProfileContent(darkTheme = darkTheme, onDarkThemeChange = { requested = it }) }
    }

    @Test
    fun switchShowsOff_whenDarkThemeIsOff() {
        setContent(darkTheme = false)

        rule.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun switchShowsOn_whenDarkThemeIsOn() {
        setContent(darkTheme = true)

        rule.onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun tappingTheRow_requestsTheOppositeValue() {
        setContent(darkTheme = false)

        // The label, not the switch: the whole row is the touch target.
        rule.onNodeWithText(rule.activity.getString(R.string.dark_theme)).performClick()

        assertEquals(true, requested)
    }

    @Test
    fun tappingTheRow_whenOn_requestsOff() {
        setContent(darkTheme = true)

        rule.onNode(isToggleable()).performClick()

        assertEquals(false, requested)
    }
}
