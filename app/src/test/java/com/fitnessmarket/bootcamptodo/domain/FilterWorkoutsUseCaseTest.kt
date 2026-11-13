package com.fitnessmarket.bootcamptodo.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class FilterWorkoutsUseCaseTest {

    private val today = LocalDate.of(2026, 3, 15)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val filterWorkouts = FilterWorkoutsUseCase(clock)

    private val run = workout(id = 1, name = "Morning run", date = today)
    private val yoga = workout(id = 2, name = "Yoga", date = today.minusDays(6))
    private val legDay = workout(id = 3, name = "Leg day", date = today.minusDays(7))
    private val swim = workout(id = 4, name = "Swim", date = today.minusDays(30))

    private val all = listOf(run, yoga, legDay, swim)

    @Test
    fun `no query and all dates returns every workout in the given order`() {
        val result = filterWorkouts(all, query = "", dateFilter = DateFilter.ALL)

        assertEquals(all, result)
    }

    @Test
    fun `query matches the name ignoring case and surrounding spaces`() {
        val result = filterWorkouts(all, query = "  RUN ", dateFilter = DateFilter.ALL)

        assertEquals(listOf(run), result)
    }

    @Test
    fun `blank query matches everything`() {
        val result = filterWorkouts(all, query = "   ", dateFilter = DateFilter.ALL)

        assertEquals(all, result)
    }

    @Test
    fun `today keeps only workouts dated today`() {
        val result = filterWorkouts(all, query = "", dateFilter = DateFilter.TODAY)

        assertEquals(listOf(run), result)
    }

    @Test
    fun `last 7 days includes today and the 6 days before it`() {
        val result = filterWorkouts(all, query = "", dateFilter = DateFilter.LAST_7_DAYS)

        assertEquals(listOf(run, yoga), result)
    }

    @Test
    fun `last 30 days includes today and the 29 days before it`() {
        val result = filterWorkouts(all, query = "", dateFilter = DateFilter.LAST_30_DAYS)

        assertEquals(listOf(run, yoga, legDay), result)
    }

    @Test
    fun `query and date filter combine`() {
        val result = filterWorkouts(all, query = "a", dateFilter = DateFilter.LAST_7_DAYS)

        assertEquals(listOf(yoga), result)
    }

    private fun workout(id: Long, name: String, date: LocalDate) =
        Workout(id = id, name = name, date = date, durationMinutes = 30)
}
