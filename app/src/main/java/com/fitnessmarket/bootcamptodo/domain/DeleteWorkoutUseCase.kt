package com.fitnessmarket.bootcamptodo.domain

class DeleteWorkoutUseCase(
    private val repository: WorkoutRepository,
    private val photoStore: PhotoStore,
) {
    suspend operator fun invoke(workout: Workout) {
        repository.delete(workout.id)
        workout.photoFileName?.let(photoStore::delete)
    }
}
