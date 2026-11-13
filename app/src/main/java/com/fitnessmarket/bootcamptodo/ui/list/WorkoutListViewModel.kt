package com.fitnessmarket.bootcamptodo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fitnessmarket.bootcamptodo.di.AppContainer
import com.fitnessmarket.bootcamptodo.domain.DateFilter
import com.fitnessmarket.bootcamptodo.domain.DeleteWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.FilterWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.GetWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.Workout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutListUiState(
    /** Workouts matching [query] and [dateFilter], newest first. */
    val workouts: List<Workout> = emptyList(),
    /** Whether any workout is stored at all, so "nothing logged" and "nothing matches" can differ. */
    val hasWorkouts: Boolean = false,
    val query: String = "",
    val dateFilter: DateFilter = DateFilter.ALL,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
) {
    val isFiltering: Boolean get() = query.isNotBlank() || dateFilter != DateFilter.ALL
}

class WorkoutListViewModel(
    private val getWorkouts: GetWorkoutsUseCase,
    private val deleteWorkout: DeleteWorkoutUseCase,
    private val filterWorkouts: FilterWorkoutsUseCase,
) : ViewModel() {

    // Updated synchronously (not combined from flows) so the search field never lags behind typing.
    private val _uiState = MutableStateFlow(WorkoutListUiState())
    val uiState: StateFlow<WorkoutListUiState> = _uiState.asStateFlow()

    private var allWorkouts: List<Workout> = emptyList()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query).filtered() }

    fun onDateFilterChange(dateFilter: DateFilter) = _uiState.update { it.copy(dateFilter = dateFilter).filtered() }

    fun onClearFilters() = _uiState.update { it.copy(query = "", dateFilter = DateFilter.ALL).filtered() }

    fun onRetry() = load()

    fun onDelete(workout: Workout) {
        viewModelScope.launch { deleteWorkout(workout) }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, loadFailed = false) }
        loadJob = viewModelScope.launch {
            getWorkouts()
                .catch { _uiState.update { it.copy(isLoading = false, loadFailed = true) } }
                .collect { workouts ->
                    allWorkouts = workouts
                    _uiState.update {
                        it.copy(hasWorkouts = workouts.isNotEmpty(), isLoading = false, loadFailed = false).filtered()
                    }
                }
        }
    }

    private fun WorkoutListUiState.filtered() = copy(workouts = filterWorkouts(allWorkouts, query, dateFilter))

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WorkoutListViewModel(container.getWorkouts, container.deleteWorkout, container.filterWorkouts)
            }
        }
    }
}
