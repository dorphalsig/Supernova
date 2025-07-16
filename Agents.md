# Agents.md

## 1. Project Structure for OpenAI Codex Navigation

```text
/app
  /src
    /main
      /java/com/supernova
        /api
        /data
        /sync
        /workers
        /ui
      /res
        /layout
        /values
/tests
/docs
```

## 2. Application Architecture

### 2.1 High-Level Components

* **API Layer**: Retrofit/Moshi `ApiService` per-portal with endpoints for live categories, streams, series, movies, EPG (XMLTV), auth.
* **Persistence**: Room database with FTS4 index on `Stream` and `EpgProgramme`.
* **Workers**:

  * **LiveSyncWorker**: sync live-streams every 12 hours + on-demand.
  * **EpgSyncWorker**: sync EPG once per day (XMLTV format).
* **Secure Storage**: `SecureDataStore` (Tink/AesGcm) for credentials.
* **UI**: Compose + XML screens for login, profile, home, loading.

### 2.2 Frameworks & Libraries

* Kotlin, Coroutines, WorkManager, Hilt
* Retrofit, Moshi, OkHttp
* Room with FTS4
* Coil (image caching and preload)
* Compose + ViewBinding

### 2.3 Architectural Patterns

* MVVM + Repository + Use-Case layering
* Single source of truth: DB ← Worker ← ViewModel

### 2.4 Sync and Atomic Swap Logic

See Sync Architecture Updates.md for details depending on your task.
- Section 3. Live Stream Sync Flow
- Section 4. XMLTV EPG Sync Flow
- Section 5. Error Handling & Reporting
- Section 6. Account & Worker Lifecycle
- Section 7. FTS Index Refresh
- Section 8. TMDB Enrichment

## 3. Dead Code Policy

Remove unused or deprecated code immediately. Regular lint and static analysis must identify and eliminate dead code.

## 4. Programmatic Checks for OpenAI Codex

Automate the following pre-merge checks:

* Unit/UI tests
* Lint checks
* Minimum code coverage of 70%

## 5. Pull Request Guidelines

Every PR must include:

* Clear description
* References (tickets/issues)
* Screenshots for UI changes
* Feedback log attachment
* Minimal, focused scope

## 6. Unit Tests

Use MockK for ViewModel tests and an in-memory Room DB for repository tests. Target at least 70% coverage.

## 7. Focus & Navigation

Ensure D-pad navigation across all screens; define `nextFocus*` attributes in XML or Compose.

## 8. Feedback Log Requirements

Maintain `codex_build_log.md` with timestamped entries, actions, outcomes, and commit conventions.
Format for entries:
```# YYYY-MM-DD HH:MM — branch - message```
additionally provide direct feedback on the task with the same information added to the md

## 9. Pre-Run Environment Checks

Verify:
* ANDROID_SDK_ROOT=/opt/sdk
* ANDROID_HOME=/opt/sdk
* Gradle wrapper permissions
* Java/Gradle versions
* Required environment variables
* Modules included in `settings.gradle`

## 10. Build & Test Automation Feedback
1. **Full Build**: Run `./build.sh assembleDebug`  **WITH NO REDIRECTS OR PIPES**.
2. **Error Analysis**: Review the `/tmp/build_error_context.md` report
3. **Fix and Repeat**: Address all errors and re-run until successful

## 11. Worker Schedules & Success Criteria
See Sync Architecture Updates.md for details depending on your task.
  - Section 2. Sync Frequency & Scheduling
## 12. Definition of Done
* All tests passing
* Feedback log updated and committed
## 13. Architecture Documentation
The architecture documentation is stored in the `/docs` folder. Each chapter is a separate markdown file.
The complete document is available at `Supernova - Architecture Design Document.md`.

> When referencing architectural decisions in a task, **explicitly include the document name and section number** (e.g., Section 4 of `Supernova - Architecture Design Document.md`).  
> The agent will not navigate multiple documents automatically unless directed.
