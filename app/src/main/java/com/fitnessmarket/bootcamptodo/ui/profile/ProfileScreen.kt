package com.fitnessmarket.bootcamptodo.ui.profile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
    ProfileContent(
        darkTheme = darkTheme ?: isSystemInDarkTheme(),
        onDarkThemeChange = viewModel::onDarkThemeChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(darkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_profile)) }) },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // The whole row is the toggle, so the Switch itself takes no click of its own.
            ListItem(
                headlineContent = { Text(stringResource(R.string.dark_theme)) },
                trailingContent = { Switch(checked = darkTheme, onCheckedChange = null) },
                modifier = Modifier.toggleable(
                    value = darkTheme,
                    onValueChange = onDarkThemeChange,
                    role = Role.Switch,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    BootCampTodoTheme { ProfileContent(darkTheme = false, onDarkThemeChange = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ProfileDarkPreview() {
    BootCampTodoTheme(darkTheme = true) { ProfileContent(darkTheme = true, onDarkThemeChange = {}) }
}
