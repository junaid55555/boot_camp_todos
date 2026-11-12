package com.fitnessmarket.bootcamptodo.domain

class DeleteWorkoutUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
