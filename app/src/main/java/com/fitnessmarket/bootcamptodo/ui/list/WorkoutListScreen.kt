package com.fitnessmarket.bootcamptodo.ui.list

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.domain.DateFilter
import com.fitnessmarket.bootcamptodo.domain.Workout
import com.fitnessmarket.bootcamptodo.ui.photo.WorkoutPhoto
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun WorkoutListScreen(viewModel: WorkoutListViewModel, photoFile: (String) -> File, onAddClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkoutListContent(
        uiState = uiState,
        photoFile = photoFile,
        onAddClick = onAddClick,
        onDelete = viewModel::onDelete,
        onQueryChange = viewModel::onQueryChange,
        onDateFilterChange = viewModel::onDateFilterChange,
        onClearFilters = viewModel::onClearFilters,
        onRetry = viewModel::onRetry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListContent(
    uiState: WorkoutListUiState,
    photoFile: (String) -> File,
    onAddClick: () -> Unit,
    onDelete: (Workout) -> Unit,
    onQueryChange: (String) -> Unit,
    onDateFilterChange: (DateFilter) -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_workout))
            }
        },
    ) { innerPadding ->
        // Centred and width-capped so cards stay readable on tablets and in landscape.
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(Modifier.fillMaxSize().widthIn(max = MAX_CONTENT_WIDTH)) {
                val showControls = !uiState.isLoading && !uiState.loadFailed && uiState.hasWorkouts
                if (showControls) {
                    SearchAndFilters(
                        query = uiState.query,
                        dateFilter = uiState.dateFilter,
                        onQueryChange = onQueryChange,
                        onDateFilterChange = onDateFilterChange,
                    )
                }
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    when {
                        uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        uiState.loadFailed -> ListMessage(
                            icon = R.drawable.ic_error,
                            title = R.string.load_error,
                            body = R.string.load_error_body,
                            actionLabel = R.string.retry,
                            onAction = onRetry,
                            isError = true,
                        )
                        !uiState.hasWorkouts -> ListMessage(
                            icon = R.drawable.ic_fitness,
                            title = R.string.empty_workouts,
                            body = R.string.empty_workouts_body,
                            actionLabel = R.string.log_first_workout,
                            onAction = onAddClick,
                        )
                        uiState.workouts.isEmpty() -> ListMessage(
                            icon = R.drawable.ic_search,
                            title = R.string.no_matches,
                            body = R.string.no_matches_body,
                            actionLabel = R.string.clear_filters,
                            onAction = onClearFilters,
                            isPrimaryAction = false,
                        )
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp,
                                bottom = FAB_CLEARANCE,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.workouts, key = { it.id }) { workout ->
                                WorkoutCard(
                                    workout = workout,
                                    photoFile = photoFile,
                                    onDelete = { onDelete(workout) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val THUMBNAIL_SIZE = 72.dp
private val MAX_CONTENT_WIDTH = 720.dp

// FAB height (56) + its margin (16) + breathing room, so the last card's delete button isn't covered.
private val FAB_CLEARANCE = 88.dp

@Composable
private fun SearchAndFilters(
    query: String,
    dateFilter: DateFilter,
    onQueryChange: (String) -> Unit,
    onDateFilterChange: (DateFilter) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.search_workouts)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(painterResource(R.drawable.ic_close), stringResource(R.string.clear_search))
                    }
                }
            } else {
                null
            },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )
        // Scrolls sideways rather than wrapping, so the row keeps one height at large font sizes.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == dateFilter,
                    onClick = { onDateFilterChange(filter) },
                    label = { Text(stringResource(filter.label)) },
                )
            }
        }
    }
}

@get:StringRes
private val DateFilter.label: Int
    get() = when (this) {
        DateFilter.ALL -> R.string.filter_all
        DateFilter.TODAY -> R.string.filter_today
        DateFilter.LAST_7_DAYS -> R.string.filter_last_7_days
        DateFilter.LAST_30_DAYS -> R.string.filter_last_30_days
    }

@Composable
private fun WorkoutCard(
    workout: Workout,
    photoFile: (String) -> File,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WorkoutThumbnail(workout.photoFileName, photoFile)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // No maxLines: long names wrap instead of being cut off.
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MetaLabel(R.drawable.ic_calendar, workout.date.format(dateFormatter))
                    MetaLabel(R.drawable.ic_timer, stringResource(R.string.workout_duration, workout.durationMinutes))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.delete_workout, workout.name),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WorkoutThumbnail(photoFileName: String?, photoFile: (String) -> File) {
    val modifier = Modifier.size(THUMBNAIL_SIZE).clip(MaterialTheme.shapes.medium)
    if (photoFileName != null) {
        WorkoutPhoto(
            file = photoFile(photoFileName),
            contentDescription = stringResource(R.string.workout_photo),
            targetSize = THUMBNAIL_SIZE,
            modifier = modifier,
        )
    } else {
        // Decorative: keeps the cards aligned and tells "no photo" apart from a photo still loading.
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_fitness),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun MetaLabel(@DrawableRes icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Centred icon, title, body and one action: used for the empty, no-match and error states. */
@Composable
private fun ListMessage(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @StringRes body: Int,
    @StringRes actionLabel: Int,
    onAction: () -> Unit,
    isError: Boolean = false,
    isPrimaryAction: Boolean = true,
) {
    val (badgeColor, iconColor) = if (isError) {
        MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
    // Scrollable so short landscape screens and large fonts never clip the action.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(badgeColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = iconColor, modifier = Modifier.size(40.dp))
        }
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (isPrimaryAction) {
            Button(onClick = onAction, modifier = Modifier.padding(top = 4.dp)) { Text(stringResource(actionLabel)) }
        } else {
            TextButton(onClick = onAction) { Text(stringResource(actionLabel)) }
        }
    }
}

private val previewWorkouts = listOf(
    Workout(
        id = 3,
        name = "Morning run along the river, then hill sprints and a long cool-down stretch",
        date = LocalDate.of(2026, 3, 15),
        durationMinutes = 55,
        photoFileName = "preview.jpg",
    ),
    Workout(id = 2, name = "Yoga", date = LocalDate.of(2026, 3, 14), durationMinutes = 45),
    Workout(id = 1, name = "Leg day", date = LocalDate.of(2026, 3, 12), durationMinutes = 60),
)

@Composable
private fun PreviewContent(uiState: WorkoutListUiState) {
    WorkoutListContent(
        uiState = uiState,
        photoFile = ::File,
        onAddClick = {},
        onDelete = {},
        onQueryChange = {},
        onDateFilterChange = {},
        onClearFilters = {},
        onRetry = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun WorkoutListPreview() {
    BootCampTodoTheme {
        PreviewContent(WorkoutListUiState(workouts = previewWorkouts, hasWorkouts = true, isLoading = false))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WorkoutListDarkPreview() {
    BootCampTodoTheme(darkTheme = true) {
        PreviewContent(
            WorkoutListUiState(
                workouts = previewWorkouts.take(1),
                hasWorkouts = true,
                query = "run",
                dateFilter = DateFilter.LAST_7_DAYS,
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 840, heightDp = 400)
@Composable
private fun WorkoutListLandscapePreview() {
    BootCampTodoTheme {
        PreviewContent(WorkoutListUiState(workouts = previewWorkouts, hasWorkouts = true, isLoading = false))
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutListEmptyPreview() {
    BootCampTodoTheme {
        PreviewContent(WorkoutListUiState(isLoading = false))
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutListNoMatchesPreview() {
    BootCampTodoTheme {
        PreviewContent(
            WorkoutListUiState(hasWorkouts = true, query = "swim", isLoading = false),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutListErrorPreview() {
    BootCampTodoTheme {
        PreviewContent(WorkoutListUiState(isLoading = false, loadFailed = true))
    }
}
