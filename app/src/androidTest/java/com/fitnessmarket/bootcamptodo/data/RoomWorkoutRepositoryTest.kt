package com.fitnessmarket.bootcamptodo.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitnessmarket.bootcamptodo.domain.Workout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomWorkoutRepositoryTest {

    private lateinit var db: WorkoutDatabase
    private lateinit var repository: RoomWorkoutRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WorkoutDatabase::class.java,
        ).build()
        repository = RoomWorkoutRepository(db.workoutDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun add_thenObserve_mapsEveryField() = runTest {
        val workout = Workout(
            name = "Leg day",
            date = LocalDate.of(2026, 3, 15),
            durationMinutes = 50,
            photoFileName = "a.jpg",
        )

        repository.add(workout)

        val stored = repository.observeWorkouts().first().single()
        assertEquals(workout.copy(id = stored.id), stored)
    }

    @Test
    fun observe_isNewestFirst() = runTest {
        repository.add(Workout(name = "Older", date = LocalDate.of(2026, 3, 1), durationMinutes = 10))
        repository.add(Workout(name = "Newer", date = LocalDate.of(2026, 3, 2), durationMinutes = 10))

        assertEquals(listOf("Newer", "Older"), repository.observeWorkouts().first().map { it.name })
    }

    @Test
    fun delete_removesTheWorkout() = runTest {
        repository.add(Workout(name = "Run", date = LocalDate.of(2026, 3, 1), durationMinutes = 10))
        val id = repository.observeWorkouts().first().single().id

        repository.delete(id)

        assertEquals(emptyList<Workout>(), repository.observeWorkouts().first())
    }

    @Test
    fun photoFileNames_listsOnlyAttachedPhotos() = runTest {
        val day = LocalDate.of(2026, 3, 1)
        repository.add(Workout(name = "A", date = day, durationMinutes = 10, photoFileName = "a.jpg"))
        repository.add(Workout(name = "B", date = day, durationMinutes = 10))
        repository.add(Workout(name = "C", date = day, durationMinutes = 10, photoFileName = "c.jpg"))

        assertEquals(setOf("a.jpg", "c.jpg"), repository.photoFileNames())
    }
}
