package com.fitnessmarket.bootcamptodo.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var db: WorkoutDatabase
    private lateinit var dao: WorkoutDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WorkoutDatabase::class.java,
        ).build()
        dao = db.workoutDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun observeAll_ordersByDateDescThenIdDesc() = runTest {
        val oldId = dao.insert(WorkoutEntity(name = "Old", dateEpochDay = 100, durationMinutes = 10))
        val newerFirstId = dao.insert(WorkoutEntity(name = "Same day A", dateEpochDay = 200, durationMinutes = 20))
        val newerSecondId = dao.insert(WorkoutEntity(name = "Same day B", dateEpochDay = 200, durationMinutes = 30))

        val ids = dao.observeAll().first().map { it.id }

        assertEquals(listOf(newerSecondId, newerFirstId, oldId), ids)
    }

    @Test
    fun deleteById_removesOnlyThatWorkout() = runTest {
        val keepId = dao.insert(WorkoutEntity(name = "Keep", dateEpochDay = 100, durationMinutes = 10))
        val deleteId = dao.insert(WorkoutEntity(name = "Delete", dateEpochDay = 100, durationMinutes = 10))

        dao.deleteById(deleteId)

        assertEquals(listOf(keepId), dao.observeAll().first().map { it.id })
    }

    @Test
    fun photoFileName_roundTripsAndIsListed() = runTest {
        val workout = WorkoutEntity(name = "Run", dateEpochDay = 100, durationMinutes = 10)
        dao.insert(workout.copy(photoFileName = "a.jpg"))
        dao.insert(workout)

        assertEquals(listOf(null, "a.jpg"), dao.observeAll().first().map { it.photoFileName })
        assertEquals(listOf("a.jpg"), dao.photoFileNames())
    }
}
