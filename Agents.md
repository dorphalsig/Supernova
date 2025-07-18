# Agents.md

## 1. Agent Role & Execution Context

Agents are isolated GPT-based workers executing atomic tasks in a CI-like environment. They are:

* Non-interactive: each task must include all context needed.
* Stateless across runs: do not assume prior output or project state.
* Responsible for preserving project integrity: repo must compile, tests must pass.

---

## 2. Task Execution Guarantees

Each task must:

* Leave the project in a compilable, test-passing state
* Touch only a **single concern** (e.g., one feature, one module, or one architectural layer)
* Be **mergeable without conflict** with other concurrent tasks

**Repo Validation Command:**

```bash
./build.py cleanup :app:assembleDebug :app:testDebugUnitTest
```
Each run will take around 10-15 minutes. Be patient and ensure all tests pass.
**Do not use** `./build.py build` or `./gradlew build`  — it may trigger unsupported instrumentation steps.

## 3. Code Style & Structure

* Use Kotlin for all Android code
* Prefer Jetpack Compose for UI
* One file per class/component
* Data, UI, and network layers must follow modular separation

### Naming Conventions

* DAO: `EntityDao.kt`
* Entity: `EntityNameEntity.kt`
* ViewModel: `FeatureViewModel.kt`
* Screen: `FeatureScreen.kt`

---

## 4. Test Policy

### Required Stack

* **JUnit**: Test runner (all tests)
* **MockK**: For mocking dependencies in unit tests
* **Room (in-memory)**: For DAO testing

### Prohibited

* ❌ Robolectric (fully removed)
* ❌ Espresso in new code (allowed for legacy only)

### Required Coverage

* Each task must add or update unit tests for its logic
* Minimum 70% line coverage (enforced by CI audit step)

### Test Validation Commands

```bash
./build.py clean :app:assembleDebug :app:testDebugUnitTest
```
Each run will take around 10-15 minutes. Be patient and ensure all tests pass.
If any errors persist, detail all of them in the Work summary with filename and line number.
---

## 5. Commit Procedure
1. Run chmod +x ./create_issue.py
2. Run `./create_issue.py --pat "<GITHUB PAT FROM YOUR PROMPT>" --title "<GOALS SUMMARY>" --body "<COMPLETE PROMPT>"` to create a new issue for the task
   this will return the issue number.
3. Include the latest output of  /tmp/build_error_context.md in the issue body.
4. The last line of the commit mus **ALWAYS** be `#<issue number>` to tag the issue when merged.


---

## 6. CI Configuration

* Instrumentation (`androidTest`) is disabled by default
* Compose Previews are used for UI snapshot validation
* CI will fail if coverage drops below threshold

---

## 7. Troubleshooting

If an agent encounters:

* Build hangs: Try `--no-daemon`, `--console=plain`, `--max-workers=1`
* Unknown test failures: Check for leftover Espresso or platform test references
* Timeouts: Reduce scope and split into smaller subtasks

---
## 8. Enforcement

All tasks must comply with:

* `/docs/architecture/*`
* `/Agents.md`
* `build.gradle.kts`, `libs.versions.toml`, and module policies

If a policy is unclear, agents must stop and request clarification before proceeding.

---

## 10. Definition of Done

A task is considered done when:

* All modified files compile
* Unit tests pass via `testDebugUnitTest`
* Coverage is maintained or improved
* Repo state is ready for next task
