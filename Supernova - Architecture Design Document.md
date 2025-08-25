# Supernova — Architecture Design Document

*Last updated: 2025‑08‑24*

> **Normative specification.** This document fixes **all interfaces** between modules. Developers implement internals only. Architecture/design decisions are **not delegated**.

---

## 1. Purpose & Scope

Design for a lean Android TV application (“Supernova”) that ingests XC IPTV and TMDB data, persists it locally, and presents a fast, focused TV UI. This spec is authoritative: data model (DDS), APIs, UI behaviors, player overlays, theming, testing, and **module contracts**.

**Non‑goals (MVP):** live UI testing (Espresso/ComposeTest), Robolectric, M3U ingestion, skip‑intro/outro heuristics, ad tech, multi‑provider merge.

---

## 2. Principles

* **Performance by design:** stream parse, batch writes, pre‑materialize rails.
* **Simplicity:** keep DAO queries trivial; move complexity to sync/materialization.
* **Local‑first UX:** offline tolerant; UI never calls remote APIs.
* **Deterministic:** explicit contracts for ranking, rails, and hero selection.
* **Testable:** standard harness + minimal instrumented checks.

---

## 3. Platforms, Modules & Composition Root

### 3.1 Target

* Android TV (API 24+), Kotlin, Jetpack Compose, Media3.

### 3.2 Repository Modules (Gradle subprojects)

* `:app` — UI shell (Compose), navigation, view models, **composition root**.
* `:domain` — pure Kotlin: models, mappers, use cases, **interfaces** shared across modules.
* `:data` — Android lib: Room (FTS4), rails, popularity caches; **DAO interfaces**.
* `:network` — JVM lib: XC/TMDB clients (OkHttp/Moshi/XmlPull); **client interfaces**.
* `:sync` — Android lib: ingestion pipelines, workers, cursors; **sync interfaces**.
* `:security` — Android lib: Keystore secure storage; **storage interface**.
* `:testing-harness` — JVM test utilities.
* `:testing-instrumented` — Android test utilities (FTS + secure storage).

**Deps shape (fixed)**

```
:app ─► :domain, :sync, :security
:sync ─► :network, :data, :domain
:data ─► :domain
:network ─► (independent)
```

### 3.3 Composition & DI (fixed)

* No DI framework. Composition occurs in `:app` via `Providers.kt` factories.
* All cross‑module usage goes through **interfaces defined in \:domain**. Concrete classes are bound in `Providers`.

---

## 4. App Flow, Navigation & Error Model

### 4.1 Entry & Splash

* Splash performs **network reachability** and **quick‑auth** ping.
* Outcomes:

    * OK ⇒ Profile selection / Home.
    * Invalid/missing creds ⇒ Config flow.
    * Unavailable (timeout/5xx) ⇒ Offline error screen with Retry.
* Provider **identity is the domain**. Same domain ⇒ keep local catalog & watch data; update creds. Different domain ⇒ wipe catalog and force initial sync.

### 4.2 Top‑level Destinations

* **Home** (hero + mixed rails), **Movies**, **Series**, **Search**, **Profile**, **Settings**.
* Movies & Series share Home layout; rails filtered by type.

### 4.3 Navigation Contracts (fixed)

Route schema:

```
route Home
route Movies
route Series
route Search
route MovieDetail/{id}        // id: VodItem.id
route SeriesDetail/{id}       // id: Series.id
route Player/{type}/{id}      // type: live|movie|episode
route Profile
route Settings
```

* All route params are **String**.

### 4.4 Error Model (domain‑level)

```kotlin
sealed interface AppError {
  data object NetworkUnavailable : AppError
  data object InvalidCredentials : AppError
  data class RemoteFailure(val code: Int, val message: String?) : AppError
  data object NotFound : AppError
  data object Unknown : AppError
}
```

* ViewModels expose `StateFlow<UiState>` that embeds `AppError?` when applicable.

---

## 5. Wireframes — Sizes & Behaviors

*Baseline canvas: 1920×1080, 8dp grid.*

### 5.1 Splash

* Centered logo; below: status text (auth/network). Retry button appears on failure.
* Spacing: logo top inset 240dp; button min width 240dp.

### 5.2 Home / Movies / Series

* **Hero**: 1920×480dp (full‑bleed). Title + 2 CTAs; background blurred poster/backdrop.
* **Rails**: vertical stack; each rail is horizontal list.

    * Poster card: **180×270dp**; Landscape card: **320×180dp**.
    * Card spacing: 24dp horizontal; 16dp vertical between rails.
    * Rail title inset 90dp; content inset 90dp.
* Focus: scale **1.06×**, focus ring, elevation 8dp.

### 5.3 Details — Movie / Series

* **MovieDetail**: poster 240×360dp left; meta panel right; actions row; similar content rail.
* **SeriesDetail**: seasons → episodes (EpisodeDetail **merged** here). Episodes show runtime badges.

### 5.4 Search

* Search field top; results grid 180×270dp cards. Empty state centered.

### 5.5 Player Overlay (VOD & Live)

* Bottom scrub bar + controls; top info bar optional.
* Controls row: Back, Play/Pause, Seek −10/+10, Next/Prev (series only), Audio, Subtitles, Settings.
* Audio/Subtitles open side panel of tracks; selecting applies immediately.

### 5.6 EPG Overlay (Live)

* INFO overlays Now/Next for current channel from local EPG.
* Up/Down days; Left/Right channels; OK tunes.

> *Skip intro/outro*: out of MVP; requires external markers we do not have.

---

## 6. Recommendations & Content Strategy

* Heroes per section: Home (editorial/personalized), Movies (top‑trending movie), Series (top‑trending series). 7‑day no‑repeat per profile.
* Rails are pre‑materialized; **UI always orders by `position`**.
* Rail ordering: first **two screenfuls** sorted by **popularity**; beyond that, stable category order. Popularity caches **refresh** whenever TMDB data is fetched.

---

## 7. Data Model (DDS) — Entities, Indexes & FTS (normative)

### 7.1 Core Entities

(Fields and indexes are **final**. Types are Room column types.)

#### Provider

| Field          | Type    | Null | Notes                |
| -------------- | ------- | ---- | -------------------- |
| `domain`       | TEXT PK | NO   | identity of provider |
| `username`     | TEXT    | NO   | encrypted at rest    |
| `display_name` | TEXT    | YES  |                      |

#### Profile

| Field    | Type    | Null | Notes    |
| -------- | ------- | ---- | -------- |
| `id`     | TEXT PK | NO   | UUID     |
| `name`   | TEXT    | NO   |          |
| `avatar` | TEXT    | YES  | URL/path |

#### Stream (Live)

| Field            | Type    | Null | Index              |
| ---------------- | ------- | ---- | ------------------ |
| `id`             | TEXT PK | NO   | XC `stream_id`     |
| `title`          | TEXT    | NO   | `idx_stream_title` |
| `category_id`    | TEXT    | YES  | `idx_stream_cat`   |
| `number`         | INTEGER | YES  | channel number     |
| `epg_channel_id` | TEXT    | YES  | `idx_stream_epg`   |

#### VodItem (Movies)

| Field         | Type    | Null | Index           |
| ------------- | ------- | ---- | --------------- |
| `id`          | TEXT PK | NO   | XC `stream_id`  |
| `title`       | TEXT    | NO   | `idx_vod_title` |
| `category_id` | TEXT    | YES  | `idx_vod_cat`   |
| `tmdb_id`     | TEXT    | YES  | `idx_vod_tmdb`  |

#### Series

| Field     | Type    | Null | Index              |
| --------- | ------- | ---- | ------------------ |
| `id`      | TEXT PK | NO   | XC `series_id`     |
| `title`   | TEXT    | NO   | `idx_series_title` |
| `tmdb_id` | TEXT    | YES  | `idx_series_tmdb`  |

#### Episode

| Field         | Type              | Null | Index           |
| ------------- | ----------------- | ---- | --------------- |
| `id`          | TEXT PK           | NO   | XC `episode_id` |
| `series_id`   | TEXT FK→Series.id | NO   | `idx_ep_series` |
| `season_num`  | INTEGER           | NO   |                 |
| `episode_num` | INTEGER           | YES  |                 |
| `title`       | TEXT              | NO   | `idx_ep_title`  |
| `runtime_min` | INTEGER           | YES  |                 |

#### Program (EPG)

| Field            | Type       | Null | Index                    |
| ---------------- | ---------- | ---- | ------------------------ |
| `id`             | INTEGER PK | NO   | autoinc                  |
| `epg_channel_id` | TEXT       | NO   | `idx_prog_channel_start` |
| `title`          | TEXT       | NO   |                          |
| `start_epoch`    | INTEGER    | NO   | `idx_prog_channel_start` |
| `end_epoch`      | INTEGER    | NO   |                          |

#### TmdbMetadata

| Field           | Type    | Null | Index       |     |
| --------------- | ------- | ---- |-------------|-----|
| `tmdb_id`       | TEXT PK | NO   |             |     |
| `type`          | TEXT    | NO   | movie       | tv  |
| `genres_json`   | TEXT    | YES  |             |     |
| `keywords_json` | TEXT    | YES  |             |     |
| `release_date`  | TEXT    | YES  | `YYYY‑MM‑DD` |     |

#### Rail

| Field   | Type    | Null | Notes                      |
| ------- | ------- | ---- |----------------------------|
| `id`    | TEXT PK | NO   | e.g., `trending_movies`    |
| `title` | TEXT    | NO   | localized                  |
| `scope` | TEXT    | NO   | 'home',' movies', 'series` |

#### RailEntry (materialized)

**#### RailEntry (materialized)

| Field        | Type               | Null | Index            | Notes                                         |
|--------------|--------------------|------|------------------|-----------------------------------------------|
| `rail_id`    | TEXT FK→Rail.id    | NO   | `uniq_rail_pos`  |                                               |
| `profile_id` | TEXT FK→Profile.id | YES  | `uniq_rail_pos`  |                                               |
| `content_type` | TEXT             | NO   |                  | Allowed: `live`, `movie`, `series`, `episode` |
| `content_id` | TEXT               | NO   |                  |                                               |
| `position`   | INTEGER            | NO   | `uniq_rail_pos`  |                                               |
| `score`      | REAL               | YES  |                  | Informational only                            |

*Constraint*: unique (`rail_id`, `profile_id` NULLS FIRST, `position`).

#### PopularityCache

| Field           | Type    | Null | Index             | Notes                                         |
|-----------------|---------|------|-------------------|-----------------------------------------------|
| `content_type`  | TEXT    | NO   | `idx_pop_type`    | Allowed: `live`, `movie`, `series`, `episode` |
| `content_id`    | TEXT    | NO   | `idx_pop_content` |                                               |
| `score`         | REAL    | NO   |                   | Higher = more popular; queries order **DESC**  |
| `as_of_epoch`   | INTEGER | NO   |                   | Unix epoch seconds; used for TTL/refresh      |

*Unique*: (`content_type`, `content_id`).


#### SyncState

| Field        | Type    | Null         |
| ------------ | ------- | ------------ |
| `endpoint`   | TEXT PK | NO           |
| `cursor`     | TEXT    | YES          |
| `offset`     | INTEGER | YES          |
| `count`      | INTEGER | NO DEFAULT 0 |
| `updated_at` | INTEGER | NO           |

#### HeroImpression

| Field        | Type    | Null | Index                   |
| ------------ | ------- | ---- | ----------------------- |
| `profile_id` | TEXT    | NO   | `idx_hero_profile_time` |
| `content_id` | TEXT    | NO   |                         |
| `shown_at`   | INTEGER | NO   | `idx_hero_profile_time` |

#### SecureItem

| Field  | Type    | Null |                 |
| ------ | ------- | ---- | --------------- |
| `key`  | TEXT PK | NO   |                 |
| `blob` | BLOB    | NO   | encrypted value |

### 7.2 FTS (Search)

* **Engine:** `FTS4` with `unicode61` tokenizer and `remove_diacritics=2`.
* **Virtual table:** `SearchFts(content="ContentIndex", title_norm, cast, keywords)`.
* **Ranking:** bm25 with boosts: `title_norm×2.0`, `keywords×1.2`, `cast×1.0`.
* **Triggers:** maintain FTS with external‑content triggers.

### 7.3 DAO Interfaces (domain contracts)

*Package `com.supernova.domain.data` — implemented in `:data`*

```kotlin
interface RailEntryDao {
  suspend fun list(railId: String, profileId: String?): List<RailEntryRef>
}

data class RailEntryRef(val contentType: String, val contentId: String, val position: Int)

interface StreamDao {
  suspend fun byId(id: String): Stream?
}
interface VodDao {
  suspend fun byId(id: String): VodItem?
}
interface SeriesDao {
  suspend fun byId(id: String): Series?
  suspend fun episodes(seriesId: String): List<Episode>
}
interface ProgramDao {
  suspend fun nowNext(epgChannelId: String, nowEpoch: Long): List<Program> // size ≤ 2
}
interface SearchDao {
  suspend fun search(query: String, limit: Int = 30): List<SearchHit>
}

data class SearchHit(val contentType: String, val contentId: String, val score: Double)
```

Queries are **fixed** to remain simple (PK lookups, FTS MATCH, and `ORDER BY position`).

---

## 8. Ingestion, Networking & Sync

### 8.1 Network Clients (`:network`) — Interfaces (fixed)

*Package `com.supernova.domain.net` — implemented in `:network`*

```kotlin
data class XcCreds(val username: String, val password: String)

enum class AuthStatus { OK, INVALID, ERROR }

data class LiveStreamDto(val id: String, val name: String, val categoryId: String?, val num: Int?)
data class VodDto(val id: String, val name: String, val categoryId: String?)
data class SeriesDto(val id: String, val name: String)
data class SeriesInfoDto(val seriesId: String, val seasons: List<SeasonDto>)
data class SeasonDto(val number: Int, val episodes: List<EpisodeDto>)
data class EpisodeDto(val id: String, val title: String, val episodeNum: Int?)
data class EpgItemDto(val title: String, val startEpoch: Long, val endEpoch: Long, val channelId: String)

interface XcClient {
  suspend fun quickAuth(creds: XcCreds): AuthStatus
  fun streamLiveStreams(creds: XcCreds, categoryId: String? = null): Flow<LiveStreamDto>
  fun streamVod(creds: XcCreds, categoryId: String? = null): Flow<VodDto>
  fun streamSeries(creds: XcCreds): Flow<SeriesDto>
  suspend fun seriesInfo(creds: XcCreds, seriesId: String): SeriesInfoDto
  fun streamEpg(creds: XcCreds, startEpoch: Long?, endEpoch: Long?): Flow<EpgItemDto>
}

data class TmdbConfig(val baseUrl: String, val posterSizes: List<String>, val backdropSizes: List<String>)
data class TmdbMeta(val id: String, val type: String, val genres: List<String>, val keywords: List<String>, val releaseDate: String?)

interface TmdbClient {
  suspend fun configuration(): TmdbConfig
  suspend fun searchMovie(title: String, year: Int?, language: String): List<TmdbMeta>
  suspend fun searchTv(title: String, year: Int?, language: String): List<TmdbMeta>
  suspend fun movieDetails(id: String, language: String): TmdbMeta
  suspend fun tvDetails(id: String, language: String): TmdbMeta
  suspend fun trendingMovieDay(language: String): List<TmdbMeta>
  suspend fun trendingTvDay(language: String): List<TmdbMeta>
}
```

All functions are **suspend** or **Flow** and must **stream** parse; no full buffering.

### 8.2 HTTP Client Policy (fixed)

* Library: **OkHttp** (+ Moshi streaming, XmlPullParser for XMLTV).
* **Timeouts:** connect 2s, read 3s, write 3s; call timeout 10s.
* **Backoff:** on `429` or `5xx`, use **exponential backoff with full jitter**: base 250ms, factor×2, max 8s; honor `Retry-After` if present.
* **Concurrency & rate:**

    * **TMDB:** token‑bucket limiter **10 rps** (burst 20), Dispatcher `maxRequestsPerHost=8`, `maxRequests=16`. If `429`, halve the bucket for 60s.
    * **XC:** Dispatcher `maxRequestsPerHost=2`, `maxRequests=4` (be gentle with providers).
* **Caching:** TMDB responses cache **50 MB** disk; respect Cache‑Control. XC: **no cache**.
* **Compression:** gzip enabled; transparent decoding.

### 8.3 Sync Engine (`:sync`) — Interfaces (fixed)

*Package `com.supernova.domain.sync` — implemented in `:sync`*

```kotlin
interface SyncEngine { suspend fun runInitial(): SyncReport; suspend fun runIncremental(): SyncReport }

data class SyncReport(val succeeded: Boolean, val batches: Int, val items: Int)

interface CatalogSync { suspend fun runXcCatalog(creds: XcCreds); suspend fun runEpg(creds: XcCreds); suspend fun runTmdbRefresh() }
interface RailMaterializer { suspend fun rebuildAll(profileId: String?); suspend fun rebuildRails(railIds: List<String>, profileId: String?) }
interface PopularityRefresher { suspend fun recomputeFirstPage() }
```

* **Post‑ingest order:** refresh FTS → recompute popularity (first page) → rebuild rails.

### 8.4 Scheduling (fixed)

* **EPG refresh:** WorkManager **periodic 20h**, flex 2h. Constraints: `CONNECTED`, `BATTERY_NOT_LOW`.
* **TMDB trending/config:** periodic **7 days**, flex 12h.
* **XC catalog incremental:** periodic **7 days**; also **manual Refresh** action triggers immediate one‑shot.
* **Foreground triggers:** app open to Home may enqueue a **unique** one‑shot `runIncremental()` if last success >24h.

---

## 9. API Contracts (XC + TMDB) — OpenAPI‑style (client subset)

> **Normative**. These contracts define exactly what the client calls and the **only** fields it reads. Parsing is **streaming‑only** (OkHttp+Moshi `JsonReader`, `XmlPullParser`). `/get.php` (M3U) is **not used**.

### 9.1 XC IPTV API (client subset)

```yaml
openapi: 3.0.0
info:
  title: XC IPTV API — Client Contract (Supernova)
  version: "2.0"
servers:
  - url: http://{server}:{port}
    variables:
      server: { default: "example.com" }
      port:   { default: "8080" }

tags:
  - name: Auth
  - name: Catalog
  - name: EPG
  - name: Playback

paths:
  /player_api.php:
    get:
      summary: Auth & catalog (action‑driven)
      description: |
        When `action` is omitted, treat as **quick‑auth ping** and read only `auth.status`.
        For catalog actions, responses are parsed as **streams** and only the fields below are read.
      tags: [Auth, Catalog]
      parameters:
        - $ref: '#/components/parameters/Username'
        - $ref: '#/components/parameters/Password'
        - name: action
          in: query
          required: false
          schema:
            type: string
            enum: [get_live_streams, get_vod_streams, get_series, get_series_info]
        - name: category_id
          in: query
          required: false
          schema: { type: integer }
        - name: series_id
          in: query
          required: false
          schema: { type: integer }
          description: Required when `action=get_series_info`.
      responses:
        '200':
          description: JSON (shape depends on `action`)
          content:
            application/json:
              schema:
                oneOf:
                  - $ref: '#/components/schemas/AuthResponse'     # no action
                  - $ref: '#/components/schemas/LiveStreamList'   # get_live_streams
                  - $ref: '#/components/schemas/VodStreamList'    # get_vod_streams
                  - $ref: '#/components/schemas/SeriesList'       # get_series
                  - $ref: '#/components/schemas/SeriesInfo'       # get_series_info

  /xmltv.php:
    get:
      summary: EPG data (XMLTV)
      tags: [EPG]
      parameters:
        - $ref: '#/components/parameters/Username'
        - $ref: '#/components/parameters/Password'
        - { name: start, in: query, required: false, schema: { type: string, format: date-time } }
        - { name: end,   in: query, required: false, schema: { type: string, format: date-time } }
      responses:
        '200':
          description: XMLTV stream (client uses pull parser)
          content: { application/xml: { schema: { type: string } } }

  /live/{username}/{password}/{stream_id}.ts:
    get:
      summary: Live transport (TS)
      tags: [Playback]
      parameters:
        - $ref: '#/components/parameters/UsernamePath'
        - $ref: '#/components/parameters/PasswordPath'
        - { name: stream_id, in: path, required: true, schema: { type: integer } }
      responses:
        '200': { description: MPEG‑TS stream }

  /movie/{username}/{password}/{stream_id}.{ext}:
    get:
      summary: VOD transport
      tags: [Playback]
      parameters:
        - $ref: '#/components/parameters/UsernamePath'
        - $ref: '#/components/parameters/PasswordPath'
        - { name: stream_id, in: path, required: true, schema: { type: integer } }
        - { name: ext, in: path, required: true, schema: { type: string }, description: "e.g., mp4, mkv, ts" }
      responses:
        '200': { description: VOD stream }

  /series/{username}/{password}/{stream_id}.{ext}:
    get:
      summary: Episode transport
      tags: [Playback]
      parameters:
        - $ref: '#/components/parameters/UsernamePath'
        - $ref: '#/components/parameters/PasswordPath'
        - { name: stream_id, in: path, required: true, schema: { type: integer } }
        - { name: ext, in: path, required: true, schema: { type: string } }
      responses:
        '200': { description: Episode stream }

components:
  parameters:
    Username:     { name: username, in: query, required: true, schema: { type: string } }
    Password:     { name: password, in: query, required: true, schema: { type: string } }
    UsernamePath: { name: username, in: path,  required: true, schema: { type: string } }
    PasswordPath: { name: password, in: path,  required: true, schema: { type: string } }

  schemas:
    AuthResponse:
      type: object
      properties:
        auth:
          type: object
          properties:
            status:
              type: string
              description: One of OK | INVALID | ERROR
      required: [auth]

    LiveStreamList:
      type: array
      items: { $ref: '#/components/schemas/LiveStreamItem' }
    LiveStreamItem:
      type: object
      description: Minimal fields read for live channels.
      properties:
        stream_id:   { type: string }
        name:        { type: string }
        category_id: { type: string }
        num:         { type: integer, description: "Channel number if provided" }
      required: [stream_id, name]

    VodStreamList:
      type: array
      items: { $ref: '#/components/schemas/VodStreamItem' }
    VodStreamItem:
      type: object
      description: Minimal fields read for movies catalog.
      properties:
        stream_id:   { type: string }
        name:        { type: string }
        category_id: { type: string }
      required: [stream_id, name]

    SeriesList:
      type: array
      items: { $ref: '#/components/schemas/SeriesItem' }
    SeriesItem:
      type: object
      description: Series directory listing (no episodes here).
      properties:
        series_id: { type: string }
        name:      { type: string }
      required: [series_id, name]

    SeriesInfo:
      type: object
      description: Per‑series seasons & episodes (action=get_series_info).
      properties:
        series_id: { type: string }
        seasons:
          type: array
          items: { $ref: '#/components/schemas/Season' }
      required: [series_id]
    Season:
      type: object
      properties:
        season_number: { type: integer }
        episodes:
          type: array
          items: { $ref: '#/components/schemas/Episode' }
    Episode:
      type: object
      properties:
        episode_id:  { type: string }
        title:       { type: string }
        episode_num: { type: integer }

x-notes:
  scope:
    - Client does **not** consume /get.php (M3U).
    - Client reads **only** the fields defined above, using streaming parsers and batched writes.
  provider-identity:
    - Providers are identified by **domain**. Same domain ⇒ keep catalog & update creds; different domain ⇒ wipe and re‑sync.
```

### 9.2 TMDB API (client subset)

```yaml
openapi: 3.0.0
info:
  title: TMDB API — Client Contract (Supernova)
  version: "1.0"
servers:
  - url: https://api.themoviedb.org/3
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
security:
  - bearerAuth: []
paths:
  /configuration:
    get:
      summary: Image configuration
      responses:
        '200':
          description: Image base URL and sizes
          content:
            application/json:
              schema:
                type: object
                properties:
                  images:
                    type: object
                    properties:
                      base_url: { type: string }
                      poster_sizes: { type: array, items: { type: string } }
                      backdrop_sizes: { type: array, items: { type: string } }
  /search/movie:
    get:
      summary: Search movies
      parameters:
        - { name: query, in: query, required: true, schema: { type: string } }
        - { name: year,  in: query, required: false, schema: { type: integer } }
        - { name: language, in: query, required: false, schema: { type: string } }
      responses:
        '200': { description: Results; client reads only minimal fields }
  /search/tv:
    get:
      summary: Search TV
      parameters:
        - { name: query, in: query, required: true, schema: { type: string } }
        - { name: first_air_date_year, in: query, required: false, schema: { type: integer } }
        - { name: language, in: query, required: false, schema: { type: string } }
      responses:
        '200': { description: Results; client reads only minimal fields }
  /movie/{id}:
    get:
      summary: Movie details (keywords appended)
      parameters:
        - { name: id, in: path, required: true, schema: { type: string } }
        - { name: append_to_response, in: query, required: false, schema: { type: string }, example: "keywords" }
        - { name: language, in: query, required: false, schema: { type: string } }
      responses:
        '200': { description: Minimal fields consumed }
  /tv/{id}:
    get:
      summary: TV details (keywords appended)
      parameters:
        - { name: id, in: path, required: true, schema: { type: string } }
        - { name: append_to_response, in: query, required: false, schema: { type: string }, example: "keywords" }
        - { name: language, in: query, required: false, schema: { type: string } }
      responses:
        '200': { description: Minimal fields consumed }
  /trending/movie/day:
    get:
      summary: Trending movies (day)
      responses: { '200': { description: Minimal fields consumed } }
  /trending/tv/day:
    get:
      summary: Trending TV (day)
      responses: { '200': { description: Minimal fields consumed } }

x-client-fields:
  - Persist only: `id`, `title|name`, `release_date|first_air_date`, `genres[]`, `keywords[]`, and local `type` (`movie|tv`).
  - Cache `/configuration` and trending for ≥7 days.
  - Language param = device locale; fallback to `en` when strings are empty.
```

---

## 10. Testing Strategy & Harness

### 10.1 Policy

* JVM tests: JUnit5; coroutines via `runTest` + Main dispatcher rule.
* Instrumented tests (device): **only** FTS parity & Secure storage.
* Bans: Robolectric, Espresso, Compose UI Test.

### 10.2 DAO/Repository Test Conventions (fixed)

* **DB setup:** use `BaseRoomTest.inMemoryDb()` or `RoomTestDbBuilder.inMemory` only.
* **Transactions:** avoid direct SQL; test via DAOs with **simple queries** (PK/FTS/`ORDER BY position`).
* **FTS parity:** use `DbAssertionHelpers.assertFtsMatch` (JVM) and `FtsTestKit` (device) for Unicode/diacritics.
* **Fixtures:** JSON under `src/test/resources/…` with **size 256 B–8 KiB**.
* **Naming:** `*DaoTest`, `*RepoTest`.

### 10.3 Harness (JVM) — Public API

*Package: `com.supernova.testing`*

```kotlin
abstract class BaseRoomTest { protected fun inMemoryDb(): RoomDatabase; protected fun <T> withDb(block: (RoomDatabase) -> T): T }
object RoomTestDbBuilder { fun <T : RoomDatabase> inMemory(context: Context, klass: KClass<T>): T }
object DbAssertionHelpers { fun assertRowCount(expected: Int, query: () -> Int); fun <T> assertContains(expected: T, actual: Collection<T>); fun assertFtsMatch(query: String, expectIdsInOrder: List<String>, run: (String) -> List<String>) }
object JsonFixtureLoader { inline fun <reified T> load(path: String): T; fun openRaw(path: String): BufferedSource }
object MockWebServerExtensions { fun MockWebServer.enqueueJson(json: String, code: Int = 200); fun MockWebServer.enqueueFixture(path: String, code: Int = 200) }
class MainDispatcherRule : TestWatcher() { val dispatcher: TestDispatcher }
object CoroutineTestUtils { suspend fun <T> runTest(block: suspend TestScope.() -> T): T }
object UiStateTestHelpers { suspend fun <T> collectStates(flow: Flow<T>, take: Int = 1): List<T> }
object PreviewFactories { fun movie(id: String = "m1"): Movie; fun series(id: String = "s1"): Series; fun episode(id: String = "e1", seriesId: String = "s1"): Episode }
class SyncScenarioFactory(val network: NetworkClient, val db: RoomDatabase) { suspend fun runXcCatalogSync(): SyncReport; suspend fun runTmdbRefresh(): SyncReport }
object TestEntityFactory { fun stream(id: String = "1", title: String = "Ch 1"): Stream; fun vod(id: String = "2", title: String = "Movie"): VodItem; fun series(id: String = "3", title: String = "Series"): Series; fun episode(id: String = "4", seriesId: String = "3"): Episode }
```

### 10.4 Instrumented Toolbox — `:testing-instrumented`

*Package: `com.supernova.testing.instrumented`*

```kotlin
class FtsTestKit(private val db: RoomDatabase) {
  fun seed(vararg items: Pair<String, String>)
  fun assertMatch(query: String, expectIdsInOrder: List<String>)
  fun assertRankingStable(query: String)
}
class SecureStorageTestKit(private val context: Context) {
  fun assertRoundTrip(key: String, value: ByteArray)
  fun assertSurvivesProcessDeath(key: String)
}
```

---

## 11. UI Components Library (TV‑focused)

UI Components Library (TV‑focused)

### 11.1 Focusable primitives (fixed)

* `focusable=true`, focus ring, scale **1.06×** on focus, elevation 8dp (2dp at rest).
* Safe‑area left inset **90dp** for rails & section titles.
* Min target size **48dp**.

### 11.2 Components (signatures are **final**)

*Package `com.supernova.ui.components`*

```kotlin
@Composable fun FocusableButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, iconStart: Painter? = null)
@Composable fun FocusableCardPoster(title: String, imageUrl: String?, onClick: () -> Unit, modifier: Modifier = Modifier)
@Composable fun FocusableCardLandscape(title: String, imageUrl: String?, onClick: () -> Unit, modifier: Modifier = Modifier)
@Composable fun Rail(title: String, content: @Composable RowScope.() -> Unit)
@Composable fun TopAppBanner(title: String, subtitle: String? = null)
```

**Fixed sizes**: Poster 180×270dp; Landscape 320×180dp; Rail vertical gap 16dp; card H spacing 24dp; section banner 88dp.

**Image loading:** **Coil** with crossfade; memory/disk cache default. **Preload policy:** hero + first **3 rails** on screen enter; subsequent rails preload **on focus‑enter**. Threshold constant: `UiTokens.preloadRailCount = 3`.

---

## 12. Player — Overlays, Remote Keys & Interactions

### 12.1 PlayerController API (fixed)

*Package `com.supernova.domain.player` — implemented in `:app`*

```kotlin
enum class MediaType { LIVE, MOVIE, EPISODE }

data class MediaRef(val type: MediaType, val id: String)

data class TrackInfo(val id: String, val label: String, val lang: String?, val selected: Boolean)

interface PlayerController {
  fun play(ref: MediaRef)
  fun pause()
  fun seekBy(millis: Long)
  fun audioTracks(): List<TrackInfo>
  fun subtitleTracks(): List<TrackInfo>
  fun selectAudioTrack(id: String)
  fun selectSubtitleTrack(id: String?) // null to disable
}
```

* Overlay behavior fixed in §5.5; track selection persists for session by type.

### 12.2 Remote key mapping (fixed)

Global mapping (TV remote):

* **D‑pad**: nav focus; OK = click.
* **Back**: pop; in player, exit overlay → exit player.
* **Play/Pause**: toggle via **MediaSession** transport controls.
* **Fast‑forward/Rewind**: seekBy ±10 s.
* **Info**: show EPG overlay in Live; show details in VOD.
* **Menu** (⋮): open track panel (Audio/Subs) in player.
* **Channel ± / Numbers**: Live only — tune next/prev or by number (if channel numbers available).

### 12.3 Live EPG Overlay

* INFO shows Now/Next; missing EPG falls back to channel number + title.
* Channel surf & day shift as in §5.6.

### 12.4 Skip Intro/Outro

* Out of scope for MVP.

---

## 13. Theming — Material 3, Color & Typography

Theming — Material 3, Color & Typography

### 13.1 Material Design

* Use **Material 3** components and elevation system (Compose Material3).

### 13.2 Color Palette (dark)

* `primary` **#9E7BFF**
* `onPrimary` #0B051A
* `secondary` **#03DAC6**
* `onSecondary` #001F1C
* `surface` **#121212**
* `onSurface` #E6E1E5
* `surfaceVariant` #1E1E1E
* `outline` #3A3A3A

### 13.3 Typography

* Font: **Roboto**.
* Sizes (sp): Display 48, Headline 32, Title 22, Body 16, Label 14.

---

## 14. Performance & Telemetry

* Lazy image loading with TMDB size hints.
* Preload hero + first **3 rails**; others on focus.
* Log sync stats, retries, materialization timings; no PII.

---

## 15. Security

* Android Keystore (AES‑GCM). Keys per install.
* Interfaces (fixed):

```kotlin
interface SecureStorage { fun put(key: String, value: ByteArray); fun get(key: String): ByteArray?; fun remove(key: String) }
```

* **Credentials:** XC creds only in `SecureStorage`.
* **TMDB token:** default from `BuildConfig.TMDB_TOKEN`; if `SecureStorage["tmdb.token"]` exists, **prefer it** (runtime override). Never log.

### 15.1 Token provisioning (dev/prod)

* **Dev:** `BuildConfig.TMDB_TOKEN` injected from **local.properties** via Gradle and available in debug builds.
* **Prod:** `BuildConfig.TMDB_TOKEN` injected by **CI secrets** (environment → Gradle property → `buildConfigField`). Token value is **not** checked into VCS.
* **Rotation:** ship a build with a new token **or** push a runtime override into `SecureStorage["tmdb.token"]`.
* **Privacy:** Token is never logged, never sent to analytics; only attached to TMDB HTTPS requests.

---

## 16. Build, CI & Quality Gates (overview)

* Single CI entry: `ciAllTests` (unit + instrumented as configured).
* TestGate writes per‑audit reports **and** a root summary JSON: **`<root>/build/TestGate.json`** (authoritative for devs).
* TestGate audits: Structure, Compile, Detekt, Lint, Stack, Harness, SQL/FTS, Fixtures, Tests, Coverage, InstrumentedResults, InstrumentedScopeBan, Nano, **I18n**.

---

## 17. Legal, Privacy & About

* **TMDB attribution:** About screen: “This product uses the TMDB API but is not endorsed by TMDB.” Show TMDB logo per guidelines.
* **First‑run privacy notice:** modal explaining local telemetry only; link from Config → About.
* **Config → About:** button to re‑open attributions & privacy.

---

## 18. Accessibility (MVP baseline)

* All focusables have non‑empty `contentDescription`.
* High‑contrast focus ring tokens.
* TalkBack announces card title + ordinal in rail.
* (Deferred) Advanced semantics for grid/collection roles.

---

## 19. Developer Experience

* **Fakes:** `XcClientFake`, `TmdbClientFake` seeded from fixtures.
* **Fixtures directory layout:**

```
src/test/resources/fixtures/
  xc/
    live_streams_min.json   # array of { stream_id, name, category_id, num? }
    vod_streams_min.json    # array of { stream_id, name, category_id }
    series_list_min.json    # array of { series_id, name }
    series_info_min.json    # { series_id, seasons:[{season_number, episodes:[{episode_id,title,episode_num}]}] }
    epg_min.xml             # minimal XMLTV sample
  tmdb/
    configuration_min.json  # { images:{ base_url, poster_sizes[], backdrop_sizes[] } }
    trending_movie_day_min.json
    trending_tv_day_min.json
    movie_details_min.json
    tv_details_min.json
```

* **Dev seed command (debug‑only):** long‑press OK on Splash (5s) opens Dev Menu: run XC fixture sync, run TMDB refresh, clear DB, toggle fakes, show TestGate JSON path.
* **Logging:** thin wrapper over **android.util.Log**; verbose in debug; errors only in release.
* **StrictMode:** enabled in debug (disk & network on main thread detections, leakedClosable); disabled in release.

---

## 20. Manifest & Assets

* Manifest: `uses-feature android.software.leanback="true" required="true"`; launcher category `LEANBACK_LAUNCHER`.
* Assets: TV banner 320×180, round icon, monochrome.
* Placeholders: posters/backdrops; profile pictures from **DiceBear bottts** seeded by profile id (cached). Glossary
* **Rail**: horizontal carousel of content.
* **Hero**: large featured item at top of section.
* **Materialization**: precomputing rail membership & order into `RailEntry`.
* **FTS**: Full‑Text Search (SQLite FTS4).

## 21. Internationalization & Localization (I18n)

* **Language policy:** App default strings in **English**; **all strings translatable**. TMDB `language` parameter uses device locale; fallback to `en` when fields are empty.
* **Compose usage:** UI text **must** come from `stringResource(R.string.*)` (or a project wrapper). No hardcoded UI literals in composables.
* **Timezones:** EPG times are stored as epoch and rendered in the **device timezone**.

### 21.1 Enforcement

* **Android Lint:**

    * `HardcodedText` = **error**
    * `SetTextI18n` = **error**
* **TestGate audit (`auditsI18n`)**

    * Scope: UI sources in `:app` (and any UI modules).
    * Flags: `Text("…")`, `text = "…"`, and string interpolation passed to text params.
    * Requires: `stringResource(…)` for user‑visible text.
    * Exceptions (wildcards via properties): tests, debug‑only dev menu, logging, explicit brand names/placeholders.
    * Tolerance: **0** in `:app`.
