package com.fitnessmarket.bootcamptodo.domain

import kotlinx.coroutines.flow.Flow

class GetWorkoutsUseCase(private val repository: WorkoutRepository) {
    operator fun invoke(): Flow<List<Workout>> = repository.observeWorkouts()
}
