---
name: test-requirements
description: Enforces that all new features and bug fixes include unit and UI tests that must pass. Use when implementing new features, fixing bugs, or making any code changes to the PgnToGif Android project.
---

# Test Requirements

All new features and bug fixes **must** include accompanying tests. A feature is not complete until its tests pass.

## Required Test Types

### 1. Unit Tests (`app/src/test/`)

- Test pure logic: data transformations, preference storage, converters, utility functions.
- Use JUnit 4 (`junit:junit:4.13.2`).
- Mock Android dependencies when necessary.
- Place in package `com.example.pgntogifconverter` mirroring the source structure.

### 2. Instrumented / UI Tests (`app/src/androidTest/`)

- Test Android-dependent logic: activities, Compose UI, resource loading, bitmap rendering.
- Use `androidx.test.ext:junit` and `androidx.test.espresso:espresso-core`.
- Use `ActivityScenario` for activity tests.
- Place in package `com.example.pgntogifconverter` mirroring the source structure.

## Workflow

1. **Before implementing**: identify which behaviors need unit vs UI tests.
2. **During implementation**: write tests alongside production code.
3. **After implementation**: run all tests and verify they pass:
   - Unit tests: `./gradlew testDebugUnitTest`
   - Instrumented tests: `./gradlew connectedDebugAndroidTest`
4. **Fix failures** before considering the task complete.

## Test Naming Convention

Use descriptive names: `methodOrFeature_condition_expectedBehavior`

Example: `highlightStyle_changingFromGreenToBlue_updatesHighlightPaintColor`

## What to Test

- **Data layer changes**: verify save/load round-trips in `PreferenceSettingsStorage`.
- **Settings changes**: verify UI state updates when settings change.
- **Rendering changes**: verify `Paint` colors, bitmap output differences, or frame dimensions.
- **UI changes**: verify composable behavior, navigation, and user interactions.
- **Bug fixes**: write a regression test that would have caught the bug.
