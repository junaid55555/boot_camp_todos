package com.fitnessmarket.bootcamptodo.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessmarket.bootcamptodo.R
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListContent(
    uiState: WorkoutListUiState,
    photoFile: (String) -> File,
    onAddClick: () -> Unit,
    onDelete: (Workout) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_workout))
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.workouts.isEmpty() -> Text(
                    text = stringResource(R.string.empty_workouts),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.workouts, key = { it.id }) { workout ->
                        WorkoutRow(workout = workout, photoFile = photoFile, onDelete = { onDelete(workout) })
                    }
                }
            }
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val THUMBNAIL_SIZE = 56.dp

@Composable
private fun WorkoutRow(workout: Workout, photoFile: (String) -> File, onDelete: () -> Unit) {
    Card {
        ListItem(
            leadingContent = workout.photoFileName?.let { fileName ->
                {
                    WorkoutPhoto(
                        file = photoFile(fileName),
                        contentDescription = stringResource(R.string.workout_photo),
                        targetSize = THUMBNAIL_SIZE,
                        modifier = Modifier.size(THUMBNAIL_SIZE).clip(MaterialTheme.shapes.small),
                    )
                }
            },
            headlineContent = { Text(workout.name) },
            supportingContent = {
                Text(
                    stringResource(
                        R.string.workout_summary,
                        workout.date.format(dateFormatter),
                        workout.durationMinutes,
                    ),
                )
            },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(
                        painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete_workout, workout.name),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutListPreview() {
    BootCampTodoTheme {
        WorkoutListContent(
            uiState = WorkoutListUiState(
                workouts = listOf(
                    Workout(
                        id = 2,
                        name = "Morning run",
                        date = LocalDate.of(2026, 3, 15),
                        durationMinutes = 30,
                        photoFileName = "preview.jpg",
                    ),
                    Workout(id = 1, name = "Yoga", date = LocalDate.of(2026, 3, 14), durationMinutes = 45),
                ),
                isLoading = false,
            ),
            photoFile = ::File,
            onAddClick = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutListEmptyPreview() {
    BootCampTodoTheme {
        WorkoutListContent(
            uiState = WorkoutListUiState(isLoading = false),
            photoFile = ::File,
            onAddClick = {},
            onDelete = {},
        )
    }
}
