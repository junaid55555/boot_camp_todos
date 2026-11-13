package com.fitnessmarket.bootcamptodo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.data.WorkoutDatabase
import com.fitnessmarket.bootcamptodo.di.AppContainer
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The real app graph (screens, ViewModels, use cases, Room) over an in-memory database. */
@RunWith(AndroidJUnit4::class)
class WorkoutNavHostTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var db: WorkoutDatabase

    private fun str(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(rule.activity, WorkoutDatabase::class.java).build()
        val container = AppContainer(rule.activity, database = db)
        rule.setContent { BootCampTodoTheme { WorkoutNavHost(container) } }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun startsOnHome_withEmptyList() {
        rule.onNodeWithText(str(R.string.tab_home)).assertIsSelected()
        rule.onNodeWithText(str(R.string.empty_workouts)).assertIsDisplayed()
    }

    @Test
    fun tabs_switchBetweenHomeAndProfile() {
        rule.onNodeWithText(str(R.string.tab_profile)).performClick()
        rule.onNodeWithText(str(R.string.dark_theme)).assertIsDisplayed()

        rule.onNodeWithText(str(R.string.tab_home)).performClick()
        rule.onNodeWithText(str(R.string.empty_workouts)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.dark_theme)).assertDoesNotExist()
    }

    @Test
    fun addScreen_isFullScreen_andBackReturnsHome() {
        rule.onNodeWithContentDescription(str(R.string.add_workout)).performClick()

        rule.onNodeWithText(str(R.string.field_name)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.tab_home)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.tab_profile)).assertDoesNotExist()

        Espresso.pressBack()

        rule.onNodeWithText(str(R.string.empty_workouts)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.tab_home)).assertIsDisplayed()
    }

    @Test
    fun invalidSave_staysOnAddScreen_withErrors() {
        rule.onNodeWithContentDescription(str(R.string.add_workout)).performClick()

        rule.onNodeWithText(str(R.string.save)).performScrollTo().performClick()

        rule.onNodeWithText(str(R.string.error_name_blank)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.error_duration_invalid)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.field_name)).assertIsDisplayed()
    }

    @Test
    fun addThenDelete_aWorkout() {
        rule.onNodeWithContentDescription(str(R.string.add_workout)).performClick()
        rule.onNodeWithText(str(R.string.field_name)).performTextInput("Leg day")
        rule.onNodeWithText(str(R.string.field_duration)).performTextInput("50")
        rule.onNodeWithText(str(R.string.save)).performScrollTo().performClick()

        // Saving goes back to the list, which now shows the workout. (The add screen's name field also
        // contains "Leg day", so wait for the list's delete button instead.)
        val deleteLegDay = hasContentDescription(str(R.string.delete_workout, "Leg day"))
        rule.waitUntil(timeoutMillis = 5_000) { rule.onAllNodes(deleteLegDay).fetchSemanticsNodes().size == 1 }
        rule.onNodeWithText("Leg day").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.tab_home)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.empty_workouts)).assertDoesNotExist()

        rule.onNode(deleteLegDay).performClick()

        rule.waitUntil(timeoutMillis = 5_000) { rule.onAllNodes(deleteLegDay).fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithText(str(R.string.empty_workouts)).assertIsDisplayed()
    }
}
