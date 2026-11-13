package com.fitnessmarket.bootcamptodo.ui.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.domain.Workout
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RunWith(AndroidJUnit4::class)
class WorkoutListContentTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val legDay = Workout(
        id = 2,
        name = "Leg day",
        date = LocalDate.of(2026, 3, 15),
        durationMinutes = 50,
        photoFileName = "leg-day.jpg",
    )
    private val yoga = Workout(id = 1, name = "Yoga", date = LocalDate.of(2026, 3, 14), durationMinutes = 45)

    private var deleted: Workout? = null
    private var addClicks = 0

    private fun str(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    private fun setContent(uiState: WorkoutListUiState) = rule.setContent {
        BootCampTodoTheme {
            WorkoutListContent(
                uiState = uiState,
                // No such files: the thumbnail shows its placeholder, which is all these tests need.
                photoFile = { File(rule.activity.cacheDir, it) },
                onAddClick = { addClicks++ },
                onDelete = { deleted = it },
            )
        }
    }

    @Test
    fun loading_showsProgressIndicator() {
        setContent(WorkoutListUiState(isLoading = true))

        rule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun noWorkouts_showsEmptyMessage() {
        setContent(WorkoutListUiState(isLoading = false))

        rule.onNodeWithText(str(R.string.empty_workouts)).assertIsDisplayed()
    }

    @Test
    fun workouts_showNameDateAndDuration_inGivenOrder() {
        setContent(WorkoutListUiState(workouts = listOf(legDay, yoga), isLoading = false))

        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        rule.onNodeWithText(str(R.string.workout_summary, legDay.date.format(formatter), 50)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.workout_summary, yoga.date.format(formatter), 45)).assertIsDisplayed()
        val legDayTop = rule.onNodeWithText("Leg day").getUnclippedBoundsInRoot().top
        val yogaTop = rule.onNodeWithText("Yoga").getUnclippedBoundsInRoot().top
        assertTrue("Leg day should be above Yoga", legDayTop < yogaTop)
    }

    @Test
    fun onlyWorkoutsWithAPhoto_showAThumbnail() {
        setContent(WorkoutListUiState(workouts = listOf(legDay, yoga), isLoading = false))

        rule.onAllNodesWithContentDescription(str(R.string.workout_photo)).assertCountEquals(1)
    }

    @Test
    fun deleteButton_deletesThatWorkout() {
        setContent(WorkoutListUiState(workouts = listOf(legDay, yoga), isLoading = false))

        rule.onNodeWithContentDescription(str(R.string.delete_workout, "Yoga")).performClick()

        assertEquals(yoga, deleted)
    }

    @Test
    fun addButton_callsOnAddClick() {
        setContent(WorkoutListUiState(isLoading = false))

        rule.onNodeWithContentDescription(str(R.string.add_workout)).performClick()

        assertEquals(1, addClicks)
    }
}
