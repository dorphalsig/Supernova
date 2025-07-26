# Agents.md — Supernova IPTV

This guide defines the enforceable constraints, scaffolding, and responsibilities for all Codex agents working on the Supernova IPTV Android TV project.

---

## 1. Project Constraints

* **Platform**: Android TV, non-touch, low-end (≤512MB RAM)
* **UI**: Jetpack Compose only
* **Minimum SDK**: 21
* **Architecture**: MVVM + Repository + Room
* **Persistence**: Room (DAO-enforced, FTS4 only)
* **Media Loading**: Coil
* **Network**: Retrofit + Moshi
* **Async**: Coroutines + WorkManager
* **No XML layouts, JUnit4, Robolectric, or AppSearch**

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

## 7. qaGate Enforcement

* Agents must run `./gradlew testDebugUnitTest` before committing any task
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
* No commit is allowed if `testDebugUnitTest` fails and escalation has not been performed
* All successful commits must include:
  - Task name
  - Final `paste.rs` report link
  - Note if auto-fixes were applied

Example commit message:
```
fix: Add BaseRoomTest to FavoritesDaoTest

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