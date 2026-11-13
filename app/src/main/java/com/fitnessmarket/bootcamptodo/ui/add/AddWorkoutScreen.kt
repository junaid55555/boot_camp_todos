package com.fitnessmarket.bootcamptodo.ui.add

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutError
import com.fitnessmarket.bootcamptodo.ui.photo.WorkoutPhoto
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AddWorkoutScreen(viewModel: AddWorkoutViewModel, photoFile: (String) -> File, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val noCameraMessage = stringResource(R.string.no_camera_app)

    LaunchedEffect(viewModel) {
        viewModel.saved.collect { currentOnBack() }
    }

    // Saveable: the system may kill this process while the camera app is in front.
    var pendingPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        pendingPhoto?.let { if (taken) viewModel.onPhotoTaken(it) }
        pendingPhoto = null
    }
    val onTakePhoto: () -> Unit = {
        val fileName = viewModel.newPhotoFileName()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile(fileName))
        try {
            pendingPhoto = fileName
            takePicture.launch(uri)
        } catch (_: ActivityNotFoundException) {
            pendingPhoto = null
            scope.launch { snackbarHostState.showSnackbar(noCameraMessage) }
        }
    }

    AddWorkoutContent(
        uiState = uiState,
        photoFile = photoFile,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::onNameChange,
        onDateChange = viewModel::onDateChange,
        onDurationChange = viewModel::onDurationChange,
        onTakePhoto = onTakePhoto,
        onRemovePhoto = viewModel::onRemovePhoto,
        onSave = viewModel::onSave,
        onBack = onBack,
    )
}

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutContent(
    uiState: AddWorkoutUiState,
    photoFile: (String) -> File,
    onNameChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onDurationChange: (String) -> Unit,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_workout)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val nameError = when {
                AddWorkoutError.NAME_BLANK in uiState.errors -> R.string.error_name_blank
                AddWorkoutError.NAME_TOO_LONG in uiState.errors -> R.string.error_name_too_long
                else -> null
            }
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.field_name)) },
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { { Text(stringResource(it)) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            DateField(
                date = uiState.date,
                isError = AddWorkoutError.DATE_IN_FUTURE in uiState.errors,
                onClick = { showDatePicker = true },
            )

            val durationError = AddWorkoutError.DURATION_INVALID in uiState.errors
            OutlinedTextField(
                value = uiState.durationText,
                onValueChange = onDurationChange,
                label = { Text(stringResource(R.string.field_duration)) },
                singleLine = true,
                isError = durationError,
                supportingText = if (durationError) {
                    { Text(stringResource(R.string.error_duration_invalid)) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            PhotoSection(
                photoFile = uiState.photoFileName?.let(photoFile),
                onTakePhoto = onTakePhoto,
                onRemovePhoto = onRemovePhoto,
            )

            Button(
                onClick = onSave,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showDatePicker) {
        WorkoutDatePickerDialog(
            initialDate = uiState.date,
            maxDate = uiState.today,
            onConfirm = {
                onDateChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

/** Read-only field that opens the date picker when tapped. */
@Composable
private fun DateField(date: LocalDate, isError: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { if (it is PressInteraction.Release) currentOnClick() }
    }

    OutlinedTextField(
        value = date.format(dateFormatter),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.field_date)) },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(painterResource(R.drawable.ic_calendar), contentDescription = stringResource(R.string.pick_date))
            }
        },
        isError = isError,
        supportingText = if (isError) {
            { Text(stringResource(R.string.error_date_in_future)) }
        } else null,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** "Add photo" button, or the photo with Retake / Remove once one is taken. */
@Composable
private fun PhotoSection(photoFile: File?, onTakePhoto: () -> Unit, onRemovePhoto: () -> Unit) {
    if (photoFile == null) {
        OutlinedButton(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
            Icon(painterResource(R.drawable.ic_photo_camera), contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_photo))
        }
        return
    }

    WorkoutPhoto(
        file = photoFile,
        contentDescription = stringResource(R.string.workout_photo),
        targetSize = PHOTO_PREVIEW_HEIGHT,
        modifier = Modifier
            .fillMaxWidth()
            .height(PHOTO_PREVIEW_HEIGHT)
            .clip(MaterialTheme.shapes.medium),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onTakePhoto, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.retake_photo))
        }
        OutlinedButton(onClick = onRemovePhoto, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.remove_photo))
        }
    }
}

private val PHOTO_PREVIEW_HEIGHT = 200.dp

private const val MILLIS_PER_DAY = 86_400_000L

/** The M3 DatePicker works in UTC midnight millis; convert to and from epoch days. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDatePickerDialog(
    initialDate: LocalDate,
    maxDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val maxMillis = maxDate.toEpochDay() * MILLIS_PER_DAY
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * MILLIS_PER_DAY,
        yearRange = 2000..maxDate.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= maxMillis
            override fun isSelectableYear(year: Int) = year <= maxDate.year
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onConfirm(LocalDate.ofEpochDay(Math.floorDiv(it, MILLIS_PER_DAY))) }
                        ?: onDismiss()
                },
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    ) {
        DatePicker(state = state)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddWorkoutPreview() {
    val today = LocalDate.of(2026, 3, 15)
    BootCampTodoTheme {
        AddWorkoutContent(
            uiState = AddWorkoutUiState(
                name = "",
                date = today,
                durationText = "0",
                errors = setOf(AddWorkoutError.NAME_BLANK, AddWorkoutError.DURATION_INVALID),
                today = today,
            ),
            photoFile = ::File,
            onNameChange = {},
            onDateChange = {},
            onDurationChange = {},
            onTakePhoto = {},
            onRemovePhoto = {},
            onSave = {},
            onBack = {},
        )
    }
}
