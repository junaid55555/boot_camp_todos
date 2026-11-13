package com.fitnessmarket.bootcamptodo.domain

/**
 * Deletes photos no workout points to: left behind by a cancelled camera, a retake, a removed photo,
 * or an add screen closed without saving. Runs once at app start.
 */
class CleanUpOrphanPhotosUseCase(
    private val repository: WorkoutRepository,
    private val photoStore: PhotoStore,
) {
    suspend operator fun invoke() {
        val referenced = repository.photoFileNames()
        photoStore.listFileNames()
            .filterNot { it in referenced }
            .forEach(photoStore::delete)
    }
}
