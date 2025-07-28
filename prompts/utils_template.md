# [TASK NAME]

## Purpose
Create layer-agnostic utility or helper classes for pure business logic (formatters, calculators, mappers, validators). Must be testable in isolation without Android dependencies.

## Definitions
- [Define utility functions and their input/output types]
- [Explain any data transformations or calculations performed]
- [Specify package location: com.supernova.util.* or domain-specific]

## Rules
- Single responsibility: 1 class = 1 specific purpose
- Pure functions preferred (stateless, no side effects)
- No Android APIs (Context, Resources, UI components)
- No business logic (belongs in UseCase/Repository)
- Avoid suspend functions unless genuinely async

## Test Harness
- Use `TestEntityFactory` for entity-based utilities
- Use `JsonFixtureLoader` for data transformation utilities
- Use standard JUnit5 for pure functions
- Use `runTest` only if testing suspend functions
- Minimum 70% test coverage required
- Extend / Reuse Test Harness components. Do NOT modify them.