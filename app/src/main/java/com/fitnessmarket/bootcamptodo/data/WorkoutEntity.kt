package com.fitnessmarket.bootcamptodo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** [java.time.LocalDate.toEpochDay] of the workout date. */
    val dateEpochDay: Long,
    val durationMinutes: Int,
)
