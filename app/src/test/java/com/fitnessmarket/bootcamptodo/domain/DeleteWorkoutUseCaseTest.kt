package com.fitnessmarket.bootcamptodo.domain

import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class DeleteWorkoutUseCaseTest {

    private val repository = mockk<WorkoutRepository> { coEvery { delete(any()) } returns Unit }
    private val photoStore = mockk<PhotoStore> { every { delete(any()) } returns Unit }
    private val deleteWorkout = DeleteWorkoutUseCase(repository, photoStore)

    @Test
    fun `deletes workout row then its photo`() = runTest {
        deleteWorkout(workout(id = 42L, photoFileName = "a.jpg"))

        coVerifyOrder {
            repository.delete(42L)
            photoStore.delete("a.jpg")
        }
    }

    @Test
    fun `workout without photo deletes no file`() = runTest {
        deleteWorkout(workout(id = 7L, photoFileName = null))

        verify(exactly = 0) { photoStore.delete(any()) }
    }

    private fun workout(id: Long, photoFileName: String?) = Workout(
        id = id,
        name = "Run",
        date = LocalDate.of(2026, 3, 1),
        durationMinutes = 30,
        photoFileName = photoFileName,
    )
}
