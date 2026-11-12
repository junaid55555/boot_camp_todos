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
- **New libraries:** don't add new libraries or Gradle plugins without asking. Already approved for the base app: Room, KSP, lifecycle-viewmodel-compose, lifecycle-runtime-compose, MockK, Turbine, kotlinx-coroutines-test.
- **Release builds:** use the AGP `optimization {}` block, not `isMinifyEnabled`, and keep rules live in `app/src/main/keepRules/`.
- **Package name:** `com.fitnessmarket.bootcamptodo` is deliberately kept. Only the user-visible `app_name` is "Workout Logger", and the theme is still `BootCampTodoTheme`.

## Architecture (decided; the base app is not built yet)

The code is still the Compose template (`MainActivity` shows a Greeting). The intended structure is MVVM in layers:

- **data:** Room `WorkoutEntity` / `WorkoutDao` / `WorkoutDatabase` and a `WorkoutRepository` implementation. The DAO exposes a `Flow` ordered by date DESC.
- **domain:** a `Workout` model, the repository interface, and use cases (add with validation: non-blank name and duration > 0; get; delete; and later, the streak calculation).
- **ui:** a `WorkoutViewModel` exposing `StateFlow` UI state, consumed with `collectAsStateWithLifecycle`.

Decisions already made. Don't revisit them without asking:

- **DI:** a hand-written app container (an `Application` subclass holding the singletons), no Hilt. ViewModels are built through a `ViewModelProvider.Factory` from the container.
- **Dates:** stored as a `Long` epoch day (`LocalDate.toEpochDay()`; java.time is available natively at minSdk 26). Streak logic works on epoch days, which makes "consecutive days" a simple integer difference.
- **Screens:** a single screen. Adding a workout happens in a dialog, so there's no Navigation library.
- **Tests:** JUnit 4 + MockK + Turbine + kotlinx-coroutines-test. Business logic (use cases, ViewModel) gets unit tests, written TDD-style (red → green → refactor).

## Process and evidence

Work follows the KICKOFF pipeline: Grill → Prototype → Spec → Tickets → TDD → AI Review → Security Audit → Human PR Review → QA. Each step's artifacts go in the matching `evidence/0N-*/` folder (01-grill … 09-qa).

## Security boundaries

Full rules in `docs/KICKOFF.md`.

- No ServiceMarket code, data, URLs or credentials. Use dummy data only, and no real secrets.
- Ask before running destructive commands (`rm -rf`, `git push --force`, `git reset --hard`).
- Never merge or deploy. A human merges.
