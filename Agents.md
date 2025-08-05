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
* **Mandatory Gradle Plugins**: com.supernova.testgate 
* **Testing Framework**: JUnit5 + MockK only
* **Test Structure**: Unit tests only, no instrumentation tests
* **Test Directory**: src/test/kotlin only, no src/androidTest
* **Version Lock**: Do not change versions of libraries or build targets

---

## 2. Test Harness (REQUIRED)

All tasks must **reuse the shared test harness** components below:

```
(test-harness
  (module :testing-harness)
  (package-root com.supernova.testing)
  (enforced true)

  (components
    (BaseRoomTest :layer "data")
    (BaseSyncTest :layer "sync")
    (TestEntityFactory :layer "all")
    (CoroutineTestUtils :layer "all")
    (DbAssertionHelpers :layer "data")
    (JsonFixtureLoader :layer "sync")
    (MockWebServerExtensions :layer "sync")
    (UiStateTestHelpers :layer "ui")
    (PreviewFactories :layer "ui")
    (SyncScenarioFactory :layer "sync")
  )

  (requirements
    :shared_module_only true
    :allowed_packages ["com.supernova.testing.*"]
    :disallowed_packages ["data.*", "sync.*", "ui.*"]
    :gradle_dependency_required true
    :minimum_coverage 70
    :coroutines_enforced true
  )

  (agent-rules
    :reuse_required true
    :no_inline_entities true
    :no_room_or_retrofit_mocks true
    :one_fixture_per_task true
    :unit_test_for_helper true
  )
)
```

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

## 7. Code Review
After writing code check:
* Have the requirements been met?
* is it as simple as can be made respecting the requirements?
* Follows Single Responsibility principle?
* Is the code easy to read?
* is it DRY?
* Are separations of concerned followed?
* Syntax Valid (lint)
* Uses non-deprecated APIs?
* Are there tests that cover the behavior?

**Make sure all these items pass before committing. If they dont, fix them before committing**

---

## 8. testGate Enforcement

* Run `./gradlew :<module worked upon>:testGate` before committing any task
  - testGate pass? -> code review -> commit
  - testGate fail -> read <project root>/build/testgate-report.json + fix. Max retry = 2.
    - pass? -> code review -> commit
    - fail? -> gh issue. Repo = dorphalsig/Supernova. Title = where + problem. Body = errors + actions + suggestions + paste.rs link
    
* Commits are allowed only if:
  - qaGate passes successfully OR 2 retries + issue
  - code review checklist passes 
  
* All commits must include:
  - Task name
  - Initial instructions
  - Brief description of changes
  - Link to issue if QA fails after 3 attempts

Example commit message:
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

- Test coverage >= 70%
- 1 JSON fixture per task
- No Robolectric
- No JUnit4
- Coroutine test wrappers
- Dead code removal (static)
- No instrumentation tests
- JUnit5 + MockK only
- No banned testing imports
- Version lock compliance

## 10. Testing Framework Enforcement

**Mandatory Testing Stack:**
- JUnit5 (@Test, @BeforeEach, @AfterEach) only
- MockK for mocking
- kotlin.test for assertions
- kotlinx.coroutines.test for runTest

**Prohibited Testing Frameworks:**
- JUnit4 (@RunWith, AndroidJUnit4) — Use JUnit5 @Test instead
- Robolectric — Use BaseRoomTest instead
- Espresso (all components) — Unit tests only
- Compose UI Test — Use UiStateTestHelpers instead
- AndroidX Test runners — Unit tests only
- Instrumentation tests — Unit tests only
- @Ignore annotations — Fix or remove test

**Test Directory Structure:**
- ALLOWED: src/test/kotlin/ — All tests here
- BANNED: src/androidTest/ — Prohibited entirely
- BANNED: src/sharedTest/ — Not allowed

**Test Directory Structure:**
- ALLOWED: src/test/kotlin/ — All tests here
- BANNED: src/androidTest/ — Prohibited entirely
- BANNED: src/sharedTest/ — Not allowed


--- END