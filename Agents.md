# Supernova — LLM Coding Agent Operating Guide

> This file operationalizes the **Architecture Design Document** for day‑to‑day coding by an LLM agent. It encodes *rules, defaults, entrypoints, harness usage, and admin overhead* so deliveries are consistent.

---

## 1) Ground Rules

* **Single source of truth:** Follow the *Architecture Design Document* (current canvas). When in doubt, prefer the Arch doc.
* **No scope creep:** Do **not** add features marked as excluded (e.g., full grid EPG, DVR, M3U input).
* **Version pins:** Use the repo’s **`libs.versions.toml`** (or gradle catalogs) exactly. **Do not upgrade or change libraries** unless explicitly asked.
* **Room FTS:** Use **FTS4** (Room does not support FTS5). Keep FTS table DDL consistent with §7.1.
* **Streaming only:** Never load entire XC/TMDB payloads into memory. Use Moshi `JsonReader` + Okio (see §8.9).
* **Materialized rails:** UI queries are *simple*, ordered by `position` in `RailEntry` (§7.6). **Never build dynamic ORDER BY popularity in queries.**
* **Hero:** Use the resolver and de‑dup rules in §6.10. Maintain `HeroImpression` for 7‑day de‑dup.

---

## 2) Repository Layout & Naming

* Mirror prod packages in tests: `src/test` and `src/androidTest` (§10.2).
* Compose UI stays thin; most logic in ViewModels/use‑cases; **no UI instrumentation** for MVP.
* Entities & DAOs are **exhaustive** (see §7.3/§7.4). Do not invent new tables without approval.

---

## 3) Entry Points (Tasks the agent should run)

* **Default (unit‑only changes):**

    * `:app:check` (runs unit tests, lint, detekt)
* **If any of these paths change** → run **all tests** (`:app:ciAllTests`):

    * `src/androidTest/**`
    * FTS schema/DAO (`SearchDao`, FTS table DDL, FTS triggers)
    * Secure storage code (`SecureDataStore`, keystore usage)
* **Aliases available:**

    * Per‑variant: `instrumented<Variant>Test` → `connected<Variant>AndroidTest`
    * Aggregate: `instrumentedTest` (all variants) *(provided by the convention plugin)*

> CI will also expose `:app:ciAllTests` (depends on `check` + `connectedDebugAndroidTest`). Use it when change detection says so.

**After running tests:** Parse the **TestGate JSON report** from the **root project** at `./build/reports/` (always root-level, even when a single module task is invoked) and use it to determine pass/fail and any gating violations.

---

## 4) Testing Harness & Policies

* **Frameworks**

    * Unit tests: **JUnit 5**.  Instrumented tests: **AndroidJUnit4** (limited scope in §10.10).
    * **Forbidden:** Robolectric.
* **Annotations & style**

    * Every unit test uses `@UseTestHarness` (or `@PureUnitTest` for POJOs).
    * Name tests `given_When_Then`, one behavior per test.
* **Harness components to use** (see §10.3):

    * `RoomTestDbBuilder` (file‑backed DB, TRUNCATE journal mode, unique file per test) — for *JVM DAO tests*.
    * `JsonFixtureLoader`, `MockWebServerExtensions` — streaming/gzip/chunked responses.
    * `DbAssertionHelpers` — row counts, shadow‑swap, `RailEntry` ordering, simple FTS shape checks.
    * `FtsTestKit` *(androidTest)* — diacritics/case/ranking checks on device.
    * `SecureStorageTestKit` *(androidTest)* — keystore round‑trip + process‑death.
* **Coverage gate:** target ≥70%; tag critical tests with `@Tag("critical")` per §10.6.
* **Instrumentation scope (only):** FTS parity & secure storage (§10.10).

---

## 5) Data & Search Rules (must follow)

* **FTS sources:** `catalog` (Stream), `episodes` (Episode), `epg` (Program); **max results 30** (§6.5).
  \$1- Keep DAO LIMIT/OFFSET stable with deterministic tie-breakers.
* Device parity for diacritics/case/locales is validated by instrumented FTS tests.
* **Provider change:** Same **host domain** → keep data & update creds; different domain → wipe & initial sync (§8.8/§12.1).
* **TMDB cadence:** preload ≈7k once, then **weekly top‑up 200**; on refresh, recompute **PopularityCache** and rebuild first‑page `RailEntry` for affected rails (§8.2, §6.11).

---

## 6) Ingestion (XC/TMDB) — Required Behavior

* **Transport:** OkHttp + Okio streaming; prefer server gzip; timeouts set.
* **Parsing:** Moshi `JsonReader` streaming (JSON), `XmlPullParser` (XMLTV) (§8.9).
* **Batching:** 200–500 rows per transaction; **shadow tables + atomic swap** for catalog/EPG.
* **Cursors & retries:** maintain resume cursor; exponential backoff (max 3); bounded memory (≈1–2 MB) (§8.9).
* **Hooks:** after ingest → refresh FTS (shadow swap), recompute **PopularityCache**, rebuild **RailEntry** for impacted rails.

---

## 7) Rails, Hero, Popularity — Must‑implement

* **Rails materialization:** workers compute first‑page slices and write to `RailEntry(position)`; UI selects by `rail_id` + `profile_id` and orders by `position` (§7.6).
* **Hero selection:** Personalized → Editorial → None; exclude CW & Trending; maintain `HeroImpression` (7 days) (§6.10).
* **Popularity ordering:** apply to the **first \~6 rails** only; persist per‑item scores in `PopularityCache`; recompute incrementally and on weekly TMDB refresh (§6.11).

---

## 8) Screen Flow & Guards

* **Splash:** check **network** and a quick **auth**; route to Offline/Config/Profile (§4.1).
* **PIN:** 4‑digit, 3 attempts, 5‑min lockout (§6.7).
* **Resume thresholds:** <5% start over; 5–94% resume; ≥95% remove from CW (§6.3).

---

## 9) Admin Overhead the Agent Must Do

* Add small, focused fixtures under `src/test/resources` (≤ a few KB each).
* When touching FTS/secure storage: create/update **androidTest** files per §10.10.
* Keep test DBs **file‑backed** (not in‑memory); one unique file per test; do not flip PRAGMAs mid‑test.
* Update `PopularityCache` & `RailEntry` rebuild triggers when ingestion or TMDB parts are touched.
* Respect exclusions (no M3U, no grid EPG, no DVR, etc.).

---

## 10) Pull Request Checklist (copy/paste into PR)

* [ ] Follows Arch doc; no excluded features
* [ ] Uses streaming ingestion patterns (Moshi/Okio)
* [ ] Rails are **precomputed**; UI queries are `ORDER BY position`
* [ ] Hero logic honors de‑dup & 7‑day impression window
* [ ] Provider domain rule handled on config changes
* [ ] Tests: `:app:check` run locally; if FTS/secure storage/androidTest touched → ran `:app:ciAllTests`
* [ ] Coverage ≥70%; `@Tag("critical")` added for items in §10.6
* [ ] No Robolectric, no `runBlocking`/`Thread.sleep` in tests
* [ ] Fixtures small and under `src/test/resources`

---

## 11) Quick Examples (what to write / avoid)

**Do write** a worker that:

* reads XC JSON via `JsonReader` → maps to entities → writes 200–500 rows per transaction
* uses shadow tables + atomic swap → enqueues Popularity/rails recompute hooks

**Don’t write** UI queries that:

* sort by popularity or perform heavy joins at runtime
* call TMDB on scroll or on every Home open

**Do** create instrumented tests *only* for:

* FTS parity (diacritics/case/ranking)
* Secure storage round‑trip + process‑death

**Don’t** use Robolectric or add random JUnit4 in unit tests.

---

# Supernova — LLM Coding Agent Operating Guide

> This file operationalizes the **Architecture Design Document** for day‑to‑day coding by an LLM agent. It encodes *rules, defaults, entrypoints, harness usage, and admin overhead* so deliveries are consistent.

---

## 0) Ground Rules

* **Single source of truth:** Follow the *Architecture Design Document* (current canvas). When in doubt, prefer the Arch doc.
* **No scope creep:** Do **not** add features marked as excluded (e.g., full grid EPG, DVR, M3U input).
* **Version pins:** Use the repo’s **`libs.versions.toml`** (or gradle catalogs) exactly. **Do not upgrade or change libraries** unless explicitly asked.
* **Room FTS:** Use **FTS4** (Room does not support FTS5). Keep FTS table DDL consistent with §7.1.
* **Streaming only:** Never load entire XC/TMDB payloads into memory. Use Moshi `JsonReader` + Okio (see §8.9).
* **Materialized rails:** UI queries are *simple*, ordered by `position` in `RailEntry` (§7.6). **Never build dynamic ORDER BY popularity in queries.**
* **Hero:** Use the resolver and de‑dup rules in §6.10. Maintain `HeroImpression` for 7‑day de‑dup.

---

## 1) Repository Layout & Naming

* Mirror prod packages in tests: `src/test` and `src/androidTest` (§10.2).
* Compose UI stays thin; most logic in ViewModels/use‑cases; **no UI instrumentation** for MVP.
* Entities & DAOs are **exhaustive** (see §7.3/§7.4). Do not invent new tables without approval.

---

## 2) Entry Points (Tasks the agent should run)

* **Default (unit‑only changes):**

    * `:app:check` (runs unit tests, lint, detekt)
* **If any of these paths change** → run **all tests** (`:app:ciAllTests`):

    * `src/androidTest/**`
    * FTS schema/DAO (`SearchDao`, FTS table DDL, FTS triggers)
    * Secure storage code (`SecureDataStore`, keystore usage)
* **Aliases available:**

    * Per‑variant: `instrumented<Variant>Test` → `connected<Variant>AndroidTest`
    * Aggregate: `instrumentedTest` (all variants) *(provided by the convention plugin)*

> CI will also expose `:app:ciAllTests` (depends on `check` + `connectedDebugAndroidTest`). Use it when change detection says so.

---

## 3) Testing Harness & Policies

* **Frameworks**

    * Unit tests: **JUnit 5**.  Instrumented tests: **AndroidJUnit4** (limited scope in §10.10).
    * **Forbidden:** Robolectric.
* **Annotations & style**

    * Every unit test uses `@UseTestHarness` (or `@PureUnitTest` for POJOs).
    * Name tests `given_When_Then`, one behavior per test.
* **Harness components to use** (see §10.3):

    * `RoomTestDbBuilder` (file‑backed DB, TRUNCATE journal mode, unique file per test) — for *JVM DAO tests*.
    * `JsonFixtureLoader`, `MockWebServerExtensions` — streaming/gzip/chunked responses.
    * `DbAssertionHelpers` — row counts, shadow‑swap, `RailEntry` ordering, simple FTS shape checks.
    * `FtsTestKit` *(androidTest)* — diacritics/case/ranking checks on device.
    * `SecureStorageTestKit` *(androidTest)* — keystore round‑trip + process‑death.
* **Coverage gate:** target ≥70%; tag critical tests with `@Tag("critical")` per §10.6.
* **Instrumentation scope (only):** FTS parity & secure storage (§10.10).

---

## 4) Data & Search Rules (must follow)

* **FTS sources:** `catalog` (Stream), `episodes` (Episode), `epg` (Program); **max results 30** (§6.5).
* **Ranking:** exact > fuzzy > boosted; voice search via system intent.
* **Provider change:** Same **host domain** → keep data & update creds; different domain → wipe & initial sync (§8.8/§12.1).
* **TMDB cadence:** preload ≈7k once, then **weekly top‑up 200**; on refresh, recompute **PopularityCache** and rebuild first‑page `RailEntry` for affected rails (§8.2, §6.11).

---

## 5) Ingestion (XC/TMDB) — Required Behavior

* **Transport:** OkHttp + Okio streaming; prefer server gzip; timeouts set.
* **Parsing:** Moshi `JsonReader` streaming (JSON), `XmlPullParser` (XMLTV) (§8.9).
* **Batching:** 200–500 rows per transaction; **shadow tables + atomic swap** for catalog/EPG.
* **Cursors & retries:** maintain resume cursor; exponential backoff (max 3); bounded memory (≈1–2 MB) (§8.9).
* **Hooks:** after ingest → refresh FTS (shadow swap), recompute **PopularityCache**, rebuild **RailEntry** for impacted rails.

---

## 6) Rails, Hero, Popularity — Must‑implement

* **Rails materialization:** workers compute first‑page slices and write to `RailEntry(position)`; UI selects by `rail_id` + `profile_id` and orders by `position` (§7.6).
* **Hero selection:** Personalized → Editorial → None; exclude CW & Trending; maintain `HeroImpression` (7 days) (§6.10).
* **Popularity ordering:** apply to the **first \~6 rails** only; persist per‑item scores in `PopularityCache`; recompute incrementally and on weekly TMDB refresh (§6.11).

---

## 7) Screen Flow & Guards

* **Splash:** check **network** and a quick **auth**; route to Offline/Config/Profile (§4.1).
* **PIN:** 4‑digit, 3 attempts, 5‑min lockout (§6.7).
* **Resume thresholds:** <5% start over; 5–94% resume; ≥95% remove from CW (§6.3).

---

## 8) Admin Overhead the Agent Must Do

* Add small, focused fixtures under `src/test/resources` (≤ a few KB each).
* When touching FTS/secure storage: create/update **androidTest** files per §10.10.
* Keep test DBs **file‑backed** (not in‑memory); one unique file per test; do not flip PRAGMAs mid‑test.
* Update `PopularityCache` & `RailEntry` rebuild triggers when ingestion or TMDB parts are touched.
* Respect exclusions (no M3U, no grid EPG, no DVR, etc.).

---

## 9) Pull Request Checklist (copy/paste into PR)

* [ ] Follows Arch doc; no excluded features
* [ ] Uses streaming ingestion patterns (Moshi/Okio)
* [ ] Rails are **precomputed**; UI queries are `ORDER BY position`
* [ ] Hero logic honors de‑dup & 7‑day impression window
* [ ] Provider domain rule handled on config changes
* [ ] Tests: `:app:check` run locally; if FTS/secure storage/androidTest touched → ran `:app:ciAllTests`
* [ ] Coverage ≥70%; `@Tag("critical")` added for items in §10.6
* [ ] No Robolectric, no `runBlocking`/`Thread.sleep` in tests
* [ ] Fixtures small and under `src/test/resources`

---

## 10) Quick Examples (what to write / avoid)

**Do write** a worker that:

* reads XC JSON via `JsonReader` → maps to entities → writes 200–500 rows per transaction
* uses shadow tables + atomic swap → enqueues Popularity/rails recompute hooks

**Don’t write** UI queries that:

* sort by popularity or perform heavy joins at runtime
* call TMDB on scroll or on every Home open

**Do** create instrumented tests *only* for:

* FTS parity (diacritics/case/ranking)
* Secure storage round‑trip + process‑death

**Don’t** use Robolectric or add random JUnit4 in unit tests.

---

## 11) Clean Code (guidance, not gated)

* **Simplicity first**: implement the minimum logic that fulfills the requirement—no speculative abstractions.
* **DRY + SOLID**: avoid duplication; single clear responsibility; inject collaborators; program to interfaces only when it improves testability/readability.
* **Size limits** (soft): classes ≤ 350 LOC, methods ≤ 50 LOC.
* **Max nesting**: 3 levels (if/when/loop). Refactor or early-return beyond that.
* **Name the intent**: prefer small helpers that express domain rules (e.g., needsInitialSync()), otherwise inline or use local functions for single-use helpers.
* **Side effects at the edge**: isolate IO/time/random/keystore/db swaps behind small seams.
* **Error handling: don’t swallow exceptions**; fail fast with clear messages; avoid printStackTrace/raw println.
* **Concurrency sanity**: no GlobalScope; inject dispatchers; use structured coroutine tests (runTest + advanceUntilIdle/advanceTimeBy when timing matters).
* **Logging**: no System.out/err; use the project logger or structured logs.
* **Comments:** explain why, not what. Prefer expressive code + tests to excessive commentary.