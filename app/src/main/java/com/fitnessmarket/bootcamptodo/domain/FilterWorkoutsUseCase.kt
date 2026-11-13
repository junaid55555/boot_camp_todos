package com.fitnessmarket.bootcamptodo.domain

import java.time.Clock
import java.time.LocalDate

/** How far back the list goes; [days] counts today, null means no limit. */
enum class DateFilter(val days: Long?) {
    ALL(null),
    TODAY(1),
    LAST_7_DAYS(7),
    LAST_30_DAYS(30),
}

class FilterWorkoutsUseCase(private val clock: Clock) {
    /** Keeps [workouts] whose name contains [query] (ignoring case) and whose date is within [dateFilter]. */
    operator fun invoke(workouts: List<Workout>, query: String, dateFilter: DateFilter): List<Workout> {
        val trimmedQuery = query.trim()
        val earliest = dateFilter.days?.let { LocalDate.now(clock).minusDays(it - 1) }
        return workouts.filter { workout ->
            (earliest == null || !workout.date.isBefore(earliest)) &&
                workout.name.contains(trimmedQuery, ignoreCase = true)
        }
    }
}
