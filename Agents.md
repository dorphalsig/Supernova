# Agents.md — Supernova IPTV

This guide defines the enforceable constraints, scaffolding, and responsibilities for all Codex agents working on the Supernova IPTV Android TV project.

---

## 1. Project Constraints

* **Platform**: Android TV, non-touch, low-end (≤512 MB RAM)
* **UI**: Jetpack Compose only
* **Minimum SDK**: 26
* **Architecture**: MVVM + Repository + Room
* **Persistence**: Room (DAO-enforced, FTS4 only)
* **Media Loading**: Coil
* **Network**: Retrofit + Moshi
* **Async**: Coroutines + WorkManager
* **No**: XML layouts, JUnit4, Robolectric, AppSearch
* **Version Lock**: Do not change versions of libraries or build targets
* **Mandatory Gradle Plugins**: `com.supernova.testgate`
* **Testing Framework**: JUnit 5 + MockK only
* **Test Structure**: Unit tests only, no instrumentation tests
* **Test Directory**: `src/test/kotlin` only (no `src/androidTest`)

---

## 2. Test Harness (REQUIRED)

All tasks must **reuse the shared test harness** components:

* **Module**: `:testing-harness`
* **Package root**: `com.supernova.testing`
* **Enforced**: `true`

### Components

* `BaseRoomTest` (layer: data)
* `BaseSyncTest` (layer: sync)
* `TestEntityFactory` (layer: all)
* `CoroutineTestUtils` (layer: all)
* `DbAssertionHelpers` (layer: data)
* `JsonFixtureLoader` (layer: sync)
* `MockWebServerExtensions` (layer: sync)
* `UiStateTestHelpers` (layer: ui)
* `PreviewFactories` (layer: ui)
* `SyncScenarioFactory` (layer: sync)

### Requirements

* Shared module only
* Allowed packages: `com.supernova.testing.*`
* Disallowed packages: `data.*`, `sync.*`, `ui.*`
* Gradle dependency required
* Minimum coverage: 70%
* Coroutines enforced

### Agent Rules

* Reuse required
* No inline entities
* No Room or Retrofit mocks
* One fixture per task
* Unit test for helper

---

## 3. Mandatory Reuse — UI Components

When generating UI tasks, agents must reuse:

* `FocusableCard`
* `FocusableButton`
* `FocusableImageCard`
* `MediaCard`
* `ContentRail`
* `SearchBar`
* `LoadingSpinner` (if not implemented, stub it)
* `UiState` sealed class for all screen states

---

## 4. Mandatory Reuse — Screens

When implementing or modifying screens, agents must base the layout on the canonical wireframes defined in the architecture document (§ 12).
Each screen includes a mapping of which components to reuse.

---

## 5. Worker Enforcement

Only the following workers are allowed in MVP:

* `SyncWorker` — triggers EPG/catalog updates on launch
* `RecWorker` — weekly personalization update

All others (e.g. `DetailCacheWorker`, `FTSIndexWorker`) are **excluded** from MVP.

---

## 6. Functional Rules to Enforce

* Room + FTS4 only; never use FTS5
* All FTS-backed tables must be mutated via DAO
* No raw SQL for search tables
* Sync is **atomic** — no partial fallback
* Playback errors = toast only
* EPG overlay = 5 rows visible, scrollable full list
* Resume UI = card with \[Resume] / \[Start Over]

---

## 7. Code Review Checklist

After finishing creating/modifying each class:

* Have the functional & non-functional requirements been met?
* Is the solution as simple as possible while respecting the requirements?
* **SOLID compliance** (overall judgment, not dogmatic box-ticking).
* Readability: clear naming, clear flow, minimal cognitive load.
* DRY where it meaningfully increases reuse and maintainability.
* **Avoid micro-abstractions:** don’t split into tiny classes/methods used only once. If reuse is low and the unit is small, **merge it** with closely related responsibilities **provided the resulting artifact remains reasonably sized and coherent**.
* Syntax/lint passes; non-deprecated APIs only.
* Appropriate tests exist and cover behavior (happy path + key edge cases).

**Make sure all items pass before committing. If not, fix them.**

---

## 8. testGate Enforcement

* Run:

  ```bash
  ./gradlew :<module worked upon>:test
  ```

  before committing any task.

  * testGate pass → code review → commit
  * testGate fail → read `<project root>/build/testgate-report.json` and fix (max retry = 2)

    * Pass → code review → commit
    * Fail → create GitHub issue (Repo = `dorphalsig/Supernova`) with title = where + problem, body = errors + actions + suggestions + paste.rs link

* Commits allowed only if:

  * qaGate passes successfully OR 2 retries + issue created
  * Code review checklist passes

* Commit message must include:

  * Task name
  * Initial instructions
  * Brief description of changes
  * Link to issue if QA fails after 3 attempts

**Example commit message:**

```
fail: Add BaseRoomTest to FavoritesDaoTest
 - implemented test methods using BaseRoomTest
 - error: java.lang.RuntimeException: Exception while computing database live data.
        at androidx.room.RoomTrackingLiveData$1.run(RoomTrackingLiveData.java:92)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1162)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:636)
        at java.lang.Thread.run(Thread.java:764)
 - retried 2 times and testGate still fails
 - ticket: Ref. #51
```

---

## 9. Required Quality Gates

* Test coverage ≥ 70%
* 1 JSON fixture per task
* No Robolectric
* No JUnit4
* Coroutine test wrappers required
* Dead code removal (static)
* No instrumentation tests
* JUnit 5 + MockK only
* No banned testing imports
* Version lock compliance

---

## 10. Testing Framework Enforcement

**Mandatory Testing Stack**

* JUnit 5 (`@Test`, `@BeforeEach`, `@AfterEach`) only
* MockK for mocking
* `kotlin.test` for assertions
* `kotlinx.coroutines.test` for `runTest`

**Prohibited**

* JUnit 4 (`@RunWith`, `AndroidJUnit4`) — use JUnit 5 instead
* Robolectric — use `BaseRoomTest` instead
* Espresso — unit tests only
* Compose UI Test — use `UiStateTestHelpers` instead
* AndroidX Test runners — unit tests only
* Instrumentation tests — unit tests only
* `@Ignore` annotations — fix or remove test

**Test Directory Structure**

* ALLOWED: `src/test/kotlin/` — all tests here
* BANNED: `src/androidTest/` — prohibited entirely
* BANNED: `src/sharedTest/` — not allowed
