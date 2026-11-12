package com.fitnessmarket.bootcamptodo.domain

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetWorkoutsUseCaseTest {

    @Test
    fun `emits workouts from repository`() = runTest {
        val workouts = listOf(Workout(id = 1, name = "Run", date = LocalDate.of(2026, 3, 1), durationMinutes = 30))
        val repository = mockk<WorkoutRepository> { every { observeWorkouts() } returns flowOf(workouts) }

        GetWorkoutsUseCase(repository)().test {
            assertEquals(workouts, awaitItem())
            awaitComplete()
        }
    }
}
