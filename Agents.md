# Supernova — LLM Coding Agent Operating Guide

> This file operationalizes the **Architecture Design Document** for day-to-day coding by an LLM agent.
> It encodes *rules, defaults, entrypoints, harness usage, and responsibilities* so deliveries are consistent.

---

## 1) Ground Rules

* **Single source of truth:** Follow the *Architecture Design Document*.
* **No scope creep:** Do **not** add features marked as excluded (e.g., DVR, full grid EPG, M3U).
* **Version pins:** Use `libs.versions.toml` exactly. No upgrades without instruction.
* **Room FTS:** Use **FTS4**. Keep schema consistent with §7.1.
* **Streaming only:** Use Moshi `JsonReader` + Okio; never buffer entire payloads.
* **Materialized rails:** UI queries are `ORDER BY position`. Never query by popularity.
* **Hero:** Follow resolver & 7-day de-dup (§6.10).
* **Simplicity first:** Implement the minimum logic to fulfill the requirement — no speculative abstractions.

---

## 2) Project Modules Map

| Module                  | Purpose                                                 | Depends on                  |
| ----------------------- | ------------------------------------------------------- | --------------------------- |
| `:app`                  | UI shell (Compose), navigation, Providers (composition) | :domain, \:sync, \:security |
| `:domain`               | Pure Kotlin: models, mappers, use cases, **interfaces** | –                           |
| `:data`                 | Room DB, rails, popularity caches, DAO impls            | :domain                     |
| `:network`              | XC/TMDB clients (OkHttp/Moshi/XmlPull)                  | –                           |
| `:sync`                 | Ingestion pipelines, workers, cursors                   | :network, \:data, \:domain  |
| `:security`             | Keystore secure storage                                 | :domain                     |
| `:testing-harness`      | JVM test utilities                                      | –                           |
| `:testing-instrumented` | Android test utilities (FTS, secure storage)            | –                           |

---

## 3) Repository Layout & Naming

* Tests mirror prod packages: `src/test` and `src/androidTest`.
* Compose UI is thin; logic in ViewModels/use-cases.
* Entities & DAOs are exhaustive. Don’t invent new tables.

---

## 4) Entry Points (Tasks the agent should run)

* **Default (unit-only):** `:app:check`
* **If these paths change → run `:app:ciAllTests`:**

    * `src/androidTest/**`
    * FTS schema/DAO
    * Secure storage code
* **Aliases:**

    * `instrumented<Variant>Test` → `connected<Variant>AndroidTest`
    * `instrumentedTest` = all variants

**After tests:** Parse root-level **TestGate JSON** at `<root>/build/TestGate.json`.

---

## 5) Testing Harness & Policies

* **Frameworks:** Unit = JUnit 5; Instrumented = AndroidJUnit4. Robolectric forbidden.
* **Naming:** `given_When_Then`; one behavior per test.
* **Annotations:** use `@UseTestHarness` (or `@PureUnitTest` for POJOs).
* **Harness to use (§10.3):**

    * `RoomTestDbBuilder`, `JsonFixtureLoader`, `MockWebServerExtensions`
    * `DbAssertionHelpers` (row counts, RailEntry order, FTS checks)
    * `FtsTestKit` (androidTest, diacritics/ranking)
    * `SecureStorageTestKit` (androidTest, keystore round-trip)
* **Coverage gate:** ≥70% (enforced by TestGate).
* **Instrumentation scope:** only FTS parity & secure storage.

---

## 6) Data & Search Rules

* **FTS sources:** Stream, Episode, Program. Max results = 30.
* **Ranking:** exact > fuzzy > boosted (`title_norm×2.0`, `keywords×1.2`, `cast×1.0`).
* **Provider change:** Same host → keep data; different host → wipe & re-sync.
* **TMDB cadence:** preload \~7k once, then weekly top-up 200. On refresh → recompute PopularityCache + rebuild rails.

---

## 7) Ingestion Rules

* **Transport:** OkHttp streaming; gzip preferred.
* **Parsing:** Moshi streaming for JSON, XmlPullParser for XMLTV.
* **Batching:** 200–500 rows per transaction; shadow tables + atomic swap.
  *Example: insert 300 rows into a shadow table, then swap atomically into production.*
* **Retries:** exponential backoff ×3; bounded memory (\~1–2 MB).
* **Hooks:** after ingest → refresh FTS, recompute PopularityCache, rebuild rails.

---

## 8) Rails, Hero & Popularity

* **Rails:** Workers compute slices, write to `RailEntry.position`.
* **Hero:** Personalized → Editorial → None; exclude CW/Trending; 7-day no-repeat.
* **Popularity:** Only applies to first \~6 rails; recomputed incrementally + weekly.

---

## 9) Screen Flow & Guards

* **Splash:** network + quick auth → Offline/Config/Profile.
* **PIN:** 4-digit, 3 attempts, 5-min lockout.
* **Resume thresholds:** <5% restart; 5–94% resume; ≥95% clear CW.

---

## 10) Agent Responsibilities

* Add small fixtures in `src/test/resources` (≤8 KiB).
* If FTS/secure storage touched → add/update androidTest.
* Keep test DBs file-backed; unique file per test.
* Update PopularityCache/RailEntry rebuild triggers when relevant code touched.
* Respect exclusions (no M3U, no DVR, etc.).

---

## 11) Pull Request Template

**Commit message:**

```
[Single-Task] <short summary>
```

**PR title:**

```
<Module>: <Concrete change>  (Stateless Task)
```

**PR body must include:**

* **Change label:** Behavior-preserving / Scoped addition / Behavior change
* **3-line summary:**

    * What changed
    * What didn’t change
    * Why
* **Acceptance criteria checklist**
* **Spec references** (sections from Architecture Doc)
* **Self-audit summary (TestGate):**

    * Build/compile: ok
    * Tests: pass; failure ratio <10%
    * Coverage: ≥70% (value: …%)
    * Detekt/Lint: ok
    * Structure/Stack/Harness/Fixtures: ok
    * I18n (app UI): ok/n.a.
    * Instrumented (if applicable): ok/n.a.

---

## 12) Clean Code Guidelines

* **Simplicity first**: minimum logic; no speculative abstractions.
* **Tight scope**: each PR solves one requirement only.
* **DRY + SOLID**: avoid duplication; single responsibility; inject collaborators.
* **Size limits (soft):** classes ≤350 LOC, methods ≤50 LOC.
* **Max nesting:** ≤3 levels. Use early-return/refactor if deeper.
* **Name intent:** expressive helpers (e.g., `needsInitialSync()`).
* **Side effects at edges:** isolate IO/time/random/keystore/db.
* **Error handling:** fail fast; never swallow exceptions; no `printStackTrace`.
* **Concurrency:** no GlobalScope; inject dispatchers; structured tests with `runTest`.
* **Logging:** never `System.out`; use project logger.
* **Comments:** explain **why**, not what.

**Final step after every edit/addition:**
Review the code against these Clean Code guidelines before committing.
If the change introduces duplication, excessive nesting, unclear naming, or other violations — refactor immediately. No “spaghetti code” is acceptable.

---

## 13) Escalation Rules

If **after 3 total attempts** the TestGate report still fails:

1. **Commit what you have so far**

    * Use normal commit style.
    * Partial work must be preserved in Git history.

2. **Open a GitHub issue**
   Include:

    * **paste.rs links** to each failed TestGate report
    * The **instructions verbatim** from task input
    * A **summary of suspected cause**
    * A reference to the **commit hash**

3. **Stop coding until clarified**

    * Do not attempt a 4th change. Wait for human input.
