# [Task Name]

## Purpose
Build or extend a TV-optimized screen using Jetpack Compose and reusable components.

## Layout & Behavior
- [Describe screen layout: rails, cards, modals, overlays]
- [List navigation: D-pad focus, back behavior, click actions]
- [Mention loading/error/success states]

## Reuse Components
- Use: [List which components should be used for this screen: `FocusableCard`, `FocusableButton`, `MediaCard`, `ContentRail`, `SearchBar`, `LoadingSpinner`]
- State management must use a `UiState` sealed class
- Compose previews required for each composable

## Test Harness
- Use `UiStateTestHelpers` and `PreviewFactories`
- Do not use Robolectric or JUnit4
- Minimum 70% test coverage required
- Extend / Reuse Test Harness components. Do NOT modify them.