package com.fitnessmarket.bootcamptodo.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutError
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RunWith(AndroidJUnit4::class)
class AddWorkoutContentTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val today = LocalDate.of(2026, 3, 15)
    private val emptyForm = AddWorkoutUiState(date = today, today = today)

    /** Every callback the screen fired, in order, e.g. "name:Run" or "save". */
    private val events = mutableListOf<String>()

    private fun str(id: Int) = rule.activity.getString(id)

    private fun setContent(uiState: AddWorkoutUiState) = rule.setContent {
        // Typed text is fed back in, as the ViewModel does; a fixed value would make the fields reset.
        var state by remember { mutableStateOf(uiState) }
        BootCampTodoTheme {
            AddWorkoutContent(
                uiState = state,
                photoFile = { File(rule.activity.cacheDir, it) },
                onNameChange = {
                    events += "name:$it"
                    state = state.copy(name = it)
                },
                onDateChange = { events += "date:$it" },
                onDurationChange = {
                    events += "duration:$it"
                    state = state.copy(durationText = it)
                },
                onTakePhoto = { events += "takePhoto" },
                onRemovePhoto = { events += "removePhoto" },
                onSave = { events += "save" },
                onBack = { events += "back" },
            )
        }
    }

    @Test
    fun typing_reportsNameAndDuration() {
        setContent(emptyForm)

        rule.onNodeWithText(str(R.string.field_name)).performTextInput("Run")
        rule.onNodeWithText(str(R.string.field_duration)).performTextInput("30")

        assertEquals(listOf("name:Run", "duration:30"), events)
    }

    @Test
    fun dateField_showsTheFormDate() {
        setContent(emptyForm.copy(date = today.minusDays(3)))

        val formatted = today.minusDays(3).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        rule.onNodeWithText(formatted).assertIsDisplayed()
    }

    @Test
    fun datePicker_confirmingKeepsTheSelectedDate() {
        val date = today.minusDays(3)
        setContent(emptyForm.copy(date = date))

        rule.onNodeWithContentDescription(str(R.string.pick_date)).performClick()
        rule.onNodeWithText(str(R.string.ok)).performClick()

        // Round-trips through the picker's UTC millis without shifting a day.
        assertEquals(listOf("date:$date"), events)
    }

    @Test
    fun noErrors_showsNoErrorMessages() {
        setContent(emptyForm)

        listOf(R.string.error_name_blank, R.string.error_duration_invalid, R.string.error_date_in_future)
            .forEach { rule.onNodeWithText(str(it)).assertDoesNotExist() }
    }

    @Test
    fun errors_areShownUnderTheirFields() {
        setContent(
            emptyForm.copy(
                errors = setOf(
                    AddWorkoutError.NAME_BLANK,
                    AddWorkoutError.DURATION_INVALID,
                    AddWorkoutError.DATE_IN_FUTURE,
                ),
            ),
        )

        rule.onNodeWithText(str(R.string.error_name_blank)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.error_duration_invalid)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.error_date_in_future)).assertIsDisplayed()
    }

    @Test
    fun nameTooLong_showsItsOwnMessage() {
        setContent(emptyForm.copy(errors = setOf(AddWorkoutError.NAME_TOO_LONG)))

        rule.onNodeWithText(str(R.string.error_name_too_long)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.error_name_blank)).assertDoesNotExist()
    }

    @Test
    fun saveButton_callsOnSave() {
        setContent(emptyForm)

        rule.onNodeWithText(str(R.string.save)).performScrollTo().assertIsEnabled().performClick()

        assertEquals(listOf("save"), events)
    }

    @Test
    fun saveButton_isDisabledWhileSaving() {
        setContent(emptyForm.copy(isSaving = true))

        rule.onNodeWithText(str(R.string.save)).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun backArrow_callsOnBack() {
        setContent(emptyForm)

        rule.onNodeWithContentDescription(str(R.string.back)).performClick()

        assertEquals(listOf("back"), events)
    }

    @Test
    fun withoutPhoto_offersAddPhoto() {
        setContent(emptyForm)

        rule.onNodeWithText(str(R.string.add_photo)).performScrollTo().performClick()

        assertEquals(listOf("takePhoto"), events)
        rule.onNodeWithContentDescription(str(R.string.workout_photo)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.retake_photo)).assertDoesNotExist()
        rule.onNodeWithText(str(R.string.remove_photo)).assertDoesNotExist()
    }

    @Test
    fun withPhoto_showsPreviewWithRetakeAndRemove() {
        setContent(emptyForm.copy(photoFileName = "a.jpg"))

        rule.onNodeWithContentDescription(str(R.string.workout_photo)).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(str(R.string.add_photo)).assertDoesNotExist()

        rule.onNodeWithText(str(R.string.retake_photo)).performScrollTo().performClick()
        rule.onNodeWithText(str(R.string.remove_photo)).performScrollTo().performClick()

        assertEquals(listOf("takePhoto", "removePhoto"), events)
    }
}
