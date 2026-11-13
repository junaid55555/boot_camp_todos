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
import com.fitnessmarket.bootcamptodo.domain.PhotoStore
import kotlinx.coroutines.CancellationException
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
    /** Photo taken for this workout, if any; a name in the [PhotoStore]. */
    val photoFileName: String? = null,
    /** Latest selectable date for the date picker. */
    val today: LocalDate,
)

class AddWorkoutViewModel(
    private val addWorkout: AddWorkoutUseCase,
    private val photoStore: PhotoStore,
    clock: Clock,
) : ViewModel() {

    private val _uiState = LocalDate.now(clock).let { MutableStateFlow(AddWorkoutUiState(date = it, today = it)) }
    val uiState: StateFlow<AddWorkoutUiState> = _uiState.asStateFlow()

    private val _saved = Channel<Unit>(Channel.BUFFERED)

    /** Emits once after the workout is stored; the screen navigates back. */
    val saved: Flow<Unit> = _saved.receiveAsFlow()

    private val _saveFailed = Channel<Unit>(Channel.BUFFERED)

    /** Emits once when storing the workout fails; the screen shows a message and the user can retry. */
    val saveFailed: Flow<Unit> = _saveFailed.receiveAsFlow()

    fun onNameChange(name: String) = _uiState.update {
        it.copy(name = name, errors = it.errors - setOf(AddWorkoutError.NAME_BLANK, AddWorkoutError.NAME_TOO_LONG))
    }

    fun onDateChange(date: LocalDate) = _uiState.update {
        it.copy(date = date, errors = it.errors - AddWorkoutError.DATE_IN_FUTURE)
    }

    fun onDurationChange(durationText: String) = _uiState.update {
        it.copy(durationText = durationText, errors = it.errors - AddWorkoutError.DURATION_INVALID)
    }

    /** Where the camera should write the next photo. Unused files are removed at the next app start. */
    fun newPhotoFileName(): String = photoStore.newFileName()

    fun onPhotoTaken(fileName: String) = _uiState.update { it.copy(photoFileName = fileName) }

    fun onRemovePhoto() = _uiState.update { it.copy(photoFileName = null) }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val result = try {
                addWorkout(state.name, state.date, state.durationText, state.photoFileName)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _saveFailed.send(Unit)
                return@launch
            }
            when (result) {
                AddWorkoutResult.Success -> {
                    // isSaving stays true: the screen is closing, and a second tap must not store a duplicate.
                    _uiState.update { it.copy(errors = emptySet()) }
                    _saved.send(Unit)
                }
                is AddWorkoutResult.Invalid ->
                    _uiState.update { it.copy(isSaving = false, errors = result.errors) }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AddWorkoutViewModel(container.addWorkout, container.photoStore, container.clock) }
        }
    }
}
