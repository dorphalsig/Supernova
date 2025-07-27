# Agents.md — Supernova IPTV

This guide defines the enforceable constraints, scaffolding, and responsibilities for all Codex agents working on the Supernova IPTV Android TV project.

---

## 1. Project Constraints

* **Platform**: Android TV, non-touch, low-end (≤512MB RAM)
* **UI**: Jetpack Compose only
* **Minimum SDK**: 26
* **Architecture**: MVVM + Repository + Room
* **Persistence**: Room (DAO-enforced, FTS4 only)
* **Media Loading**: Coil
* **Network**: Retrofit + Moshi
* **Async**: Coroutines + WorkManager
* **No XML layouts, JUnit4, Robolectric, or AppSearch**
* **Version Lock**: Do not change versions of libraries or build targets.
* **Mandatory Gradle Plugins**
  - com.supernova.testgate 

---
## 2. Test Harness (REQUIRED)

All tasks must **reuse the shared test harness** components below:

### Module
- **Name**: `testing-harness`
- **Package Root**: `com.supernova.testing`
- **Enforced**: `true`

### Components by Layer
- **Data**
  - `BaseRoomTest`
  - `DbAssertionHelpers`
- **Sync**
  - `BaseSyncTest`
  - `JsonFixtureLoader`
  - `MockWebServerExtensions`
  - `SyncScenarioFactory`
- **UI**
  - `UiStateTestHelpers`
  - `PreviewFactories`
- **All Layers**
  - `TestEntityFactory`
  - `CoroutineTestUtils`

### Requirements
- Must use shared module only
- Allowed packages: `com.supernova.testing.*`
- Disallowed packages: `data.*`, `sync.*`, `ui.*`
- Gradle dependency must be present
- Minimum test coverage: **70%**
- Coroutines required for async code
- Tests should extend the provided base classes. 
- **DO NOT** modify anything in the `testing-harness` module.

### Agent Rules
- Reuse of components is **mandatory**
- No inline entity definitions
- No mocks for Room or Retrofit
- Only one fixture per task
- Helpers must be covered by unit tests
- DO NOT modify the `testing-harness` module
- Do NOT modify gradle.properties

---

## 3. Mandatory Reuse (UI Components)

When generating UI tasks, agents must reuse the following components:

* `FocusableCard`
* `FocusableButton`
* `FocusableImageCard`
* `MediaCard`
* `ContentRail`
* `SearchBar`
* `LoadingSpinner` (if not implemented, stub it)
* `UiState` sealed class for all screen states

---

## 4. Mandatory Reuse (Screen Definitions)

When implementing or modifying screens, agents must base the layout on the canonical wireframes defined in the architecture document §12. Each screen includes a mapping of which components to reuse.

---

## 5. Worker Enforcement

Only the following workers are allowed in MVP:

* `SyncWorker` — triggers EPG/catalog updates on launch
* `RecWorker` — weekly personalization update

All others (e.g., `DetailCacheWorker`, `FTSIndexWorker`) are **excluded** from MVP.

---

## 6. Functional Rules to Enforce

* **Room + FTS4 only**, never use FTS5
* All FTS-backed tables must be mutated via DAO
* No raw SQL for search tables
* Sync is **atomic** — no partial fallback
* Playback errors = toast only
* EPG overlay = 5 rows visible, scrollable full list
* Resume UI = card with \[Resume] / \[Start Over]

---

## 7. qaGate Enforcement

* Agents must run `./gradlew test` before committing any task
* If the build fails at `:app:qaGate`, agents must:
  - Parse `app/build/reports/check-results.json`
  - Read `qaGateSummary.overallSuccess`
  - Extract remediation hints from:
    - `checkTestStructure.violations[].remediation`
    - `checkFails.failedCount`
    - `checkBanned.violations[].file`
    - `compileErrors.success`
  - Retry up to 3 times (4 total attempts), applying fixes using patterns like:
    - extend `BaseRoomTest`, `BaseSyncTest`, `UiStateTestHelpers`
    - apply `runTest + MockK`
    - remove banned imports
* Every failed attempt must log and include the `paste.rs` report link
* If all 4 attempts fail:
  - Open a GitHub issue using the GitHub CLI (`gh`)
  - Use `$PAT` from environment for authentication
  - Title: `qaGate failure: unresolvable after 4 attempts`
  - Include in body:
    - Commit hash
    - Summary of issues from JSON
    - All `paste.rs` links
* No commit is allowed without a passing `qaGate` or 4 failed attempts and an issue created
* All successful commits must include:
  - Task name
  - Initial instructions
  - Brief description of changes
  - Final `paste.rs` report link
  - Note if auto-fixes were applied

Example commit message:
```
feat: Create SupernovaDatabase and core DAOs


qaGate passed after 2 retries. Report: https://paste.rs/abc12
```

---

## 7. Required Quality Gates

| Check                      | Enforced? |
| -------------------------- | --------- |
| Test coverage ≥70%         | ✅         |
| 1 JSON fixture per task    | ✅         |
| No Robolectric             | ✅         |
| No JUnit4                  | ✅         |
| Coroutine test wrappers    | ✅         |
| Dead code removal (static) | ✅         |

--- END