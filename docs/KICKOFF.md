# Kickoff — Claude Code Practice

## Capstone
**App:** Workout Logger, a simple Android app (Kotlin + Jetpack Compose)
**Purpose:** Practice the full AI coding workflow (Grill → Prototype → Spec → Tickets → TDD → AI Review → Security Audit → Human PR Review → QA) on a small, safe project.

### Base app (built first, before the capstone)
- Add a workout: name, date, duration (minutes)
- See the list of workouts, newest first
- Delete a workout
- Data saved locally (Room)

### Capstone feature (finalised on Day 2)
**Workout streak counter:** show how many consecutive days the user has logged a workout.

## Scope
**In scope**
- Single-user, offline app
- Local storage only (Room)
- Streak calculation + display on the home screen
- Unit tests for business logic (ViewModel / UseCase)

**Out of scope**
- Login / accounts
- Backend, APIs, cloud sync
- Notifications, reminders
- Sharing, social features
- Analytics, ads, payments

## Tech stack
- Kotlin, Jetpack Compose, Material 3
- MVVM: ViewModel + Repository + UseCase
- Room (local database)
- Hilt (dependency injection) — optional, keep simple if needed
- JUnit + MockK + Turbine (unit tests)

## Security boundaries
- No ServiceMarket code, data, URLs or credentials
- No real API keys or secrets; `.gitignore` covers `local.properties`, keystores, `.env`, `google-services.json`
- Dummy data only
- Claude must ask before destructive commands (`rm -rf`, `git push --force`, `git reset --hard`)
- No automatic merge or deployment; a human merges

## Definition of Done
- [ ] Grilling notes + `CONTEXT.md`
- [ ] Prototype findings
- [ ] Approved spec with acceptance criteria
- [ ] Dependency-aware tickets
- [ ] TDD evidence (red → green → refactor) + passing `./gradlew testDebugUnitTest`
- [ ] AI code review findings, triaged
- [ ] Security audit, severity-ranked, no open High/Critical
- [ ] Human-reviewed PR
- [ ] QA test cases: positive, negative, security, regression

## Evidence folders
```
evidence/
├── 01-grill/
├── 02-prototype/
├── 03-spec/
├── 04-tickets/
├── 05-tdd/
├── 06-ai-review/
├── 07-security/
├── 08-pr-review/
└── 09-qa/
```
