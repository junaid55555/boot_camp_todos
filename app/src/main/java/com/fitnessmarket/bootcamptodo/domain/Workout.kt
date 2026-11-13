package com.fitnessmarket.bootcamptodo.domain

import java.time.LocalDate

data class Workout(
    val id: Long = 0,
    val name: String,
    val date: LocalDate,
    val durationMinutes: Int,
    /** Name of the optional photo in the [PhotoStore]. */
    val photoFileName: String? = null,
)
