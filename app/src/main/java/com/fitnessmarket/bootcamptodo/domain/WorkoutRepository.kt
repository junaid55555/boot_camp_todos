package com.fitnessmarket.bootcamptodo.domain

import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    /** Workouts ordered newest first (date descending, then most recently added). */
    fun observeWorkouts(): Flow<List<Workout>>

    suspend fun add(workout: Workout)

    suspend fun delete(id: Long)

    /** Photo file names currently attached to a workout. */
    suspend fun photoFileNames(): Set<String>
}
