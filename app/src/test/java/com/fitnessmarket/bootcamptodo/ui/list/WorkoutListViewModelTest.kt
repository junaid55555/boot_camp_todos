package com.fitnessmarket.bootcamptodo.ui.list

import app.cash.turbine.test
import com.fitnessmarket.bootcamptodo.MainDispatcherRule
import com.fitnessmarket.bootcamptodo.domain.DateFilter
import com.fitnessmarket.bootcamptodo.domain.DeleteWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.FilterWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.GetWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.PhotoStore
import com.fitnessmarket.bootcamptodo.domain.Workout
import com.fitnessmarket.bootcamptodo.domain.WorkoutRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class WorkoutListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 3, 15)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    private val run = Workout(id = 2, name = "Morning run", date = today, durationMinutes = 30)
    private val yoga = Workout(id = 1, name = "Yoga", date = today.minusDays(10), durationMinutes = 45)

    // replay = 1 so an emission isn't lost if it races the ViewModel's upstream subscription.
    private val workoutsFlow = MutableSharedFlow<List<Workout>>(replay = 1)
    private val repository = mockk<WorkoutRepository> {
        every { observeWorkouts() } returns workoutsFlow
        coEvery { delete(any()) } returns Unit
    }
    private val photoStore = mockk<PhotoStore> { every { delete(any()) } returns Unit }

    private fun viewModel() = WorkoutListViewModel(
        getWorkouts = GetWorkoutsUseCase(repository),
        deleteWorkout = DeleteWorkoutUseCase(repository, photoStore),
        filterWorkouts = FilterWorkoutsUseCase(clock),
    )

    @Test
    fun `starts loading then shows repository workouts`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(WorkoutListUiState(isLoading = true), awaitItem())
            workoutsFlow.emit(listOf(run, yoga))
            assertEquals(
                WorkoutListUiState(workouts = listOf(run, yoga), hasWorkouts = true, isLoading = false),
                awaitItem(),
            )
        }
    }

    @Test
    fun `empty repository shows empty list, not loading`() = runTest {
        val viewModel = viewModel()

        workoutsFlow.emit(emptyList())

        assertEquals(WorkoutListUiState(isLoading = false), viewModel.uiState.value)
    }

    @Test
    fun `query change filters the visible workouts immediately`() = runTest {
        val viewModel = viewModel()
        workoutsFlow.emit(listOf(run, yoga))

        viewModel.onQueryChange("yog")

        val state = viewModel.uiState.value
        assertEquals("yog", state.query)
        assertEquals(listOf(yoga), state.workouts)
        assertTrue(state.hasWorkouts)
    }

    @Test
    fun `date filter change filters the visible workouts`() = runTest {
        val viewModel = viewModel()
        workoutsFlow.emit(listOf(run, yoga))

        viewModel.onDateFilterChange(DateFilter.LAST_7_DAYS)

        assertEquals(DateFilter.LAST_7_DAYS, viewModel.uiState.value.dateFilter)
        assertEquals(listOf(run), viewModel.uiState.value.workouts)
    }

    @Test
    fun `filters stay applied when the repository emits again`() = runTest {
        val viewModel = viewModel()
        workoutsFlow.emit(listOf(run))
        viewModel.onQueryChange("yoga")

        workoutsFlow.emit(listOf(run, yoga))

        assertEquals(listOf(yoga), viewModel.uiState.value.workouts)
    }

    @Test
    fun `onClearFilters resets query and date filter`() = runTest {
        val viewModel = viewModel()
        workoutsFlow.emit(listOf(run, yoga))
        viewModel.onQueryChange("nothing matches")
        viewModel.onDateFilterChange(DateFilter.TODAY)

        viewModel.onClearFilters()

        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertEquals(DateFilter.ALL, state.dateFilter)
        assertEquals(listOf(run, yoga), state.workouts)
        assertFalse(state.isFiltering)
    }

    @Test
    fun `load failure shows an error, and retry loads again`() = runTest {
        var attempts = 0
        every { repository.observeWorkouts() } returns flow {
            attempts++
            if (attempts == 1) throw IOException("disk") else emit(listOf(run))
        }
        val viewModel = viewModel()

        assertEquals(WorkoutListUiState(isLoading = false, loadFailed = true), viewModel.uiState.value)

        viewModel.onRetry()

        assertEquals(
            WorkoutListUiState(workouts = listOf(run), hasWorkouts = true, isLoading = false),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `onDelete deletes the workout and its photo`() = runTest {
        val viewModel = viewModel()

        viewModel.onDelete(run.copy(id = 7, photoFileName = "a.jpg"))

        coVerify(exactly = 1) { repository.delete(7L) }
        verify(exactly = 1) { photoStore.delete("a.jpg") }
    }
}
