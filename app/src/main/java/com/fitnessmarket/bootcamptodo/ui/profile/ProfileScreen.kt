package com.fitnessmarket.bootcamptodo.ui.profile

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessmarket.bootcamptodo.R
import com.fitnessmarket.bootcamptodo.domain.ThemeMode
import com.fitnessmarket.bootcamptodo.ui.theme.BootCampTodoTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appVersion = remember(context) { context.appVersionName() }
    ProfileContent(
        themeMode = themeMode,
        appVersion = appVersion,
        onThemeModeChange = viewModel::onThemeModeChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(themeMode: ThemeMode, appVersion: String, onThemeModeChange: (ThemeMode) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_profile)) }) },
    ) { innerPadding ->
        // Scrolls so nothing is cut off in landscape or at large font sizes.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            // On tablets and in landscape, a full-width list is hard to read, so cap the width.
            Column(
                modifier = Modifier
                    .widthIn(max = CONTENT_MAX_WIDTH)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileHeader()

                SectionHeader(R.string.section_appearance)
                SettingsCard(Modifier.selectableGroup()) {
                    THEME_OPTIONS.forEachIndexed { index, option ->
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        ThemeOptionRow(
                            option = option,
                            selected = option.mode == themeMode,
                            onClick = { onThemeModeChange(option.mode) },
                        )
                    }
                }

                SectionHeader(R.string.section_about)
                SettingsCard {
                    InfoRow(
                        icon = R.drawable.ic_info,
                        title = stringResource(R.string.app_version),
                        description = appVersion,
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    InfoRow(
                        icon = R.drawable.ic_lock,
                        title = stringResource(R.string.data_storage),
                        description = stringResource(R.string.data_storage_description),
                    )
                }
            }
        }
    }
}

private val CONTENT_MAX_WIDTH = 640.dp
private val AVATAR_SIZE = 64.dp

private class ThemeOption(
    val mode: ThemeMode,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @StringRes val description: Int? = null,
)

private val THEME_OPTIONS = listOf(
    ThemeOption(mode = ThemeMode.LIGHT, label = R.string.theme_light, icon = R.drawable.ic_light_mode),
    ThemeOption(mode = ThemeMode.DARK, label = R.string.dark_theme, icon = R.drawable.ic_dark_mode),
    ThemeOption(
        mode = ThemeMode.SYSTEM,
        label = R.string.theme_system,
        icon = R.drawable.ic_contrast,
        description = R.string.theme_system_description,
    ),
)

@Composable
private fun ProfileHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(AVATAR_SIZE),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_person), contentDescription = null, modifier = Modifier.size(36.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.profile_heading),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.profile_subheading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionHeader(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            .semantics { heading() },
    )
}

@Composable
private fun SettingsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(content = content)
    }
}

/** The whole row is the touch target, so the radio button itself takes no click of its own. */
@Composable
private fun ThemeOptionRow(option: ThemeOption, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(option.label)) },
        supportingContent = option.description?.let { { Text(stringResource(it)) } },
        leadingContent = { Icon(painterResource(option.icon), contentDescription = null) },
        trailingContent = { RadioButton(selected = selected, onClick = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}

@Composable
private fun InfoRow(@DrawableRes icon: Int, title: String, description: String) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = { Icon(painterResource(icon), contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

private fun Context.appVersionName(): String {
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
    return info.versionName.orEmpty()
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    BootCampTodoTheme(darkTheme = false) {
        ProfileContent(themeMode = ThemeMode.LIGHT, appVersion = "1.0", onThemeModeChange = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileDarkPreview() {
    BootCampTodoTheme(darkTheme = true) {
        ProfileContent(themeMode = ThemeMode.DARK, appVersion = "1.0", onThemeModeChange = {})
    }
}

@Preview(showBackground = true, fontScale = 2f, widthDp = 320)
@Composable
private fun ProfileLargeFontPreview() {
    BootCampTodoTheme(darkTheme = false) {
        ProfileContent(themeMode = ThemeMode.SYSTEM, appVersion = "1.0", onThemeModeChange = {})
    }
}
