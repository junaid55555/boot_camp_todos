# Code style and conventions

These rules are what the existing code already does. New code should read as if the same person wrote it. When a rule and the surrounding code disagree, match the code and flag the mismatch.

## Kotlin formatting

- **Style:** the official Kotlin style (`kotlin.code.style=official` in `gradle.properties`). Use 4-space indents and no wildcard imports.
- **Trailing commas** go on every multi-line parameter list, argument list and enum: `durationMinutes = durationMinutes,`.
- **Line length:** keep lines to about 120 characters. Break long calls into one named argument per line.
- **Imports:** sorted the way Android Studio orders them by default, with `java.*` after `kotlinx.*`. Remove unused imports.
- **Bodies:** use an expression body for one-liners (`override suspend fun delete(id: Long) = dao.deleteById(id)`) and a block body otherwise.
- **Named arguments:** use them for constructors and for calls with more than one argument of the same type, for example `Workout(name = ..., date = ..., durationMinutes = ...)`.
- **Constants:** use `const val UPPER_SNAKE` in a `companion object`, or a `private` top-level value when only that file uses it.
- **Visibility:** helpers stay `private` (mappers, small composables, formatters). Make something public only when another file needs it.
- **No `!!`** unless the line above has just proved the value isn't null. The only one today is in `AddWorkoutUseCase`, after validation.

## Naming

| Thing | Pattern | Example |
|---|---|---|
| Use case | `VerbNounUseCase`, `operator fun invoke` | `AddWorkoutUseCase` |
| Repository | interface `XxxRepository` (domain), implementation `<Tech>XxxRepository` (data) | `RoomWorkoutRepository`, `SharedPrefsSettingsRepository` |
| Room | `XxxEntity`, `XxxDao`, `XxxDatabase` | `WorkoutEntity` |
| DAO methods | `observeAll()` for a `Flow`, `insert`, `deleteById` | |
| ViewModel | `XxxViewModel`, with state `XxxUiState` (a data class with defaults) | `WorkoutListUiState` |
| ViewModel events | `onXxx` / `onXxxChange` | `onDelete`, `onNameChange` |
| Composables | stateful `XxxScreen(viewModel, nav callbacks)` and stateless `XxxContent(uiState, callbacks)` | `ProfileContent` |
| Backing state | a `_private` mutable value exposed through a read-only one | `_uiState` / `uiState` |
| Mappers | private extension functions `toDomain()` / `toEntity()` | |
| Resources | `ic_<name>.xml` for icons; strings in snake_case grouped by screen under `<!-- Screen -->` comments | `error_name_blank` |

## Architecture rules

The layers are described in CLAUDE.md's Architecture section. In practice:

- **domain/** has no Android imports. It holds pure Kotlin and `java.time`.
- **Validation and business rules** live in use cases, not in ViewModels or composables. Return a sealed result type such as `AddWorkoutResult`, not an exception.
- **"Today" and time** always come from the injected `Clock`. Never call `LocalDate.now()` without one.
- **ViewModels:**
  - expose `StateFlow` (`stateIn(viewModelScope, WhileSubscribed(5_000), initial)` or `asStateFlow()`)
  - expose one-shot events as a `Channel(...).receiveAsFlow()`
  - get a `companion object { fun factory(container: AppContainer) }`
- **Use-case exception:** a plain stored value with no rules (like a setting) can go straight from the ViewModel to the repository without a use case.
- **Composables:**
  - collect with `collectAsStateWithLifecycle()`
  - never take a ViewModel in `XxxContent`
  - take callbacks as lambdas, and pass `viewModel::onXxx` where possible
- **UI text:**
  - all user-visible text comes from `strings.xml`, never hard-coded
  - icons that do something need a `contentDescription`; decorative icons use `null`
- **Previews:** each `XxxContent` gets at least one private `@Preview` wrapped in `BootCampTodoTheme`. Add a dark one when the screen depends on the theme.

## Comments

- Comments are sparse and explain **why**, not what: "replay = 1 so an emission isn't lost…", "Read once at startup so the first frame has the right theme".
- Use one-line KDoc on public members only when the name doesn't say enough, for example `/** null = the user hasn't picked yet … */`.
- Don't leave commented-out code, and don't leave TODOs without a ticket.

## Tests

- **Location:** JVM unit tests go in `app/src/test`, in the same package as the class under test, named `XxxTest`.
- **Names:** backtick sentences that describe behaviour, such as `` `future date is rejected` ``, not `testSave1`.
- **Layout:** arrange, act and assert, separated by blank lines. Prefer one behaviour per test.
- **Mocks:** MockK, with `coEvery`/`coVerify` for suspend functions. Mock repositories, not use cases: ViewModel tests run the real use cases.
- **Flows:** Turbine `test { awaitItem() }`. `MainDispatcherRule` goes in any test that touches `viewModelScope`.
- **Dates:** fixed dates and a `Clock.fixed(...)`. Don't depend on the real date.
- **Process:** TDD. Commit the test with the code, and confirm red before green.

## Git

- Commit messages follow Conventional Commits: `feat:`, `fix:`, `chore:`, `refactor:`, `test:`, `docs:`, all lower-case and in the imperative, for example `feat: add bottom tabs (Home, Profile)`.
- Make one logical change per commit. Run `./gradlew testDebugUnitTest` before committing.
- Never commit secrets or `local.properties`. Never force-push. A human merges.
