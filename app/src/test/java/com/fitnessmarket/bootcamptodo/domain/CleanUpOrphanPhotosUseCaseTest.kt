package com.fitnessmarket.bootcamptodo.domain

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CleanUpOrphanPhotosUseCaseTest {

    @Test
    fun `deletes only files no workout references`() = runTest {
        val repository = mockk<WorkoutRepository> { coEvery { photoFileNames() } returns setOf("kept.jpg") }
        val photoStore = mockk<PhotoStore> {
            every { listFileNames() } returns listOf("kept.jpg", "orphan.jpg")
            every { delete(any()) } returns Unit
        }

        CleanUpOrphanPhotosUseCase(repository, photoStore)()

        verify(exactly = 1) { photoStore.delete("orphan.jpg") }
        verify(exactly = 0) { photoStore.delete("kept.jpg") }
    }
}
