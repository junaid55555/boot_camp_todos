package com.fitnessmarket.bootcamptodo.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fitnessmarket.bootcamptodo.di.AppContainer
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutError
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutResult
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

data class AddWorkoutUiState(
    val name: String = "",
    val date: LocalDate,
    val durationText: String = "",
    val errors: Set<AddWorkoutError> = emptySet(),
    val isSaving: Boolean = false,
    /** Latest selectable date for the date picker. */
    val today: LocalDate,
)

class AddWorkoutViewModel(
    private val addWorkout: AddWorkoutUseCase,
    clock: Clock,
) : ViewModel() {

    private val _uiState = LocalDate.now(clock).let { MutableStateFlow(AddWorkoutUiState(date = it, today = it)) }
    val uiState: StateFlow<AddWorkoutUiState> = _uiState.asStateFlow()

    private val _saved = Channel<Unit>(Channel.BUFFERED)
    /** Emits once after the workout is stored; the screen navigates back. */
    val saved: Flow<Unit> = _saved.receiveAsFlow()

    fun onNameChange(name: String) = _uiState.update {
        it.copy(name = name, errors = it.errors - setOf(AddWorkoutError.NAME_BLANK, AddWorkoutError.NAME_TOO_LONG))
    }

    fun onDateChange(date: LocalDate) = _uiState.update {
        it.copy(date = date, errors = it.errors - AddWorkoutError.DATE_IN_FUTURE)
    }

    fun onDurationChange(durationText: String) = _uiState.update {
        it.copy(durationText = durationText, errors = it.errors - AddWorkoutError.DURATION_INVALID)
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            when (val result = addWorkout(state.name, state.date, state.durationText)) {
                AddWorkoutResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, errors = emptySet()) }
                    _saved.send(Unit)
                }
                is AddWorkoutResult.Invalid ->
                    _uiState.update { it.copy(isSaving = false, errors = result.errors) }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AddWorkoutViewModel(container.addWorkout, container.clock) }
        }
    }
}
