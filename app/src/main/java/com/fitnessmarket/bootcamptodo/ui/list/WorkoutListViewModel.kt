package com.fitnessmarket.bootcamptodo.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fitnessmarket.bootcamptodo.di.AppContainer
import com.fitnessmarket.bootcamptodo.domain.DeleteWorkoutUseCase
import com.fitnessmarket.bootcamptodo.domain.GetWorkoutsUseCase
import com.fitnessmarket.bootcamptodo.domain.Workout
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkoutListUiState(
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
)

class WorkoutListViewModel(
    getWorkouts: GetWorkoutsUseCase,
    private val deleteWorkout: DeleteWorkoutUseCase,
) : ViewModel() {

    val uiState: StateFlow<WorkoutListUiState> = getWorkouts()
        .map { WorkoutListUiState(workouts = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutListUiState())

    fun onDelete(id: Long) {
        viewModelScope.launch { deleteWorkout(id) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { WorkoutListViewModel(container.getWorkouts, container.deleteWorkout) }
        }
    }
}
