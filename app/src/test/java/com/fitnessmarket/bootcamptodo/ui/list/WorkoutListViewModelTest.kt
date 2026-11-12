package com.fitnessmarket.bootcamptodo.ui.list

import app.cash.turbine.test
import com.fitnessmarket.bootcamptodo.MainDispatcherRule
import com.fitnessmarket.bootcamptodo.domain.DeleteWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.GetWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.Workout
import com.fitnessmarket.bootcamptodo.domain.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class WorkoutListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // replay = 1 so an emission isn't lost if it races the ViewModel's upstream subscription.
    private val workoutsFlow = MutableSharedFlow<List<Workout>>(replay = 1)
    private val repository = mockk<WorkoutRepository> {
        every { observeWorkouts() } returns workoutsFlow
        coEvery { delete(any()) } returns Unit
    }
    private val viewModel = WorkoutListViewModel(GetWorkoutsUseCase(repository), DeleteWorkoutUseCase(repository))

    @Test
    fun `starts loading then shows repository workouts`() = runTest {
        val workouts = listOf(Workout(id = 1, name = "Run", date = LocalDate.of(2026, 3, 1), durationMinutes = 30))

        viewModel.uiState.test {
            assertEquals(WorkoutListUiState(isLoading = true), awaitItem())
            workoutsFlow.emit(workouts)
            assertEquals(WorkoutListUiState(workouts = workouts, isLoading = false), awaitItem())
        }
    }

    @Test
    fun `empty repository shows empty list, not loading`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            workoutsFlow.emit(emptyList())
            assertEquals(WorkoutListUiState(workouts = emptyList(), isLoading = false), awaitItem())
        }
    }

    @Test
    fun `onDelete deletes workout by id`() = runTest {
        viewModel.onDelete(7L)

        coVerify(exactly = 1) { repository.delete(7L) }
    }
}
