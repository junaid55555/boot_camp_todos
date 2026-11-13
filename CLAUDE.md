# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Workout Logger: a single-user, offline Android app (Kotlin + Jetpack Compose + Material 3), built as a training project for practising an AI-assisted workflow. `docs/KICKOFF.md` is the source of truth for scope, tech stack, security boundaries and the Definition of Done. Read it before planning any feature.

- **Base app:** add a workout (name, date, duration in minutes), list workouts newest first, delete a workout, and persist them locally with Room.
- **Capstone feature:** a workout streak counter (consecutive days with a logged workout), shown on the home screen.

## Commands

Run these from the repo root. Use `./gradlew` in Bash and `.\gradlew.bat` in PowerShell.

```bash
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew testDebugUnitTest             # JVM unit tests (the DoD gate)
./gradlew testDebugUnitTest --tests "com.fitnessmarket.bootcamptodo.SomeTest"            # one class
./gradlew testDebugUnitTest --tests "com.fitnessmarket.bootcamptodo.SomeTest.someMethod" # one test
./gradlew connectedDebugAndroidTest     # instrumented tests (needs device)
./gradlew lint                          # Android lint
```

## Build setup

- **Versions:** AGP 9.x uses its built-in Kotlin support, so the `kotlin-android` plugin isn't applied; only `kotlin.compose` is.
- **Dependency catalog:** dependencies go in `gradle/libs.versions.toml` and are referenced as `libs.*` in `app/build.gradle.kts`.
- **SDK and Java:** compileSdk/targetSdk 37, minSdk 26, Java 11. `java.time` is used natively, with no core library desugaring.
- **New libraries:** don't add new libraries or Gradle plugins without asking. Already approved for the base app: Room (+ its Gradle plugin), KSP, lifecycle-viewmodel-compose, lifecycle-runtime-compose, navigation-compose, MockK, Turbine, kotlinx-coroutines-test. There's no icons library: icons are vector drawables in `res/drawable/` (`ic_*.xml`).
- **Room schema:** exported to `app/schemas/` and committed. A schema change needs a version bump and a migration. It's at version 2 now: an `AutoMigration(1 → 2)` added the nullable `photoFileName` column.
- **Release builds:** use the AGP `optimization {}` block, not `isMinifyEnabled`, and keep rules live in `app/src/main/keepRules/`.
- **Package name:** `com.fitnessmarket.bootcamptodo` is deliberately kept. Only the user-visible `app_name` is "Workout Logger", and the theme is still `BootCampTodoTheme`.

## Architecture

MVVM in layers under `com.fitnessmarket.bootcamptodo`. Dependencies point inward: ui → domain ← data.

- **domain/:** the `Workout` model (holds a `LocalDate`), the `WorkoutRepository` interface, and one use case per action. `AddWorkoutUseCase` owns all validation (name trimmed, 1–50 characters; duration 1–1440; date not after today). It returns `AddWorkoutResult.Success`, or `Invalid(Set<AddWorkoutError>)` with every error found at once. "Today" comes from an injected `java.time.Clock`.
- **data/:** Room `WorkoutEntity` (date as `dateEpochDay: Long`), `WorkoutDao`, `WorkoutDatabase`, and `RoomWorkoutRepository`, which maps entity ↔ domain. Newest-first ordering lives in the DAO SQL (`dateEpochDay DESC, id DESC`).
- **Settings:** `SettingsRepository` (domain) is backed by `SharedPrefsSettingsRepository` (data; plain SharedPreferences, not DataStore, which isn't an approved library). It holds `darkTheme: StateFlow<Boolean?>`, where null means follow the system. `MainActivity` reads it to pick the theme and the status-bar icon colours, and the Profile tab's switch writes it.
- **Photos:** a workout can have an optional photo, which is stored as a file name only (`Workout.photoFileName`).
  - **Storage:** `PhotoStore` (domain) is backed by `FilePhotoStore`. It keeps JPEGs in private storage at `filesDir/photos/`.
  - **Taking a photo:** the add screen uses `ActivityResultContracts.TakePicture` and a `FileProvider` (`res/xml/file_paths.xml` shares only that folder). There's no CAMERA permission and no CameraX.
  - **Showing photos:** `ui/photo/WorkoutPhoto` decodes a downscaled bitmap and turns it upright from the EXIF orientation. No image library is used.
  - **Cleanup:** `DeleteWorkoutUseCase` deletes the photo along with its workout. Any other unused file is deleted at app start by `CleanUpOrphanPhotosUseCase`. Those come from a retake, a cancelled camera, or an add screen closed without saving.
- **di/AppContainer:** built once in `WorkoutApp.onCreate()`. It owns the DB, the repository, the use cases and the `Clock`. Each ViewModel has a `companion fun factory(container)`.
- **ui/:** `WorkoutNavHost` owns the outer `Scaffold` with a bottom `NavigationBar` (tabs `home` = workout list, `profile` = settings, currently the dark theme switch). The bar is hidden on the full-screen `add` route. Each screen keeps its own inner `Scaffold`. The outer one uses `contentWindowInsets = WindowInsets(0)` and `consumeWindowInsets` so the system bars aren't padded twice. Each destination has a screen and, when it has state, a ViewModel. Screens are split into a stateful `XxxScreen(viewModel)` and a stateless `XxxContent(uiState, callbacks)`, which is what the previews use. The add screen reports success through a one-shot `saved` `Flow` (a Channel), not through state.

Tests: JVM unit tests in `app/src/test` use `MainDispatcherRule` for `viewModelScope`, and run the real use cases over a MockK repository. The Room DAO test is instrumented (`app/src/androidTest`, in-memory DB).

UI tests (`app/src/androidTest`, Compose test rule, run with `connectedDebugAndroidTest`):
- **Stateless screens:** `*ContentTest` render a screen's `XxxContent` with a fixed state and record its callbacks.
- **Navigation and flows:** `WorkoutNavHostTest` runs the real app graph through `AppContainer(context, database = <in-memory>)`.
- **Camera:** `AddWorkoutScreenPhotoTest` replaces the camera app with a fake `ActivityResultRegistry` provided through `LocalActivityResultRegistryOwner`. The fake writes a real JPEG through the `FileProvider` URI.
- **Dark theme:** `DarkThemeTest` launches `MainActivity` and checks the screen pixels.
- **Isolation:** tests never touch the user's real data. They use an in-memory DB, a separate prefs file (`SharedPrefsSettingsRepository(context, fileName)`), or save and restore the real settings.

Decisions already made. Don't revisit them without asking:

- **DI:** a hand-written app container (an `Application` subclass holding the singletons), no Hilt. ViewModels are built through a `ViewModelProvider.Factory` from the container.
- **Dates:** stored as a `Long` epoch day (`LocalDate.toEpochDay()`; java.time is available natively at minSdk 26). Streak logic works on epoch days, which makes "consecutive days" a simple integer difference.
- **Screens:** bottom tabs Home (workout list) and Profile, plus a full-screen add screen, all with navigation-compose and string routes. Type-safe routes would need the kotlinx-serialization plugin. The add screen's date defaults to today, and future dates can't be picked.
- **Tests:** JUnit 4 + MockK + Turbine + kotlinx-coroutines-test. Business logic (use cases, ViewModel) gets unit tests, written TDD-style (red → green → refactor).

## Code style

Follow the code style and naming conventions in @docs/CODE_STYLE.md: formatting, naming, layer rules, comments, tests and commit messages.

A PostToolUse hook (`.claude/settings.json` → `.claude/hooks/ktlint-check.sh`) runs ktlint in check-only mode on every `.kt`/`.kts` file that is edited or written. Its rules are in `.editorconfig`. When it reports violations, fix them before moving on. ktlint itself is a gitignored CLI binary: run `tools/get-ktlint.sh` once per checkout.

## Process and evidence

Work follows the KICKOFF pipeline: Grill → Prototype → Spec → Tickets → TDD → AI Review → Security Audit → Human PR Review → QA. Each step's artifacts go in the matching `evidence/0N-*/` folder (01-grill … 09-qa).

## Security boundaries

Full rules in `docs/KICKOFF.md`.

- No ServiceMarket code, data, URLs or credentials. Use dummy data only, and no real secrets.
- Ask before running destructive commands (`rm -rf`, `git push --force`, `git reset --hard`).
- Never merge or deploy. A human merges.
