package com.fitnessmarket.bootcamptodo.data

import com.fitnessmarket.bootcamptodo.domain.Workout
import com.fitnessmarket.bootcamptodo.domain.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomWorkoutRepository(private val dao: WorkoutDao) : WorkoutRepository {

    override fun observeWorkouts(): Flow<List<Workout>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(workout: Workout) {
        dao.insert(workout.toEntity())
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun photoFileNames(): Set<String> = dao.photoFileNames().toSet()
}

private fun WorkoutEntity.toDomain() = Workout(
    id = id,
    name = name,
    date = LocalDate.ofEpochDay(dateEpochDay),
    durationMinutes = durationMinutes,
    photoFileName = photoFileName,
)

private fun Workout.toEntity() = WorkoutEntity(
    id = id,
    name = name,
    dateEpochDay = date.toEpochDay(),
    durationMinutes = durationMinutes,
    photoFileName = photoFileName,
)
