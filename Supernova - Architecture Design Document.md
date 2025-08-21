# Supernova IPTV Architecture Design Document

## 1. Project Overview

* **Project Type**: IPTV Client MVP
* **Goal**: Build a live + VOD IPTV experience with D-pad UX for Android TV
* **Scope**: Playback, EPG, resume, search, profiles, sync, recommendations, error handling

---

## 2. MVP Scope and Constraints

### 2.1 Functional MVP Scope

* Playback: pause/resume, seek (VOD only)
* EPG: current/next, mini-guide overlay
* Resume: VOD/series only, resume threshold 5%–95%
* Favorites: toggle, rail, sorted by recency
* Search: text/voice, multi-source, ranked
* Profiles: create/switch, history tracking
* Parental controls: global PIN, rating filter, retry lock
* Recommendations: based on watch history and TMDB metadata
* Error handling: retry, banner, offline UI

### 2.2 Explicitly Excluded Features

* Catch-up TV
* Recording (DVR, series record)
* Full grid EPG
* Cloud resume
* Social sharing
* Cross-device sync

### 2.3 Non-functional Constraints

* **Performance**: Startup ≤2s, navigation latency ≤300ms
* **Minimum Test Coverage**: 70%
* **Layering Required**: true
* **Compose Required**: true

---

## 3. Platform and UI Baseline

* **Platform**: Android TV
* **Device Constraints**: low RAM (512MB), slow CPUs, non-touch
* **UI Framework**: Jetpack Compose
* **D-pad Navigation**: enabled
* **Focusable Components**: FocusableCard, FocusableButton, FocusableImageCard

### 3.1 Accessibility

* High contrast: system-default
* Text scaling: system-default
* TalkBack: Compose-compatible
* Remappable buttons: not supported

### 3.2 Repository Modules (Gradle subprojects)

**Modules**
* **:app** — Android TV app (Compose UI, navigation, ViewModels, screens)
* **:domain** — Pure Kotlin domain (model, mapper, usecase)
* **:data** — Android library (Room entities/FTS4, DAOs, migrations, materialized rails, popularity cache)
* **:network** — Kotlin library (XC + TMDB clients/DTOs with Retrofit/Moshi)
* **:sync** — Android library (repositories, ingestion for XC/TMDB/XMLTV, workers, cursors)
* **:security** — Android library (Keystore‑backed secure storage API)
* **:testing-harness** — Kotlin library with JVM test helpers (no Android deps)
* **:testing-instrumented** — Android library with instrumented test helpers (device‑only kits)

**Dependency shape**
```
:app ─► :domain, :sync, :security
:sync ─► :network, :data, :domain
:data ─► :domain
:network ─► (independent) or :domain (for mappers)
:testing-harness ─► (testImplementation only)
:testing-instrumented ─► (androidTestImplementation only)
```

**Notes**
* Keep ```:network``` as JVM unless Android APIs are required.
* All **FTS** uses **Room FTS4** (Room doesn’t support FTS5). See §7.1.
* UI rails **query by ```position```** from ```RailEntry``` (precomputed). See §7.6.
---

## 4. Navigation and Screen Flows

### 4.1 Navigation Flow
@todo fix. Post login  -> Profile selection -> home. Home is personalized
* Launch → **Splash**

    * **Splash checks**: network connectivity and credential validity (quick auth ping)
    * **Outcomes**:

        * No network → show **Offline Error (blocking)** with Retry
        * Missing/invalid credentials → go to **Config**
        * Credentials valid → **ProfileSelection**
* Post-login → **Home**
* Single-activity, Compose-based navigation
* **NavFrame** persists across screens except **VideoPlayer**

### 4.2 Focus & D-Pad Behavior

* All screens use D-pad navigation
* Content cards scale on focus and show metadata
* PIN Prompt is modal, not part of nav graph
* **Note**: Home, Movies, and Series share the same layout (see §§5.5, 5.13, 5.14); hero rules §6.10; rail ordering §6.11.

---

## 5. Wireframes

### 5.1 Splash Screen

```
Splash Screen
-------------
[ Supernova Logo ]
[ Loading Spinner ] ← uses: LoadingSpinner
[ Network + Auth Checks ]
Outcomes:
- If NO NETWORK → open "Offline Error" (blocking) with [Retry]
- If LOGIN FAILS or NO CREDS → navigate to Config
- Else → navigate to Profile Selection
```

### 5.2 Config Screen

```
Config Screen
-------------
[ Input: Host     ] [ Port ]
[ Username        ] [ Password ]
[   [ Test & Save ]  ] ← uses: FocusableButton
[ Snackbar/Error Banner if failed ]
```

### 5.3 Offline Error Screen (Blocking)

```
Offline Error Screen (Blocking)
-------------------------------
[ ⚠ No Internet Connection ]
[ Retry ] ← uses: FocusableButton
```

### 5.4 Profile Selection

```
Profile Selection
-----------------
[ Profile Avatar 1 ]  [ + New Profile ]  [ Profile Avatar 2 ]  ... ← uses: FocusableCard
Layout = angled perspective, carousel-style
Focus Behavior:
- Focused card: scaled (1.15x), centered, glow + Z-lift
- Non-focused cards: smaller, blurred or desaturated
- D-pad left/right: scrolls carousel with easing
- On focus: backdrop may blur or darken
```

### 5.5 Home Screen

```
Home Screen
-----------
[ Hero Spotlight ]  ← large backdrop, title, 2-line overview
  • Source order: Personalized Recommendation → Editorial → None
  • De-dup: exclude anything visible in Continue Watching or Everyone's Watching (Trending)
  • CTA: Play / More Info (Resume if applicable)

[ Continue Watching ] → [Card1] [Card2] ... ← uses: ContentRail + MediaCard
[ Favorites         ] → [Card1] [Card2] ... ← uses: ContentRail + MediaCard
[ Recommendations   ] → [Card1] [Card2] ... ← uses: ContentRail + MediaCard
[ Everyone's Watching ] → [Card1] [Card2] ... ← uses: ContentRail + MediaCard (TMDB trending ∩ catalog — intersection)
```

### 5.6 Search Results

```
Search Results
--------------
[ Search Bar ] ← uses: SearchBar
[ Top Results Grid ] ← uses: MediaCard or FocusableCard
[ EPG Hits (if any) ]
```

### 5.7 Movie Detail

```
Movie Detail
------------
[ BACKDROP IMAGE ]
[ Poster | Title, Overview, ★ Rating ]
[ ▶ Play ] [+ Favorite] [ℹ More Info] ← uses: FocusableButton
[ Cast Grid (photos + names) ] ← uses: FocusableImageCard
```

### 5.8 Series Detail (Merged)

```
Series Detail (Merged)
----------------------
[ Series Poster | Title | Overview | Vote ]
[ ▶ Play ] [+ Favorite] [ℹ More Info] ← uses: FocusableButton
[ Season Carousel: S1 S2 S3 ... ] ← uses: FocusableImageCard
[ Selected Season: overview, vote ]
[ Episode Rail: Thumbnail, Ep#, Title, Score ] ← uses: FocusableCard or MediaCard
```

### 5.9 Video Player

```
Video Player
------------
[ Fullscreen Video ]
[ (Optional) Player Overlay on D-pad OK ]
[ Resume / Start Over Dialog if applicable ] ← uses: FocusableButton
```

### 5.10 EPG Overlay

```
EPG Overlay
-----------
[ Now/Next info ]  ← current channel
[ Scrollable list of 5 visible channels ] ← uses: ContentRail + MediaCard
[ Channel Up/Down (D-pad), Jump via number ]
```

### 5.11 PIN Prompt (Modal)

```
PIN Prompt (Modal)
------------------
[ 🔒 Enter PIN ]
[ [0] [1] [2] ... [9] ] ← uses: FocusableButton
[ Attempts Left: 2 ]
[ ❌ Cancel ] ← uses: FocusableButton
```

### 5.12 Settings Screen

```
Settings Screen
---------------
Privacy & Data:
- [ ] GDPR Consent ← uses: FocusableButton
- [ ] Parental Lock Setup ← uses: FocusableButton

About:
- Version: x.y.z
- [Logos], [Licenses] ← uses: FocusableImageCard

Language:
- [English ▼] ← uses: FocusableButton or Dropdown (to define)
```

### 5.13 Movies Screen (Shared Layout)

```
Movies Screen (shared with Home layout)
--------------------------------------
Layout = identical to Home (§5.5):
[ Hero Spotlight ]  ← uses: §6.10 hero rules (movies-only)
[ Continue Watching ]  ← movies-only items
[ Favorites ]          ← movies-only
[ Recommendations ]    ← movies-only (per profile)
[ Everyone's Watching ]← Trending Movies (TMDB ∩ catalog — intersection)
(Additional rails: movies_category_<genreId> as needed)
Notes:
- Above-the-fold rails sorted per §6.11 (popularity).
- De-dup vs Hero and Trending per §6.10.
```

### 5.14 Series Screen (Shared Layout)

```
Series Screen (shared with Home layout)
--------------------------------------
Layout = identical to Home (§5.5):
[ Hero Spotlight ]  ← uses: §6.10 hero rules (series-only)
[ Continue Watching ]  ← series-episodes only
[ Favorites ]          ← series-only
[ Recommendations ]    ← series-only (per profile)
[ Everyone's Watching ]← Trending Series (TMDB ∩ catalog — intersection)
(Additional rails: series_category_<genreId> as needed)
Notes:
- Above-the-fold rails sorted per §6.11 (popularity).
- De-dup vs Hero and Trending per §6.10.
```

---

## 6. Functional Behavior

### 6.1 Playback

* **Pause/Resume**: Supported for all stream types where provider allows.
* **Seek**: Supported for VOD only; seek disabled for live unless provider supports archive.
* **Auto-next episode**: Enabled for series; triggered by episode track metadata.
* **Resume/Restart UI**: Card-style overlay with **Resume / Start Over**.
* **Player overlay**: D-pad **OK** toggles overlay (playback controls, **quality selector**).
* **FF/RW**: Not implemented; no variable speed support.
* **Skip intro**: Not supported (requires stream metadata).
* **PiP**: Excluded from MVP.

### 6.2 EPG and Mini-guide

* 5-channel overlay
* Trigger: remote or long press
* Scroll with D-pad up/down
* Channel jump: numeric input + Enter
* Current/next info supported
* Full grid excluded
* Reminders supported (Room + WorkManager)

### 6.3 Resume and Continue Watching

* Types: movies, series episodes, VOD
* Max 15 items
* Removal threshold: ≥95% watched
* Resume threshold: ≥5%
* Sorted by last watched
* Rail: ContentRail
* Cross-device resume excluded

### 6.4 Favorites

* Toggle via content icon
* Rail: max 15 items, sorted by recency, shown on Home
* Remove via menu/long-press
* Backend sync enabled

### 6.5 Search

* **FTS**: enabled

**Indexing Notes**

* **Catalog (Stream)**: live channels and movies (type = MOVIE). TMDB-enriched alt names & keywords.

* **Episodes (Episode)**: episode titles, season/episode numbers, TMDB-enriched keywords.

* **EPG (Program)**: program titles indexed with time windows (TTL-limited).

* **Query sources**: `catalog`, `episodes`, `epg`

* **Voice search**: fallback to system intent

* **Ranking**: exact > fuzzy > boosted

* **History**: excluded

* **Suggestions**: none

* **Empty state**: "No results found"

* **Max results per query**: 30

### 6.6 Profiles

* Create only (username)
* No delete/edit/reorder
* Unlimited (UI-constrained)
* History used for resume/recommendations
* Persist across hosts

### 6.7 Parental Controls

* PIN prompt on restricted content
* PIN: 4-digit, 3 attempts, 5 min lockout
* Change UI in Settings
* Rating enforcement via TMDB (fallback: locked)

### 6.8 Recommendations

* **Enabled**: true
* **Trigger**: watch history + TMDB metadata present
* **Worker**: weekly (**RecWorker**)
* **Scope**: VOD and series episodes only

**Data Sources**

* `WatchHistory`
* `TmdbMetadata`

**Scoring Formula**

* `score = overlap_count × recency_weight × percent_watched_weight`

**Recency Weight (days → weight)**

* 0 → 1.0
* 1 → 0.8
* 2 → 0.6
* 3–6 → 0.5
* 7–13 → 0.3
* 14+ → 0.2

**Percent Watched Weight**

* ≥95 → 0.2
* 70–94 → 0.7
* 40–69 → 1.0
* 5–39 → 0.5
* <5 → 0.0

**Output Table**

* `recommendation (profile_id, content_id, score, source_tag, created_at)`

**Rail**

* component: `ContentRail`
* title: **Recommended For You**
* sort: `score DESC`

### 6.9 Error Handling

* Playback: **404/buffering ⇒ toast**
* Sync: **retry with exponential backoff** (max 3)
* Offline mode: checked at Splash; show **Offline Error** with Retry; browsing allowed offline, playback disabled
* Error UI: **toast** for minor errors; **banner** for global failures

### 6.10 Hero Strategy

**Goal**: Avoid duplicating existing rails (**Continue Watching**, **Trending**) while surfacing one high-value spotlight.

**Common guards**

* Must be in catalog, pass parental rules, and have valid backdrop.
* Exclude items shown in visible portions of **Continue Watching** and **Trending** rails (top 10 items each).
* 7‑day per‑profile de‑duplication for the same hero (except CW case if ever enabled later).

**Home hero (Spotlight)**

1. **Personalized Recommendation** (top score not in excluded sets).
2. **Editorial Spotlight** (optional, static).
3. **None** (hide hero).

**Movies hero**

* **Personalized Movie Recommendation** only (exclude items in **Trending Movies** rail and first rows of category rails). If none → **Editorial** → **None**.

**Series hero**

* **Personalized Series Recommendation** only (exclude items in **Trending Series** rail and first rows of category rails). If none → **Editorial** → **None**.

**Implementation notes**

* `HeroResolver(profile)` returns `HeroSelection(content_id, reason, ctaPrimary, ctaSecondary)`.

* Maintain `hero_impression(profile_id, content_id, shown_at)` for 7‑day de‑dup.

---

### 6.11 Rail Ordering Policy

**Goal**: Prioritize “above-the-fold” rails with smart ordering while avoiding heavy network calls.

**Scope**

* Apply reordering to the first \~2 screenfuls on TV (≈ first **6 rails**). Subsequent rails use provider/default order.

**Popularity score (local, cacheable weekly)**

```
popularity = 0.6 * recency_weighted_watch_count_14d
           + 0.25 * favorites_count_norm
           + 0.15 * completion_rate_norm
```

* Use the same **recency weights** as Recommendations (0:1.0, 1:0.8, 2:0.6, 3–6:0.5, 7–13:0.3, 14+:0.2).
* Normalize counts per-rail population (min–max or log1p / max).
* Cold-start boost: if in **cached TMDB Trending Top‑100 ∩ catalog** (refreshed weekly), add a small bonus.

**Where it applies**

* **Home**:

    * **Continue Watching**: time-ordered by design (no popularity sort).
    * **Recommendations** + category rails: use `popularity` for the first page (e.g., top 20 items).
    * **Trending** rail: already sorted by the cached trending list.
* **Movies / Series pages**: each category rail uses `popularity` for its first page; remainder default order.

**De-dup & diversity**

* Penalize items already surfaced as **Hero** or as the **#1 item** in a previous visible rail: `score *= 0.8` (once).
* Optional cap: **≤2 titles per franchise** in the first 10 positions of a rail.

**Tie-breakers**

* Newer **release date** first → **Title A–Z**.

**Offline & cost**

* Inputs are local (**WatchHistory**, **Favorites**, completion) + a **weekly cached** Trending list. No per-scroll TMDB fetching.

**Caching & recomputation**

* Persist per-item scores in **PopularityCache** (see §7.3) to avoid recomputing on every open.
* **Recompute triggers** (incremental): when watch/favorite/completion events occur for a title, recompute *only that title* for the affected profile/rail.
* **Full refresh**: when TMDB Trending cache is refreshed (weekly) or when catalog deltas arrive, recompute scores for the first‑page slice of each affected rail.
* **TTL**: 7 days; stale entries lazily refreshed on access.

## 7. Data Architecture

### 7.1 FTS Rules

* Enabled on Stream, Episode, Program
* Queries must use FTS tables
* No direct SQL inserts/updates

### 7.2 Secure Storage

* Backend: SecureDataStore
* Secured: provider credentials, parental PIN

### 7.3 Room Entities
@todo add fields and indexes for the rest
* Stream
* Program
* Episode
* Category
* Profile
* Favorite
* WatchHistory
* Recommendation
* TmdbMetadata
* **PopularityCache** { content\_id, profile\_id (nullable), score, computed\_at, flags }
* **RailEntry** { rail\_id, profile\_id (nullable), position, content\_id, updated\_at }
* **HeroImpression** { profile\_id, content\_id, shown\_at }

### 7.4 DAOs

* StreamDao
* ProgramDao
* EpisodeDao
* CategoryDao
* ProfileDao
* FavoriteDao
* WatchHistoryDao
* RecommendationDao
* TmdbMetadataDao
* PopularityCacheDao
* RailEntryDao
* SearchDao
* HeroImpressionDao

### 7.5 Notes

* Seasons modeled in Episode.season\_num
* Content grouped by stream\_type

---

### 7.6 Rail Materialization & Query (Simple)

**Principle**: keep SQL trivial; do all ranking/dedup in workers.
**Definition**: `RailEntry` is a **materialized view** — the precomputed contents of each rail (first‑page slice) stored with a stable `position`.

**Materialization (in workers)**

* Builders (RecWorker, Trending refresh, Catalog delta handler) compute the first‑page slice for each rail and write to `RailEntry` with stable `position` (1..N).
* Triggers: on RecWorker run, weekly TMDB refresh, favorites toggle, significant watch/completion events (for the affected content only), or provider switch.

**Query (UI)**

* Fetch by `rail_id` (and `profile_id` when personalized) ordered by `position`.
* Example (IDs only):

  ```sql
  SELECT content_id
  FROM RailEntry
  WHERE rail_id = :railId
    AND (profile_id = :profileId OR profile_id IS NULL)
  ORDER BY position
  LIMIT :limit OFFSET :offset;
  ```
* Example (with join to appropriate content table, e.g., Stream or Episode):

  ```sql
  SELECT c.*
  FROM RailEntry r
  JOIN Stream AS c ON c.id = r.content_id
  WHERE r.rail_id = :railId
    AND (r.profile_id = :profileId OR r.profile_id IS NULL)
  ORDER BY r.position
  LIMIT :limit OFFSET :offset;
  ```
* Continue Watching remains direct from `WatchHistory` (time-ordered) with thresholds; no RailEntry needed.

**Rail IDs (suggested)**

* `home_recommendations`, `home_trending`, `home_favorites`
* `movies_trending`, `series_trending`
* `movies_category_<genreId>`, `series_category_<genreId>`

**Notes**

* De-dup vs. hero/other rails happens during materialization (worker) to keep queries simple.
* Rebuilds are **idempotent**: writers replace the `RailEntry` set for a rail in a single transaction.

## 8. Sync and Ingestion

### 8.1 Catalog & EPG Sync

* XC API ingest
* Parallel sync enabled
* Triggers: app launch, manual refresh
* Atomic FTS refresh

### 8.2 TMDB Enrichment

* Preload ≈7,000 items initially; weekly top-up 200
* Storage: Room with TTL ≥7 days
* Fields: genres, keywords, type
* Lookup key: title + year
* **On refresh**: after the weekly TMDB Trending cache/top‑up completes, enqueue **PopularityCache** recomputation for the first‑page slice of impacted rails (Home, Movies, Series).

### 8.3 Matching Strategy

* Fields: title, category
* Threshold: 0.8
* Fallback: partial match

### 8.4 Rate Limit & Retry

* Manual retry with exponential backoff (max 3)
* Errors shown via toast/banner

### 8.5 File Parsing

* **JSON (XC API)**: Moshi `JsonReader` (streaming). OkHttp + Moshi handle UTF‑8 per JSON spec automatically; **no manual charset fallback**.
* **XMLTV**: `XmlPullParser` (streaming, forward-only). Use `setInput(InputStream, null)` to let the parser **auto‑detect encoding** from the XML prolog.

### 8.6 Reminders

* Backend: Room + WorkManager
* Scope: program alarms

### 8.7 Sync Constraints

* Partial sync disallowed
* Catalog sync required at launch

### 8.8 Provider Change Handling

* **Domain comparison**: compare the provider **host domain** (ignore port).

    * **Same domain**: assume same provider → **keep current synced data** (catalog/EPG), update stored credentials, and schedule an **incremental sync**.
    * **Different domain**: treat as new provider → **wipe all synced provider data** (Stream, Program, Recommendation, WatchHistory entries tied to provider as applicable, caches), then **force a fresh initial sync**.
* Performed on **Save** in Config and also validated on **app startup** if credentials changed.
* FTS refresh still uses atomic shadow-table swap.

### 8.9 XC API Streaming Ingestion Policy

**Principle**: never load full responses in memory; stream and batch‑commit.

**Transport**

* OkHttp with `ResponseBody.source()` / Okio `BufferedSource`; prefer server gzip/deflate; set sane read timeouts.

**Parsing**

* **JSON endpoints** (`get_live_streams`, `get_series`, `player_api` variants): Moshi **`JsonReader`** in streaming mode; iterate array elements and map to entities one by one.
* **EPG** (`get_epg`): request **windowed ranges** (by channel/time) when available; parse streaming and write in batches.
* **XMLTV**: `XmlPullParser` (already streaming).

**Batching & Transactions**

* Accumulate **200–500** items per batch; write inside a single Room transaction per batch.
* Use **shadow tables** during catalog/EPG rebuild; **atomic swap** on completion to avoid partial reads.

**Idempotency & Cursors**

* Upsert by stable IDs; maintain a **sync cursor** (last item/time) per endpoint to resume after failures.
* Record counts and last offsets in a `SyncState` table.

**Backpressure & Memory**

* Keep parser buffers small (target < **1–2 MB** RAM usage during ingest).
* Yield cooperatively in workers if processing > N ms to avoid ANRs (WorkManager background).

**Error Handling**

* Per-batch retry with exponential backoff (max 3) and **resume** from last committed cursor.
* On final failure, show banner and keep last known good dataset.

**Recomputation hooks**

* After successful ingest: refresh FTS (shadow swap), enqueue **PopularityCache** and **RailEntry** rebuilds for affected rails.

---

## 9. API Contracts (XC + TMDB)

> Minimal, implementation-ready contracts. Parsing is **streaming-only** (Moshi `JsonReader` for JSON; `XmlPullParser` for XMLTV). Batched writes with shadow-table swaps.  
> **Explicitly out of scope:** `/get.php` (M3U).

---

### 9.1 XC API (Provider Adapter Contract) — **corrected to match XC OAI**

**Base URL**  
`http(s)://<host>[:port]/`  
Provider identity is the **domain**: same domain ⇒ keep data & update creds; different domain ⇒ wipe catalog and force a fresh initial sync.

**Auth & Splash quick-auth**  
Call `/player_api.php` **without** `action` using `username` & `password`. Read only:
```json
{ "auth": { "status": "OK | INVALID | ERROR" } }
```
Route: `OK`→Profile/Sync, `INVALID`/missing→Config, network/5xx→Offline error.

**Endpoints the app actually consumes**

- `GET /player_api.php?action=get_live_streams&username=…&password=…`  
  **Stream list** (implicitly live). Read only:  
  `stream_id` (string), `name` (string), `category_id` (string), `num` (int, optional).

- `GET /player_api.php?action=get_vod_streams&username=…&password=…`  
  **VOD list.** Read only:  
  `stream_id` (string), `name` (string), `category_id` (string).  
  *(Any container/extension fields are ignored by the client.)*

- `GET /player_api.php?action=get_series&username=…&password=…`  
  **Series directory.** Read only:  
  `series_id` (string), `name` (string). *(No episodes here.)*

- `GET /player_api.php?action=get_series_info&series_id={id}&username=…&password=…`  
  **Per-series seasons & episodes.** Read only:  
  `seasons[].season_number` (int) and `seasons[].episodes[]` with  
  `episode_id` (string), `title` (string), `episode_num` (int).

- `GET /xmltv.php?username=…&password=…[&start=iso][&end=iso]`  
  **EPG (XMLTV).** Parsed with a pull parser; persist programs in batches.  
  Read only per item: `title`, `start`, `end`, `channel_id`.

**Other XC endpoints** (present in XC but **not used** by the app)
- `/player_api.php?action=get_*_categories`, `get_vod_info`, `get_short_epg`, `get_simple_data_table` — **ignored**.
- `/get.php` (M3U) — **not supported**.

**Playback transports** (not parsed, just handed to player)
- `GET /live/{username}/{password}/{stream_id}.ts` — live.
- `GET /movie/{username}/{password}/{stream_id}.{ext}` — VOD.
- `GET /series/{username}/{password}/{stream_id}.{ext}` — episode.

**Client behavior contracts**
- **Streaming ingestion** only; never load full payloads to memory.
- **Batch size** ~200–500 per transaction; shadow-swap on table rebuilds.
- **Retry**: `429/5xx/IO` with exponential backoff (≤3); `401/403` marks creds invalid.
- **Resume**: maintain `SyncState(endpoint, cursor|offset, updated_at)`.

---

### 9.2 TMDB API (Minimal External Contract)

**Base URL**: `https://api.themoviedb.org/3` — **Auth**: Bearer token (`Authorization: Bearer …`).  
**Language**: `language=<device-locale>`, fallback to `en` if fields are empty.  
**Images**: Bootstrap with `/configuration` (cache ≥7d) to get `base_url` & sizes; compose `base_url + size + file_path`.

**Endpoints used**
- `GET /configuration` — image base & sizes.
- **Search & details**
    - `GET /search/movie?query=<title>&year=<yyyy>&language=…`
    - `GET /search/tv?query=<title>&first_air_date_year=<yyyy>&language=…`
    - `GET /movie/{id}?append_to_response=keywords`
    - `GET /tv/{id}?append_to_response=keywords`  
      **Persisted fields only:** `id`, `genres[]`, `keywords[]`, `title|name`, `release_date|first_air_date`, local `type` (`movie|tv`).
- **Trending** (for weekly caches / rails seed)
    - `GET /trending/movie/day`, `GET /trending/tv/day`.

**Cadence & pagination**
- Initial preload to satisfy first-page rails; weekly top-ups; TTL ≥7d.
- Page only as needed for caches; UI never calls TMDB directly (rails are materialized).

---

### 9.3 Data Mapping (XC/TMDB → Room) — **aligned with corrected XC**

- **XC Live → `Stream`**:  
  `stream_id`→`id`, `name`→`title`, `category_id`, `num`→`number`, `is_live = true`.

- **XC VOD → `VodItem` (or unified `Stream` if you model both)**:  
  `stream_id`→`id`, `name`→`title`, `category_id`, `is_live = false`.

- **XC Series + SeriesInfo → `Episode`** (flattened):  
  `series_id`→`Episode.series_id`, `season_number`→`Episode.season_num`,  
  `episode_id`→`Episode.id`, `title`, `episode_num`.

- **XC EPG → `Program`**:  
  `title`, `start`, `end`, `channel_id`→`epg_channel_id`.

- **TMDB → `TmdbMetadata`**:  
  `tmdb_id` (`id`), `genres`, `keywords`, `title|name`, `release_date|first_air_date`, `type`.

---

### 9.4 Post-Sync Rebuilds (rails & popularity)

After any successful XC/TMDB ingest:
1. Refresh FTS (shadow swap).
2. Recompute **PopularityCache** (first-page items).
3. Rebuild **RailEntry** per rail.

> **UI queries remain trivial:** select by `rail_id` (+ `profile_id` if applicable), `ORDER BY position`. No popularity sorting at query time.

---

## 10. Testing and Harness

### 10.1 Quality Gate

* Coverage ≥70%
* Forbidden: Robolectric.
* Fixture policy: 1 JSON per task (tiny, focused)
* Coroutines required for tests that use suspend/Flow/time
* **All unit tests must be annotated with `@UseTestHarness`** (or explicitly exempted with `@PureUnitTest` for truly synchronous POJO tests)
* **Unit tests:** JUnit 5. **Instrumented tests:** AndroidJUnit4 runner. **Single CI entrypoint:** `:app:ciAllTests` runs both (depends on `check` + `connectedDebugAndroidTest`).
* Frameworks: JUnit5, MockK, `kotlinx.coroutines.test` (`runTest`)
* Repository tests may mock DAOs; **DAO tests use real Room DB**.
* **Instrumentation scope**: only **FTS parity** and **Secure Storage** (see §10.10)

### 10.2 Conventions

* **Structure**: mirror production packages under `src/test` and `src/androidTest`.
* **Naming**: `given_When_Then` method style; one behavior per test.
* **Flows/Coroutines**: use `runTest {}` + `StandardTestDispatcher`; no `runBlocking`/`Thread.sleep`.
* **Determinism**: avoid time-based flakiness; use `FakeClock` and `advanceTimeBy`.
* **Fixtures**: one small JSON per test in module `test/resources`; keep under a few KB.
* **No UI instrumentation** for MVP; Compose/UI covered via ViewModel/use-case tests.
* **DB tests (JVM)**: file-backed Room DB with deterministic journal mode; WAL semantics (if needed) verified only in instrumented/nightly.
* **Test types (used project-wide)**: Pure Unit; Coroutine Unit; Network Client; Ingestion Pipeline; Ranking/Rec/Hero; Rail Materialization. See §10.5 for per‑layer cases.

### 10.3 Harness Components

* **BaseSyncTest** — Base class wiring the harness: installs `Dispatchers.Main`, builds a test DB, manages `MockWebServer` lifecycle. *Use to:* start end‑to‑end sync/worker tests with minimal boilerplate.

* **RoomTestDbBuilder** — Builds a **file‑backed** Room DB for JVM tests with deterministic journal mode (TRUNCATE) and an `assertJournalMode()` helper; optional bundled SQLite driver hook. *Use to:* avoid WAL flakiness and keep DAO tests stable.

* **JsonFixtureLoader** — Loads tiny JSON fixtures from `test/resources/`; returns `String` or Okio `BufferedSource` for streaming. *Use to:* feed parsers/clients without buffering whole payloads.

* **MockWebServerExtensions** — Helpers to enqueue **gzip**, **chunked**, delayed, or **early‑close** responses. *Use to:* simulate streaming, backpressure, and network hiccups.

* **SyncScenarioFactory** — High‑level builders for common flows (e.g., “TMDB trending → ingest → rail rebuild”). *Use to:* assert batch sizes, shadow‑table swap, and `RailEntry` materialization in one go.

* **DbAssertionHelpers** — Assertions for row counts, **shadow swap**, rail ordering by `position`, and simple FTS shape checks. *Use to:* make DB expectations readable and repeatable.

* **CoroutineTestUtils** — Thin wrappers for `runTest`, `advanceUntilIdle`, `advanceTimeBy`, and flow collection with time control. *Use to:* write deterministic coroutine/Flow tests.

* **UiStateTestHelpers** — Utilities to collect and assert **state sequences** from ViewModels (loading → content/error, etc.). *Use to:* validate UI state machines without touching Compose.

* **TestEntityFactory** — Builders for Room entities (Stream, Program, Episode, Recommendation, etc.) with sensible defaults. *Use to:* seed DB quickly in tests.

* **FtsTestKit (androidTest)** — Seeds a tiny **diacritics/case** dataset and provides query + ranking assertions on device. *Use to:* run **FTS parity** checks (the only device‑dependent FTS we keep).

* **SecureStorageTestKit (androidTest)** — Helpers to write/read creds & PIN, simulate **process death**, and (optionally) key rotation. *Use to:* validate Keystore‑backed storage behavior end‑to‑end.

* **PreviewFactories** — Sample domain models for previews; also handy as lightweight seeds in unit tests. *Use to:* avoid verbose test object setup.

### 10.4 Harness Enforcement (JUnit 5 + Detekt + TestGate)

**Annotations**

* `@UseTestHarness` (required default for unit tests)

  ```kotlin
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  @ExtendWith(
    MainDispatcherExtension::class
    // , MockKExtension::class // optional
  )
  @TestInstance(org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD)
  @Tag("harness")
  annotation class UseTestHarness
  ```
* `MainDispatcherExtension` (installs a test dispatcher)

  ```kotlin
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  class MainDispatcherExtension : BeforeEachCallback, AfterEachCallback {
    private val dispatcher = kotlinx.coroutines.test.StandardTestDispatcher()
    override fun beforeEach(context: ExtensionContext) {
      kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }
    override fun afterEach(context: ExtensionContext) {
      kotlinx.coroutines.Dispatchers.resetMain()
    }
  }
  ```
* `@PureUnitTest` (explicit exemption for sync-only tests)

  ```kotlin
  @Target(AnnotationTarget.CLASS)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class PureUnitTest
  ```

**Authoring pattern**

```kotlin
@UseTestHarness
class RepoTest {
  @Test fun loadsData() = kotlinx.coroutines.test.runTest {
    // arrange/act/assert
    advanceUntilIdle()
  }
}
```

**Detekt enforcement (treated as TestGate errors)**

* `TestGate:RequireHarnessAnnotationOnTests`
  Flags any class under `src/test` containing `@Test` methods **without** `@UseTestHarness` or `@PureUnitTest`.
* `TestGate:BanRunBlockingInTests`
  Forbids `kotlinx.coroutines.runBlocking` in test sources.
* `TestGate:BanThreadSleepInTests`
  Forbids `Thread.sleep(...)` in test sources.
* `TestGate:BanDirectDispatchersInProd`
  Forbids direct `Dispatchers.IO/Default` in prod sources (require injected dispatcher provider).

**TestGate wiring**

* Detekt XML is parsed by **DetektAudit**; the rule IDs above are mapped to **error** severity so **tolerance=0** will fail the build on violations.

**Notes**

* No auto-wrapping via JUnit `InvocationInterceptor`; tests should call `runTest {}` explicitly where needed (simpler, forward-compatible).

### 10.5 Per-layer Standard Cases (checklist)

* **ViewModels**: initial → loading → content/error; error surfacing (toast/banner); pagination / above‑the‑fold slicing.
* **UseCases/Repositories**: happy path; timeout/retry/backoff; cancellation respected; clock‑driven behavior via `FakeClock`.
* **Workers**: schedules & coalescing; batch sizes (200–500) and retries (max 3); hooks executed (shadow swap, PopularityCache, RailEntry rebuild).
* **DAOs (JVM)**: CRUD, joins, LIMIT/OFFSET paging; FTS query *shape* only (parity covered by instrumentation in §10.10).
* **Search**: ranking order (exact > fuzzy > boosted); source filtering; empty‑state handling.
* **Parental/PIN**: 3 attempts → 5‑min lockout; rating enforcement blocks restricted items.
* **Splash routing**: network down → Offline Error; invalid creds → Config; valid → ProfileSelection.

### 10.6 Critical Functionality Matrix (must-have tests)

Mark these tests with `@Tag("critical")`. TestGate may enforce presence later.

1. **Splash routing**: network down → Offline Error; invalid creds → Config; valid → ProfileSelection.
2. **Parental lock**: 3 wrong PINs → 5‑min lockout; rating filter blocks restricted items.
3. **Resume thresholds**: <5% = Start Over; 5–94% = Resume; ≥95% removes from CW.
4. **Recommendations**: scoring formula (recency × % watched × overlap) sorts as expected; weekly worker writes output.
5. **Hero selection**: excludes items in CW/Trending; 7‑day de‑dup respected.
6. **Rail ordering**: popularity formula applied to above‑the‑fold; tie‑breakers stable; diversity cap optional.
7. **Provider change**: same domain → keep data + incremental sync; different domain → wipe + initial sync.
8. **Streaming ingest**: large JSON parsed in batches (200–500); cursor resumes after simulated failure; memory stays bounded.
9. **Search ranking**: exact > fuzzy > boosted; sources filtered; empty state on none.
10. **Error policy**: playback 404/buffering → toast; global sync failure → banner.

### 10.7 Fixtures, Fakes, and Test Utils

* **Fixtures**: one JSON per task (policy already set). Keep tiny, focused.
* **Fakes**: `FakeClock`, `FakeDispatcherProvider`, `FakeNetworkMonitor`, `FakeTmdbClient`, `FakeXcClient` (streaming via Okio Buffer), `FakeRecommendationDao`.
* **Helpers**: `advanceUntilIdle()`, `advanceTimeBy(...)` wrappers; small builders in `TestEntityFactory`.
* **MockWebServerExtensions**: shortcuts for enqueuing gzip’d JSON streams.

### 10.8 CI Profiles (fast vs full)

* **Fast (PR)**: unit + harness tests; Detekt + Lint; branch coverage threshold.
* **Full (Nightly/Main)**: full test set + long‑running ingestion/worker scenarios.
* **Single entrypoint**: `:app:ciAllTests` depends on `check` + `connectedDebugAndroidTest`.

### 10.9 Module Checklist (definition of done)

* Tests use `@UseTestHarness` or `@PureUnitTest`.
* At least one `@Tag("critical")` test if the module affects a policy in §6.
* Adopt standard conventions from §10.2 (naming, structure, fixtures, banned patterns).

### 10.10 Instrumented Test Scope (FTS & Secure Storage)

**Why instrumented?** Only these two areas depend on real device behavior:

* **FTS parity**: tokenizer, diacritics/case folding, locale behavior, and ranking.
* **Secure storage**: Android Keystore/Jetpack Security behavior for encrypted prefs/DataStore.

**What runs (nightly or when relevant files change):**

1. **FtsParityInstrumentedTest**
   **Seed**: tiny dataset across `catalog`, `episodes`, `epg` (10–20 rows total).
   **Asserts**:

    * Accent/case-insensitive matches (e.g., "pokemon" → "Pokémon").
    * Multi-token queries and ordering (exact > fuzzy > boosted per §6.5).
    * Source filtering (only targeted table returns results when requested).
    * `LIMIT/OFFSET` behavior and stable tie-breakers.
    * Optional locale spot-check (en + one non-English locale).

**Goal runtime**: < 30s.

2. **SecureStorageInstrumentedTest**
   **Scope**: `SecureDataStore` usage for provider creds + parental PIN (see §7.2).
   **Asserts**:

    * Write → read round‑trip for creds/PIN.
    * Process‑death survival (kill & relaunch) still decrypts successfully.
    * Key rotation/migration path (if applicable) succeeds without data loss.
    * Graceful error when keystore unavailable (fallback policy respected).

**Goal runtime**: < 30s.

**What we do not instrument:** rails/materialization, ingestion/sync, recommendations/hero, DAO correctness—covered by JVM tests per §§10.5–10.9.

**CI policy**

* **PRs**: run only if paths change under `src/androidTest/**`, FTS schema/DAO, or secure storage code; otherwise skip.
* **Nightly/Main**: always run the two instrumented tests on a managed emulator (debug variant).
  Gradle: `connectedDebugAndroidTest` (or `connectedCheck`).

**Test locations**

* `app/src/androidTest/java/.../FtsParityInstrumentedTest.kt`
* `app/src/androidTest/java/.../SecureStorageInstrumentedTest.kt`

### 10.11 Test Templates (copy/paste)

> Minimal examples showing how to **use** the harness. Keep fixtures tiny; trim as needed.

**A) ViewModel (Coroutine Unit)**

```kotlin
@UseTestHarness
class HomeViewModelTest {
  @Test fun givenFreshProfile_whenInit_thenEmitsLoadingThenContent() = runTest {
    val repo = FakeHomeRepo(success = true)
    val vm = HomeViewModel(repo)
    val seen = mutableListOf<HomeState>()
    val job = launch { vm.state.collect { seen += it } }
    advanceUntilIdle()
    assertEquals(listOf(HomeState.Loading, isA<HomeState.Content>()), listOf(seen[0], seen[1]))
    job.cancel()
  }
}
```

**B) Network client (MockWebServer + streaming)**

```kotlin
@UseTestHarness
class XcClientTest {
  private val server = MockWebServer()
  @Before fun setUp() { server.start() }
  @After fun tearDown() { server.shutdown() }

  @Test fun streamsLargeArray_inBatches() = runTest {
    server.enqueue(gzipJsonResponse(fixture("xc/live_streams_1000.json")))
    val client = xcClient(baseUrl = server.url("/").toString())
    val items = client.getLiveStreams().take(3).toList() // pretend flow of batches
    assertTrue(items.isNotEmpty())
  }
}
```

**C) Ingestion worker (batches + shadow swap)**

```kotlin
@UseTestHarness
class IngestionWorkerTest {
  @Test fun ingestsInBatches_andSwapsAtomically() = runTest {
    seedServerWithTrending(200)
    val result = IngestionWorker(ctx = testContext()).doWork()
    assertEquals(ListenableWorker.Result.success(), result)
    db.assertShadowTableSwapped("Stream")
    db.assertRowCount("Stream", 200)
  }
}
```

**D) Ranking/Rec/Hero (de-dup determinism)**

```kotlin
@UseTestHarness
class HeroResolverTest {
  @Test fun excludesItemsFromCWAndTrending() = runTest {
    seedCW(1); seedTrending(2); seedRecs(1,2,3)
    val hero = HeroResolver(db).resolve(profile = P1)
    assertEquals(3, hero.contentId) // not 1 or 2
  }
}
```

**E) Rail materialization → simple query**

```kotlin
@UseTestHarness
class RailMaterializerTest {
  @Test fun writesFirstPage_orderedByPosition() = runTest {
    seedPopularity(/* ... */)
    RailMaterializer(db).build("home_recommendations", profile = P1)
    val ids = railDao.listIds("home_recommendations", P1, limit = 10, offset = 0)
    assertEquals(10, ids.size)
    assertTrue(ids.zipWithNext().all { (a, b) -> a != b })
  }
}
```

**F) DAO / FTS query shape (JVM)**

```kotlin
@UseTestHarness
class SearchDaoJvmTest {
  @Test fun ftsQuery_returnsExpectedIds_ordered() {
    seedFtsRows()
    val ids = searchDao.searchMovies("star war*", limit = 10, offset = 0)
    assertTrue(ids.isNotEmpty())
  }
}
```

**G) Instrumented — FTS parity**

```kotlin
@RunWith(AndroidJUnit4::class)
class FtsParityInstrumentedTest {
  @Test fun pokemonMatchesPokémon_andRanksExactBeforeFuzzy() {
    seedDeviceDb(listOf("Pokémon", "Pokemon Origins"))
    val results = searchDao.searchAll("pokemon")
    assertEquals("Pokémon", results.first().title)
  }
}
```

**H) Instrumented — Secure storage**

```kotlin
@RunWith(AndroidJUnit4::class)
class SecureStorageInstrumentedTest {
  @Test fun keystoreRoundTrip_survivesProcessDeath() {
    secureStore.saveCreds("user","pass")
    killAndRestartApp()
    assertEquals("user", secureStore.loadCreds().user)
  }
}
```
### 10.12 Instrumented Testing — `:testing-instrumented` toolbox & templates

#### 10.12.1 Device-only toolbox (`:testing-instrumented`)
**Purpose.** Centralize **instrumented** helpers that must run on device/emulator; avoid copy/paste across modules.

**Contents.**
- `FtsTestKit` — on-device Room **FTS4** parity checks (diacritics, case-folding, ranking).
- `SecureStorageTestKit` — Android **Keystore** round-trip + process-death sanity.

**How to use (in consumer modules).**
```kotlin
androidTestImplementation(project(":testing-instrumented"))
```
Write the actual tests under the consumer’s `src/androidTest/**` and import the kits.

**Policy.**
- Runner: `AndroidJUnit4` in consumers’ instrumented tests.
- **Allowed scope only:** FTS parity & Secure storage (matches §10.10).
- **Banned everywhere:** Robolectric, Espresso, Compose UI Test.
- Not gated by Testgate (informational convenience).

---

#### 10.12.2 Instrumented test templates (use the toolbox above)

**A) Instrumented — FTS parity (Room FTS4)**  
Goal: verify on-device FTS4 behavior (tokenization, diacritics/case, ranking) matches expectations.
```kotlin
// app/src/androidTest/.../FtsParityInstrumentedTest.kt
@RunWith(AndroidJUnit4::class)
class FtsParityInstrumentedTest {

    @Test fun fts4_parity_basicQueries() {
        val db = TestDbFactory.createInMemory(context) // your test DB builder
        FtsTestKit(db).seed(
            "canción" to 1,
            "Cancion" to 2,
            "cancion" to 3
        )
        FtsTestKit(db).assertMatch(
            query = "cancion",
            expectIdsInOrder = listOf(1, 2, 3) // diacritics/case-insensitive, ranking stable
        )
    }

    @Test fun fts4_parity_ranking_is_stable() {
        val db = TestDbFactory.createInMemory(context)
        FtsTestKit(db).seed(/* minimal corpus */)
        FtsTestKit(db).assertRankingStable("query")
    }
}
```
**Notes.** DB must use **FTS4** (Room doesn’t support FTS5). Keep corpus tiny; focus on tokenizer/diacritics/case/ranking.

**B) Instrumented — Secure storage (Keystore)**  
Goal: verify Keystore-backed storage round-trip and process-death resilience.
```kotlin
// security/src/androidTest/.../SecureStorageInstrumentedTest.kt
@RunWith(AndroidJUnit4::class)
class SecureStorageInstrumentedTest {

    @Test fun keystore_roundTrip_and_processDeath() {
        val kit = SecureStorageTestKit(context)
        kit.assertRoundTrip(key = "session", value = byteArrayOf(1,2,3,4))
        kit.assertSurvivesProcessDeath(key = "session")
    }
}
```
**Notes.** Keep values small; assert both retrieval and post-restart availability. No UI instrumentation; pure API-level checks only.
 

## 11. Reusable Components

### 11.1 UI Components

* **FocusableButton**: focusable action button
* **FocusableCard**: scalable TV card
* **FocusableImageCard**: image + metadata card
* **MediaCard**: thumbnail + label + progress
* **ContentRail**: horizontal container
* **SearchBar**: input for search

### 11.2 Data Components

* TestEntityFactory: test data builders *(testing; see §10.3)*
* DbAssertionHelpers: Room assertions *(testing; see §10.3)*
* CoroutineTestUtils: coroutine helpers *(testing; see §10.3)*
* Room Entity Pattern: standard annotations
* TmdbMetadata Normalizer (planned)

---

## 12. App Config and Policies

### 12.1 Provider Credentials

* Host, port, user, pass inputs
* Test connection inline
* Save enabled only if valid
* Errors: inline, snackbar, or banner
* **Startup checks**: Splash verifies **network** and attempts a **quick auth** using stored credentials.
* **Provider identity rule** (domain-based):

    * If the **host domain** is unchanged → treat as **same provider**; **retain** existing synced data and just update credentials; trigger **incremental sync**.
    * If the **host domain** changed → treat as **provider switch**; **clear** synced data and require a **full initial sync**.

### 12.2 Profiles

* Persisted across host switch
* No avatar/edit/delete

### 12.3 Parental Controls

* PIN: 4-digit, 3 retries, 5 min lockout
* Change UI in Settings
* Rating fallback: locked

### 12.4 EPG Overlay

* Trigger: remote/long press
* Display: 5 rows, scrollable
* Fallback: "No info"

### 12.5 Playback

* Controls: play/pause, seek (VOD)
* Adaptive quality
* Resume point VOD only
* Errors: toast

### 12.6 Search

* FTS enabled
* Max 30 results
* Empty state: "No results found"
* Voice via system intent
* Ranking: exact > fuzzy > boosted

### 12.7 Favorites

* Max 15 items
* Sorted by recency

### 12.8 Recommendations

* TMDB top-up: weekly (200), scheduled via nightly job
* Personalization: history + metadata
* Fallback: TMDB trending

### 12.9 Errors

* Minor: toast
* Global: banner
* Retry: backoff (max 3)
* Offline mode: Retry screen, playback disabled

### 12.10 Settings

* Language dropdown
* Clear cache
* Provider config
* Accessibility via system defaults

### 12.11 Sync

* EPG: every 24h
* Catalog sync: launch
* Manual refresh enabled
* Partial sync disabled

### 12.12 Telemetry

* Minimal consent
* Events: playback, search, favorites
* Crash reporting: Firebase
* No opt-out (MVP)

### 12.13 Localization

* UI language: system locale (fallback en)
* TMDB language: system locale fallback
