package com.fitnessmarket.bootcamptodo.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteWorkoutUseCaseTest {

    @Test
    fun `deletes workout by id`() = runTest {
        val repository = mockk<WorkoutRepository> { coEvery { delete(any()) } returns Unit }

        DeleteWorkoutUseCase(repository)(42L)

        coVerify(exactly = 1) { repository.delete(42L) }
    }
}
