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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutError
import com.fitnessmarket.bootcamptodo.domain.AddWorkoutUseCase
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
    val saveFailedMessage = stringResource(R.string.save_failed)

    LaunchedEffect(viewModel) {
        launch { viewModel.saved.collect { currentOnBack() } }
        launch { viewModel.saveFailed.collect { snackbarHostState.showSnackbar(saveFailedMessage) } }
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
    val durationFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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
        // The app draws edge to edge, so adjustResize doesn't shrink the window: pad for the keyboard here.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = MAX_FORM_WIDTH)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SectionHeader(stringResource(R.string.section_details))

                NameField(
                    name = uiState.name,
                    errors = uiState.errors,
                    onNameChange = onNameChange,
                    onNext = { durationFocus.requestFocus() },
                )

                DateField(
                    date = uiState.date,
                    today = uiState.today,
                    isError = AddWorkoutError.DATE_IN_FUTURE in uiState.errors,
                    onClick = { showDatePicker = true },
                )

                DurationField(
                    durationText = uiState.durationText,
                    isError = AddWorkoutError.DURATION_INVALID in uiState.errors,
                    onDurationChange = onDurationChange,
                    onDone = {
                        focusManager.clearFocus()
                        onSave()
                    },
                    modifier = Modifier.focusRequester(durationFocus),
                )

                Spacer(Modifier.size(SECTION_GAP))
                SectionHeader(stringResource(R.string.section_photo))

                PhotoSection(
                    photoFile = uiState.photoFileName?.let(photoFile),
                    onTakePhoto = onTakePhoto,
                    onRemovePhoto = onRemovePhoto,
                )

                Spacer(Modifier.size(SECTION_GAP))
                SaveButton(isSaving = uiState.isSaving, onSave = onSave)
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

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun NameField(name: String, errors: Set<AddWorkoutError>, onNameChange: (String) -> Unit, onNext: () -> Unit) {
    val error = when {
        AddWorkoutError.NAME_BLANK in errors -> R.string.error_name_blank
        AddWorkoutError.NAME_TOO_LONG in errors -> R.string.error_name_too_long
        else -> null
    }
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.field_name)) },
        placeholder = { Text(stringResource(R.string.name_placeholder)) },
        singleLine = true,
        isError = error != null,
        supportingText = {
            Row {
                Text(error?.let { stringResource(it) }.orEmpty(), modifier = Modifier.weight(1f))
                // Counts the trimmed name, as that is what the length rule checks.
                Text(stringResource(R.string.name_counter, name.trim().length, AddWorkoutUseCase.MAX_NAME_LENGTH))
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Read-only field that opens the date picker when tapped; says how long ago the date is. */
@Composable
private fun DateField(date: LocalDate, today: LocalDate, isError: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { if (it is PressInteraction.Release) currentOnClick() }
    }

    val supporting = if (isError) stringResource(R.string.error_date_in_future) else relativeDate(date, today)
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
        supportingText = supporting?.let { { Text(it) } },
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun relativeDate(date: LocalDate, today: LocalDate): String? {
    val daysAgo = (today.toEpochDay() - date.toEpochDay()).toInt()
    return when {
        daysAgo == 0 -> stringResource(R.string.date_today)
        daysAgo == 1 -> stringResource(R.string.date_yesterday)
        daysAgo > 1 -> pluralStringResource(R.plurals.date_days_ago, daysAgo, daysAgo)
        else -> null
    }
}

@Composable
private fun DurationField(
    durationText: String,
    isError: Boolean,
    onDurationChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val range = AddWorkoutUseCase.DURATION_RANGE
    OutlinedTextField(
        value = durationText,
        onValueChange = onDurationChange,
        label = { Text(stringResource(R.string.field_duration)) },
        singleLine = true,
        isError = isError,
        supportingText = {
            Text(
                if (isError) {
                    stringResource(R.string.error_duration_invalid)
                } else {
                    stringResource(R.string.duration_hint, range.first, range.last)
                },
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier.fillMaxWidth(),
    )
}

/** A tappable "Add photo" card, or the photo preview with Retake / Remove once one is taken. */
@Composable
private fun PhotoSection(photoFile: File?, onTakePhoto: () -> Unit, onRemovePhoto: () -> Unit) {
    if (photoFile == null) {
        OutlinedCard(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_photo_camera),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
                Text(stringResource(R.string.add_photo), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.add_photo_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    WorkoutPhoto(
        file = photoFile,
        contentDescription = stringResource(R.string.workout_photo),
        targetSize = PHOTO_DECODE_SIZE,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(PHOTO_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.medium),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onTakePhoto, modifier = Modifier.weight(1f)) {
            Icon(
                painterResource(R.drawable.ic_photo_camera),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.retake_photo))
        }
        OutlinedButton(
            onClick = onRemovePhoto,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                painterResource(R.drawable.ic_delete),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.remove_photo))
        }
    }
}

/** Disabled with a spinner while saving, so one tap stores one workout. */
@Composable
private fun SaveButton(isSaving: Boolean, onSave: () -> Unit) {
    Button(
        onClick = onSave,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        if (isSaving) {
            val savingDescription = stringResource(R.string.saving)
            CircularProgressIndicator(
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(ButtonDefaults.IconSize)
                    .semantics { contentDescription = savingDescription },
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(stringResource(R.string.save))
    }
}

/** Keeps the form readable on tablets and in landscape. */
private val MAX_FORM_WIDTH = 600.dp
private val SECTION_GAP = 8.dp
private const val PHOTO_ASPECT_RATIO = 4f / 3f

/** Shorter side to decode the preview at; enough for the preview at [MAX_FORM_WIDTH] without full-size bitmaps. */
private val PHOTO_DECODE_SIZE = 360.dp

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
private fun AddWorkoutErrorsPreview() {
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

@Preview(showBackground = true)
@Composable
private fun AddWorkoutSavingDarkPreview() {
    val today = LocalDate.of(2026, 3, 15)
    BootCampTodoTheme(darkTheme = true) {
        AddWorkoutContent(
            uiState = AddWorkoutUiState(
                name = "Leg day",
                date = today.minusDays(2),
                durationText = "50",
                isSaving = true,
                photoFileName = "preview.jpg",
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
