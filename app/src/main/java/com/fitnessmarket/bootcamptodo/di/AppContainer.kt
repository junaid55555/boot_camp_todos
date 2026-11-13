package com.fitnessmarket.bootcamptodo.di

import android.content.Context
import androidx.room.Room
import com.fitnessmarket.bootcamptodo.data.FilePhotoStore
import com.fitnessmarket.bootcamptodo.data.RoomWorkoutRepository
import com.fitnessmarket.bootcamptodo.data.SharedPrefsSettingsRepository
import com.fitnessmarket.bootcamptodo.data.WorkoutDatabase
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.CleanUpOrphanPhotosUseCase
import com.fitnessmarket.bootcamptodo.domain.DeleteWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.FilterWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.GetWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.SettingsRepository
import com.fitnessmarket.bootcamptodo.domain.WorkoutRepository
import java.io.File
import java.time.Clock

/**
 * Hand-written DI: builds the app's singletons once, from [com.fitnessmarket.bootcamptodo.WorkoutApp].
 * UI tests build their own with an in-memory [database].
 */
class AppContainer(
    context: Context,
    database: WorkoutDatabase = Room.databaseBuilder(
        context.applicationContext,
        WorkoutDatabase::class.java,
        "workouts.db",
    ).build(),
) {

    val clock: Clock = Clock.systemDefaultZone()

    private val repository: WorkoutRepository = RoomWorkoutRepository(database.workoutDao())

    /** Concrete type so the UI can resolve a photo's [java.io.File]. */
    val photoStore = FilePhotoStore(File(context.filesDir, "photos"))

    val getWorkouts = GetWorkoutsUseCase(repository)
    val addWorkout = AddWorkoutUseCase(repository, clock)
    val deleteWorkout = DeleteWorkoutUseCase(repository, photoStore)
    val filterWorkouts = FilterWorkoutsUseCase(clock)
    val cleanUpOrphanPhotos = CleanUpOrphanPhotosUseCase(repository, photoStore)

    val settings: SettingsRepository = SharedPrefsSettingsRepository(context)
}
