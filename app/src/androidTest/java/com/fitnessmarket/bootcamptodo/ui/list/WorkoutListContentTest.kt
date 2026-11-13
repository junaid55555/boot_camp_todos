package com.fitnessmarket.bootcamptodo.ui.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.domain.DateFilter
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
    private val loaded = WorkoutListUiState(workouts = listOf(legDay, yoga), hasWorkouts = true, isLoading = false)

    private var deleted: Workout? = null
    private var addClicks = 0
    private var query: String? = null
    private var dateFilter: DateFilter? = null
    private var clearFilterClicks = 0
    private var retryClicks = 0

    private fun str(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    private fun setContent(uiState: WorkoutListUiState) = rule.setContent {
        BootCampTodoTheme {
            WorkoutListContent(
                uiState = uiState,
                // No such files: the thumbnail shows its placeholder, which is all these tests need.
                photoFile = { File(rule.activity.cacheDir, it) },
                onAddClick = { addClicks++ },
                onDelete = { deleted = it },
                onQueryChange = { query = it },
                onDateFilterChange = { dateFilter = it },
                onClearFilters = { clearFilterClicks++ },
                onRetry = { retryClicks++ },
            )
        }
    }

    @Test
    fun loading_showsProgressIndicator() {
        setContent(WorkoutListUiState(isLoading = true))

        rule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun noWorkouts_showsEmptyState_withoutSearch() {
        setContent(WorkoutListUiState(isLoading = false))

        rule.onNodeWithText(str(R.string.empty_workouts)).assertIsDisplayed()
        rule.onNode(hasSetTextAction()).assertDoesNotExist()
    }

    @Test
    fun emptyStateAction_callsOnAddClick() {
        setContent(WorkoutListUiState(isLoading = false))

        rule.onNodeWithText(str(R.string.log_first_workout)).performClick()

        assertEquals(1, addClicks)
    }

    @Test
    fun workouts_showNameDateAndDuration_inGivenOrder() {
        setContent(loaded)

        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        rule.onNodeWithText(legDay.date.format(formatter)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.workout_duration, 50)).assertIsDisplayed()
        rule.onNodeWithText(yoga.date.format(formatter)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.workout_duration, 45)).assertIsDisplayed()
        val legDayTop = rule.onNodeWithText("Leg day").getUnclippedBoundsInRoot().top
        val yogaTop = rule.onNodeWithText("Yoga").getUnclippedBoundsInRoot().top
        assertTrue("Leg day should be above Yoga", legDayTop < yogaTop)
    }

    @Test
    fun onlyWorkoutsWithAPhoto_showAThumbnail() {
        setContent(loaded)

        rule.onAllNodesWithContentDescription(str(R.string.workout_photo)).assertCountEquals(1)
    }

    @Test
    fun longName_wrapsInsteadOfBeingCut() {
        val longName = "Morning run along the river, then hill sprints and a long cool-down stretch on the grass"
        setContent(loaded.copy(workouts = listOf(yoga.copy(name = longName))))

        rule.onNodeWithText(longName).assertIsDisplayed()
        val oneLine = rule.onNodeWithText(str(R.string.workout_duration, 45)).getUnclippedBoundsInRoot().height
        val name = rule.onNodeWithText(longName).getUnclippedBoundsInRoot().height
        assertTrue("Name should take more than one line", name > oneLine)
    }

    @Test
    fun deleteButton_deletesThatWorkout() {
        setContent(loaded)

        rule.onNodeWithContentDescription(str(R.string.delete_workout, "Yoga")).performClick()

        assertEquals(yoga, deleted)
    }

    @Test
    fun addButton_callsOnAddClick() {
        setContent(WorkoutListUiState(isLoading = false))

        rule.onNodeWithContentDescription(str(R.string.add_workout)).performClick()

        assertEquals(1, addClicks)
    }

    @Test
    fun typingInSearch_callsOnQueryChange() {
        setContent(loaded)

        rule.onNode(hasSetTextAction()).performTextInput("yo")

        assertEquals("yo", query)
    }

    @Test
    fun clearSearchButton_clearsQuery() {
        setContent(loaded.copy(query = "yo"))

        rule.onNodeWithContentDescription(str(R.string.clear_search)).performClick()

        assertEquals("", query)
    }

    @Test
    fun dateFilterChip_showsSelection_andReportsClicks() {
        setContent(loaded.copy(dateFilter = DateFilter.TODAY))

        rule.onNodeWithText(str(R.string.filter_today)).assertIsSelected()
        rule.onNodeWithText(str(R.string.filter_last_7_days)).performClick()

        assertEquals(DateFilter.LAST_7_DAYS, dateFilter)
    }

    @Test
    fun noMatches_showsMessage_andClearFilters() {
        setContent(loaded.copy(workouts = emptyList(), query = "swim"))

        rule.onNodeWithText(str(R.string.no_matches)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.empty_workouts)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.clear_filters)).performClick()

        assertEquals(1, clearFilterClicks)
    }

    @Test
    fun loadFailed_showsError_andRetry() {
        setContent(WorkoutListUiState(isLoading = false, loadFailed = true))

        rule.onNodeWithText(str(R.string.load_error)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.retry)).performClick()

        assertEquals(1, retryClicks)
    }
}
