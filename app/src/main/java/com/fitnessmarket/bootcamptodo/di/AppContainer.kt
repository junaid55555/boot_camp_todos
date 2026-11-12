package com.fitnessmarket.bootcamptodo.di

import android.content.Context
import androidx.room.Room
import com.fitnessmarket.bootcamptodo.data.RoomWorkoutRepository
import com.fitnessmarket.bootcamptodo.data.WorkoutDatabase
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.DeleteWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.GetWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.WorkoutRepository
import java.time.Clock

/** Hand-written DI: builds the app's singletons once, from [com.fitnessmarket.bootcamptodo.WorkoutApp]. */
class AppContainer(context: Context) {

    val clock: Clock = Clock.systemDefaultZone()

    private val database: WorkoutDatabase = Room.databaseBuilder(
        context.applicationContext,
        WorkoutDatabase::class.java,
        "workouts.db"
    ).build()

    private val repository: WorkoutRepository = RoomWorkoutRepository(database.workoutDao())

    val getWorkouts = GetWorkoutsUseCase(repository)
    val addWorkout = AddWorkoutUseCase(repository, clock)
    val deleteWorkout = DeleteWorkoutUseCase(repository)
}
