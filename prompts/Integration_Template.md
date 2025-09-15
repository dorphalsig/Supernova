# [TASK NAME]

## Purpose
Connect ViewModels, UseCases, Workers, or Repositories. Task should define clear data flow between UI and domain/data layers.

## Input/Output
- [Define ViewModel state, worker input/output, repository call signatures]
- [Explain any mappings or DTO conversion]

## Test Harness
- Use `BaseSyncTest`, `JsonFixtureLoader`, and `MockWebServerExtensions`
- Each test must use one JSON fixture in `src/test/resources`
- Use `CoroutineTestUtils` to control dispatchers and delay
- No mocking Room or Retrofit directly
- Minimum 70% test coverage required
- Extend / Reuse Test Harness components. Do NOT modify them.
