package com.fitnessmarket.bootcamptodo.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class AddWorkoutUseCaseTest {

    private val today = LocalDate.of(2026, 3, 15)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val repository = mockk<WorkoutRepository>()
    private lateinit var addWorkout: AddWorkoutUseCase

    @Before
    fun setUp() {
        coEvery { repository.add(any()) } returns Unit
        addWorkout = AddWorkoutUseCase(repository, clock)
    }

    @Test
    fun `valid input saves trimmed workout and returns success`() = runTest {
        val result = addWorkout(name = "  Morning run ", date = today, durationText = " 30 ")

        assertEquals(AddWorkoutResult.Success, result)
        coVerify(exactly = 1) {
            repository.add(Workout(name = "Morning run", date = today, durationMinutes = 30))
        }
    }

    @Test
    fun `photo file name is stored on the workout`() = runTest {
        addWorkout(name = "Run", date = today, durationText = "30", photoFileName = "a.jpg")

        coVerify(exactly = 1) {
            repository.add(Workout(name = "Run", date = today, durationMinutes = 30, photoFileName = "a.jpg"))
        }
    }

    @Test
    fun `past date is accepted`() = runTest {
        val result = addWorkout(name = "Swim", date = today.minusDays(10), durationText = "45")

        assertEquals(AddWorkoutResult.Success, result)
    }

    @Test
    fun `blank name is rejected`() = runTest {
        assertInvalid(addWorkout(name = "   ", date = today, durationText = "30"), AddWorkoutError.NAME_BLANK)
    }

    @Test
    fun `name over 50 characters is rejected`() = runTest {
        assertInvalid(
            addWorkout(name = "a".repeat(51), date = today, durationText = "30"),
            AddWorkoutError.NAME_TOO_LONG,
        )
    }

    @Test
    fun `name of exactly 50 characters is accepted`() = runTest {
        assertEquals(AddWorkoutResult.Success, addWorkout(name = "a".repeat(50), date = today, durationText = "30"))
    }

    @Test
    fun `zero duration is rejected`() = runTest {
        assertInvalid(addWorkout(name = "Run", date = today, durationText = "0"), AddWorkoutError.DURATION_INVALID)
    }

    @Test
    fun `negative duration is rejected`() = runTest {
        assertInvalid(addWorkout(name = "Run", date = today, durationText = "-5"), AddWorkoutError.DURATION_INVALID)
    }

    @Test
    fun `duration over 1440 is rejected`() = runTest {
        assertInvalid(addWorkout(name = "Run", date = today, durationText = "1441"), AddWorkoutError.DURATION_INVALID)
    }

    @Test
    fun `duration of 1440 is accepted`() = runTest {
        assertEquals(AddWorkoutResult.Success, addWorkout(name = "Run", date = today, durationText = "1440"))
    }

    @Test
    fun `non-numeric duration is rejected`() = runTest {
        assertInvalid(addWorkout(name = "Run", date = today, durationText = "abc"), AddWorkoutError.DURATION_INVALID)
    }

    @Test
    fun `empty duration is rejected`() = runTest {
        assertInvalid(addWorkout(name = "Run", date = today, durationText = ""), AddWorkoutError.DURATION_INVALID)
    }

    @Test
    fun `future date is rejected`() = runTest {
        assertInvalid(
            addWorkout(name = "Run", date = today.plusDays(1), durationText = "30"),
            AddWorkoutError.DATE_IN_FUTURE,
        )
    }

    @Test
    fun `all errors are reported together`() = runTest {
        val result = addWorkout(name = "", date = today.plusDays(1), durationText = "0")

        assertEquals(
            AddWorkoutResult.Invalid(
                setOf(AddWorkoutError.NAME_BLANK, AddWorkoutError.DURATION_INVALID, AddWorkoutError.DATE_IN_FUTURE),
            ),
            result,
        )
    }

    private fun assertInvalid(result: AddWorkoutResult, expected: AddWorkoutError) {
        assertEquals(AddWorkoutResult.Invalid(setOf(expected)), result)
        coVerify(exactly = 0) { repository.add(any()) }
    }
}
