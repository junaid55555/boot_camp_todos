package com.fitnessmarket.bootcamptodo.domain

import java.time.Clock
import java.time.LocalDate

enum class AddWorkoutError { NAME_BLANK, NAME_TOO_LONG, DURATION_INVALID, DATE_IN_FUTURE }

sealed interface AddWorkoutResult {
    data object Success : AddWorkoutResult
    data class Invalid(val errors: Set<AddWorkoutError>) : AddWorkoutResult
}

class AddWorkoutUseCase(
    private val repository: WorkoutRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(name: String, date: LocalDate, durationText: String): AddWorkoutResult {
        val trimmedName = name.trim()
        val duration = durationText.trim().toIntOrNull()

        val errors = buildSet {
            when {
                trimmedName.isEmpty() -> add(AddWorkoutError.NAME_BLANK)
                trimmedName.length > MAX_NAME_LENGTH -> add(AddWorkoutError.NAME_TOO_LONG)
            }
            if (duration == null || duration !in DURATION_RANGE) add(AddWorkoutError.DURATION_INVALID)
            if (date.isAfter(LocalDate.now(clock))) add(AddWorkoutError.DATE_IN_FUTURE)
        }
        if (errors.isNotEmpty()) return AddWorkoutResult.Invalid(errors)

        repository.add(Workout(name = trimmedName, date = date, durationMinutes = duration!!))
        return AddWorkoutResult.Success
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
        val DURATION_RANGE = 1..1440
    }
}
