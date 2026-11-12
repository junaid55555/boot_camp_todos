package com.fitnessmarket.bootcamptodo.ui.add

import app.cash.turbine.test
import com.fitnessmarket.bootcamptodo.MainDispatcherRule
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutError
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.Workout
import com.fitnessmarket.bootcamptodo.domain.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class AddWorkoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 3, 15)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val repository = mockk<WorkoutRepository> { coEvery { add(any()) } returns Unit }
    private val viewModel = AddWorkoutViewModel(AddWorkoutUseCase(repository, clock), clock)

    @Test
    fun `initial state has empty fields and today's date`() {
        assertEquals(AddWorkoutUiState(date = today, today = today), viewModel.uiState.value)
    }

    @Test
    fun `invalid save shows errors and does not emit saved`() = runTest {
        viewModel.saved.test {
            viewModel.onSave()

            assertEquals(
                setOf(AddWorkoutError.NAME_BLANK, AddWorkoutError.DURATION_INVALID),
                viewModel.uiState.value.errors
            )
            expectNoEvents()
        }
        coVerify(exactly = 0) { repository.add(any()) }
    }

    @Test
    fun `editing a field clears only that field's error`() {
        viewModel.onSave()

        viewModel.onNameChange("Run")

        assertEquals(setOf(AddWorkoutError.DURATION_INVALID), viewModel.uiState.value.errors)
    }

    @Test
    fun `valid save stores workout and emits saved`() = runTest {
        val date = today.minusDays(2)
        viewModel.onNameChange("Cycling")
        viewModel.onDateChange(date)
        viewModel.onDurationChange("60")

        viewModel.saved.test {
            viewModel.onSave()
            awaitItem()
        }

        assertTrue(viewModel.uiState.value.errors.isEmpty())
        coVerify(exactly = 1) { repository.add(Workout(name = "Cycling", date = date, durationMinutes = 60)) }
    }
}
