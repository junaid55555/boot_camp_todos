---
name: add-dark-theme
description: Add a user-controlled dark theme switch to a Jetpack Compose + Material 3 app (MVVM, hand-written DI), saved across restarts with SharedPreferences. Use when asked to add a dark mode / dark theme option, toggle or setting.
---

# Add a dark theme switch

Adds an on/off "Dark theme" switch that re-themes the whole app immediately and is remembered after restart. Until the user touches it, the app follows the system setting.

Follow the project's CLAUDE.md for layering, naming and the library rule. The recipe below uses **no new libraries**: SharedPreferences, not DataStore. If DataStore isn't on the approved list, don't add it without asking.

## Before you start

1. Read CLAUDE.md, `ui/theme/Theme.kt`, `MainActivity.kt`, the DI container and the screen the switch will live on (usually Profile/Settings).
2. Check whether a `SettingsRepository` already exists. If so, add a key to it instead of creating a second one.
3. Confirm the theme composable takes a `darkTheme: Boolean` parameter (the template's `XxxTheme(darkTheme = isSystemInDarkTheme())` does).

## Steps (TDD: write the test first and watch it fail)

### 1. Test: `app/src/test/.../ui/<screen>/<Screen>ViewModelTest.kt`
Mock `SettingsRepository` with MockK, backed by a `MutableStateFlow<Boolean?>(null)`:
- the value is `null` until the user picks one
- the ViewModel's flow follows the saved value
- `onDarkThemeChange(true)` / `(false)` calls `setDarkTheme` with that value

Run `./gradlew testDebugUnitTest` and confirm it fails to compile (red).

### 2. Domain: `domain/SettingsRepository.kt`
```kotlin
interface SettingsRepository {
    /** The user's dark theme choice, or null when they haven't picked one (follow the system). */
    val darkTheme: StateFlow<Boolean?>
    fun setDarkTheme(enabled: Boolean)
}
```
`Boolean?` matters: `null` means "follow the system", so a phone switched to dark mode still gets a dark app until the user overrides it.

### 3. Data: `data/SharedPrefsSettingsRepository.kt`
- Use `context.applicationContext.getSharedPreferences("settings", MODE_PRIVATE)`.
- Read the value **synchronously in the constructor** into a `MutableStateFlow`. Use `contains(key)` to tell "unset" (null) apart from `false`. Reading it up front means the first frame already has the right theme, with no light-to-dark flash.
- `setDarkTheme` writes with `prefs.edit { putBoolean(...) }` (apply, in the background) and updates the flow.

### 4. DI
Add `val settings: SettingsRepository = SharedPrefsSettingsRepository(context)` to the container. It must be a single instance, because MainActivity and the ViewModel must share the same flow.

### 5. ViewModel: `ui/<screen>/<Screen>ViewModel.kt`
Expose `val darkTheme = settings.darkTheme` and `fun onDarkThemeChange(enabled: Boolean)`, plus the usual `companion fun factory(container)`. It doesn't need a use case, because it's only a stored value with no rules to check.

### 6. Screen
- Stateful screen: `darkTheme ?: isSystemInDarkTheme()` gives the value the switch shows.
- Stateless content: a `ListItem` with the `Switch` as `trailingContent`, using `onCheckedChange = null`. Put `Modifier.toggleable(value, onValueChange, role = Role.Switch)` on the row, so the whole row is the touch target and TalkBack reads it as a single switch.
- Add a `dark_theme` string to `strings.xml`, plus light and dark `@Preview`s.
- Wire the ViewModel into the NavHost destination.

### 7. MainActivity: this is what actually changes the theme
```kotlin
setContent {
    val saved by container.settings.darkTheme.collectAsStateWithLifecycle()
    val darkTheme = saved ?: isSystemInDarkTheme()

    // Status/navigation bar icons follow the app's theme, not the system's.
    DisposableEffect(darkTheme) {
        val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        onDispose {}
    }

    AppTheme(darkTheme = darkTheme) { NavHost(container) }
}
```
Without the `enableEdgeToEdge` re-call, the app turns dark on a light-mode phone but keeps dark status-bar icons, which are unreadable on the dark background.

### 8. Docs
Add a line to CLAUDE.md's Architecture section describing `SettingsRepository`, what null means, and who reads and writes it.

## Verify

1. `./gradlew testDebugUnitTest`: all green, including the new tests (green).
2. `./gradlew installDebug`, then on the emulator, with the phone's night mode **off** (`adb shell cmd uimode night` shows "no"):
   - Open the screen and turn the switch on. Every screen goes dark straight away, the bottom bar included, and the status-bar icons are white.
   - Force-stop and relaunch. The app is still dark.
   - Turn it off. The light theme comes back.
   - `adb logcat -b crash -d | grep <package>` shows nothing.
3. Report the results with screenshots. Don't commit unless the user asks.

## Known limitation
With dynamic color (Android 12+), the dark scheme comes from the wallpaper, not the app's own colours. The splash and window background before the first Compose frame still follow the system theme. Mention this to the user rather than fixing it unasked.
