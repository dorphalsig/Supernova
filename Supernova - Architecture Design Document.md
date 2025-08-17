# Supernova IPTV Architecture Design Document (REORDERED)

## 0\_project\_overview

project\_type = "IPTV Client MVP"
goal = "Build a live + VOD IPTV experience with D-pad UX for Android TV"
scope = "Playback, EPG, resume, search, profiles, sync, recommendations, error handling"

## 1\_mvp\_scope\_and\_constraints

### Functional MVP Scope

includes = \[
"Playback: pause/resume, seek (VOD only)",
"EPG: current/next, mini-guide overlay",
"Resume: VOD/series only, resume threshold 5%–95%",
"Favorites: toggle, rail, sorted by recency",
"Search: text/voice, multi-source, ranked",
"Profiles: create/switch, history tracking",
"Parental controls: global PIN, rating filter, retry lock",
"Recommendations: based on watch history and TMDB metadata",
"Error handling: retry, banner, offline UI"
]

### Explicitly Excluded Features

excludes = \[
"Catch-up TV",
"Recording (DVR, series record)",
"Full grid EPG",
"Cloud resume",
"Social sharing",
"Cross-device sync"
]

### Non-functional Constraints

performance = {
startup\_time = "≤2s",
nav\_latency = "≤300ms"
}
minimum\_test\_coverage = "70%"
layering\_required = true
compose\_required = true

## 2\_platform\_and\_ui\_baseline

platform = "Android TV"
device\_constraints = \["low RAM (512MB)", "slow CPUs", "non-touch"]
ui\_framework = "Jetpack Compose"
dpad\_navigation = true
focusable\_components = \["FocusableCard", "FocusableButton", "FocusableImageCard"]
accessibility = {
high\_contrast = "system-default",
text\_scaling = "system-default",
talkback = "Compose-compatible",
remappable\_buttons = false
}

## 3\_navigation\_and\_screen\_flows

### 3.1 Screen List

screens = \[
"Splash",
"Config",
"ProfileSelection",
"Home",
"Movies",
"MovieDetail",
"Series",
"SeriesDetail",
"EpisodeDetail",
"SearchResults",
"VideoPlayer",
"EPGOverlay",
"Settings",
"NavFrame (persistent)"
]

### 3.2 Navigation Flow
* Launch starts at `Splash` → `Config` (if needed) → `ProfileSelection`
* Post-login route lands at `Home`
* Navigation is single-activity, Compose-based
* `NavFrame` persists on all screens except `VideoPlayer`

### 3.3 Focus & D-Pad Behavior

* All screens use D-pad left/right/up/down navigation
* Content cards scale on focus and show metadata
* PIN Prompt is modal and not part of nav graph

### 3.4 Wireframes

```
Splash Screen
-------------
[ Supernova Logo ]
[ Loading Spinner ] ← uses: LoadingSpinner

Config Screen
-------------
[ Input: Host     ] [ Port ]
[ Username        ] [ Password ]
[   [ Test & Save ]  ] ← uses: FocusableButton
[ Snackbar/Error Banner if failed ]

Profile Selection
-----------------
[ Profile Avatar 1 ]  [ + New Profile ]  [ Profile Avatar 2 ]  ... ← uses: FocusableCard
Layout = angled perspective, carousel-style
Focus Behavior:
- Focused card: scaled (1.15x), centered, glow + Z-lift
- Non-focused cards: smaller, blurred or desaturated
- D-pad left/right: scrolls carousel with easing
- On focus: backdrop may blur or darken

Home Screen
-----------
[ Continue Watching ] → [Card1] [Card2] ... ← uses: ContentRail + MediaCard
[ Favorites         ] → [Card1] [Card2] ... ← uses: ContentRail + MediaCard
[ Recommendations   ] → [Card1] [Card2] ... ← uses: ContentRail + MediaCard
[ Everyone's Watching ] → [Card1] [Card2] ... ← uses: ContentRail + MediaCard
Source: TMDB trending API

Search Results
--------------
[ Search Bar ] ← uses: SearchBar
[ Top Results Grid ] ← uses: MediaCard or FocusableCard
[ EPG Hits (if any) ]

Movie Detail
------------
[ BACKDROP IMAGE ]
[ Poster | Title, Overview, ★ Rating ]
[ ▶ Play ] [+ Favorite] [ℹ More Info] ← uses: FocusableButton
[ Cast Grid (photos + names) ] ← uses: FocusableImageCard

Series Detail (Merged)
----------------------
[ Series Poster | Title | Overview | Vote ]
[ ▶ Play ] [+ Favorite] [ℹ More Info] ← uses: FocusableButton
[ Season Carousel: S1 S2 S3 ... ] ← uses: FocusableImageCard
[ Selected Season: overview, vote ]
[ Episode Rail: Thumbnail, Ep#, Title, Score ] ← uses: FocusableCard or MediaCard

Video Player
------------
[ Fullscreen Video ]
[ (Optional) Player Overlay on D-pad OK ]
[ Resume / Start Over Dialog if applicable ] ← uses: FocusableButton

EPG Overlay
-----------
[ Now/Next info ]  ← current channel
[ Scrollable list of 5 visible channels ] ← uses: ContentRail + MediaCard
[ Channel Up/Down (D-pad), Jump via number ]

PIN Prompt (Modal)
------------------
[ 🔒 Enter PIN ]
[ [0] [1] [2] ... [9] ] ← uses: FocusableButton
[ Attempts Left: 2 ]
[ ❌ Cancel ] ← uses: FocusableButton

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

## 4\_functional\_behavior

### 4.0\_playback

pause\_resume = "supported for all stream types where provider allows"
seek = "supported for VOD only; seek disabled for live unless provider supports archive"
auto\_next\_episode = "enabled for series; triggered by episode track metadata"
resume\_restart\_ui = "card-style overlay with Resume / Start Over"
player\_overlay = "D-pad OK toggles overlay (playback controls, quality selector)"
ff\_rw = "not implemented; no variable speed support"
skip\_intro = "not supported (requires stream metadata)"
pip = "excluded from MVP"

### 4.1\_epg\_and\_mini\_guide

mini\_overlay = {
type = "5-channel overlay",
trigger = "remote or long press",
scroll = "D-pad up/down scroll full list (5 visible)",
channel\_number\_jump = "numeric input + Enter (with or without overlay)"
}
current\_next\_info = true
fallback\_no\_info\_label = "No info"
full\_grid = "excluded from MVP"
reminders = "supported via Room + WorkManager (basic alarm only)"
recording = "excluded from MVP"

### 4.2\_resume\_and\_continue\_watching

continue\_watching = {
types = \["movies", "series episodes", "VOD only"],
max\_items = 15,
removal\_threshold = "≥95% watched",
resume\_threshold = "≥5% watched",
sort\_order = "last watched",
rail\_component = "ContentRail"
}

on\_focus\_behavior = {
show\_title = true,
show\_type = true,
show\_progress = true,
focus\_glow = true
}
cross\_device\_resume = false

### 4.3\_favorites

favorites = {
toggle = "via content icon",
rail = {
layout = "ContentRail",
max\_items = 15,
sort = "recency",
position = "Home screen"
},
remove = "long-press or menu",
backend\_sync = true
}

### 4.4\_search

fts4 = true

**Indexing Notes**

query\_sources have different indexing logic:

* streams: fuzzy match on raw titles (XC-provided), useful for live channels
* movies: indexed with TMDB metadata (title, alt names, keywords)
* series: indexed via episode/season hierarchy with TMDB enrichment
* epg: indexed temporally (title + timestamp window), limited TTL
  query\_sources = \["streams", "movies", "series", "EPG"]
  voice\_search = "fallback to system intent"
  ranking = "exact > fuzzy > boosted"
  history = "excluded"
  suggestions = "none"
  empty\_state = "No results found"
  fts\_bin\_limit = 30

### 4.5\_profiles

create = "username only"
delete = false
edit = false
reorder = false
limit = "unbounded (UI constrained only)"
profile\_history = "used for resume and recs"
persist\_across\_hosts = true
avatar\_selection = "none"

### 4.6\_parental\_controls

pin\_prompt\_trigger = "on restricted content access"
pin\_format = "4-digit numeric"
pin\_retry\_limit = 3
pin\_lockout = "5 minutes after max attempts"
pin\_change\_ui = "Settings screen"
rating\_enforcement = "TMDB-based (TV-MA, NC17)"
not\_rated\_fallback = "locked by default"

### 4.7\_recommendations

enabled = true
trigger = "watch history + TMDB metadata present"
worker = "weekly (RecWorker)"
scope = "VOD and series episodes only"

data\_sources = \[
"WatchHistory", "TmdbMetadata"
]

scoring\_formula = "overlap\_count × recency\_weight × percent\_watched\_weight"

recency\_weight (in days) = {
0: 1.0,
1: 0.8,
2: 0.6,
3..6: 0.5,
7..13: 0.3,
14+: 0.2
}

percent\_watched\_weight = {
">=95": 0.2,
"70–94": 0.7,
"40–69": 1.0,
"5–39": 0.5,
"<5": 0.0
}

output\_table = "recommendation (profile\_id, content\_id, score, source\_tag, created\_at)"

rail = {
component = "ContentRail",
title = "Recommended For You",
sort = "score DESC"
}

### 4.8\_error\_handling

playback\_errors = {
404 = "toast",
buffering\_fail = "toast"
}
sync\_errors = {
5xx = "retry with exponential backoff (max 3)"
}
offline\_mode = {
entry\_check = "connectivity check on Splash; if offline → show blocking error screen with Retry only (like Netflix)",
home\_enabled = true,
playback\_disabled = true
}
logging = "crashlytics or local logcat"
error\_ui = {
minor = "toast",
global = "banner (top of screen)"
}

## 5\_data\_architecture

fts\_enabled = true
fts\_entities = \["Stream", "Program"]
fts\_rule = "All search queries must use MATCH or LIKE on FTS-backed virtual tables"
fts\_integrity = "No direct inserts/updates to base tables — use Room DAO only"

secure\_storage = true
secure\_backend = "SecureDataStore"
secured\_fields = \["provider credentials", "parental PIN"]

room\_entities = \[
"Stream { id, title, category\_id, stream\_type, is\_live, number }",
"Program { id, title, start, end, epg\_channel\_id }",
"Episode { id, title, series\_id, season\_num, episode\_num, vote, poster }",
"Category { id, name }",
"Profile { id, username }",
"Favorite { profile\_id, content\_id }",
"WatchHistory { profile\_id, content\_id, percent\_watched, timestamp }",
"Recommendation { profile\_id, content\_id, score, source\_tag, created\_at }",
"TmdbMetadata { content\_id, tmdb\_id, genres, keywords, type }"
]

daos = \[
"StreamDao",
"ProgramDao",
"WatchHistoryDao",
"TmdbMetadataDao",
"RecommendationDao"
]

entity\_notes = {
Season = "modeled as Episode.season\_num (no standalone entity)",
Content = "grouped via stream\_type and associated metadata"
}

## 6\_sync\_and\_ingestion

### 6.0\_catalog\_and\_epg\_sync

xc\_api\_ingest = true
parallel\_sync = true
sync\_triggers = \["on app launch", "manual refresh"]
fts\_refresh = "atomic table swap with shadow table"

### 6.1\_tmdb\_enrichment

strategy = "hover-triggered + preload"
preload\_count = 7000
weekly\_topup = 200
storage = "Room with TTL (≥7 days)"
fields\_used = \["genres", "keywords", "type"]
lookup\_key = "title + year"

### 6.2\_matching\_strategy

match\_fields = \["title", "category"]
threshold = 0.8
fallback = "partial match with normalized title"

### 6.3\_rate\_limit\_and\_retry

rate\_limit\_handling = "manual + retry with exponential backoff (max 3)"
sync\_errors = "toast or global banner based on severity"

### 6.4\_file\_parsing

xmltv\_parser = "XmlPullParser"
m3u\_support = "#EXTM3U standard"
charset\_fallback = "UTF-8 with 'Unknown' label if unsupported"

### 6.5\_reminders

reminder\_backend = "Room + WorkManager"
reminder\_scope = "Program-based (one-shot alarm)"
reminder\_ui = "Triggers system toast or local notification"

### 6.6\_sync\_constraints

partial\_sync\_allowed = false
error\_display = "banner for partial failures"
catalog\_sync = "required at launch"

## 7\_api\_contracts

### 7.0\_xc\_api

endpoints = \[
"auth: { user, pass } → { token, expires }",
"get\_live\_streams: \[ { stream\_id, name, category\_id, stream\_type } ]",
"get\_series: \[ { series\_id, episodes, cover, genre } ]",
"get\_epg: \[ { epg\_channel\_id, start, end, title, description } ]",
"player\_api",
"m3u: #EXTINF:-1 tvg-id="..." group-title="...",Title
http\://..."
]
rate\_limit\_handling = "manual + retry"

### 7.1\_tmdb\_api

fields\_used = \["genres", "keywords", "type"]
lookup\_strategy = "title + year match"
caching\_strategy = "Room preload (7000) + weekly top-up (200)"
language\_support = "system-locale fallback = en"
notes = "TMDB does not support multi-ID batch lookup; daily dump lacks tags"

## 8\_testing\_and\_harness

**Quality Gate**

coverage\_required = 70
forbidden = \["Robolectric", "JUnit4"]
fixture\_policy = "1 JSON fixture per task"
coroutines\_required = true
frameworks = \["JUnit5", "MockK", "runTest"]
daos = \{mocked: true, instantiation: "mockk<YourDao>()", stubbing: "every { dao.method() } returns flowOf(mockedData)"}
forbidden = \["Robolectric", "JUnit4"]
fixture\_policy = "1 JSON fixture per task"
coroutines\_required = true

### 8.1\_harness\_components

* BaseSyncTest  # Retrofit + MockWebServer integration base
* TestEntityFactory  # Builders for test Stream, Episode, Category, etc.
* CoroutineTestUtils  # runTest, advanceUntilIdle, dispatcher wrappers
* UiStateTestHelpers  # Compose state test helpers for UI logic
* SyncScenarioFactory  # Preconfigured sync inputs (e.g. retry windows, payloads)
* JsonFixtureLoader  # Loads JSON from src/test/resources/\*.json
* PreviewFactories  # Compose previews for loading/success/error states
* DbAssertionHelpers  # Reusable Room assertions: row count, entity equality
* MockWebServerExtensions  # Enqueue JSON/error responses with headers

## 9\_reusable\_components

### 9.1\_ui\_components

Each reusable UI component is defined by its purpose, props, expected states, and preview support.

#### FocusableButton

* **Purpose**: D-pad focusable action trigger
* **Props**: `onClick: () -> Unit`, `label: String`, `modifier: Modifier = Modifier`
* **States**: focused (glow + scale), unfocused (idle)
* **Usage**: Play, Favorite, Info buttons, PIN pad, Settings toggles

#### FocusableCard

* **Purpose**: Generic TV-optimized card container with focus scaling and glow
* **Props**: `onClick: () -> Unit`, `modifier: Modifier`, `content: @Composable () -> Unit`
* **States**:

  * focused: glow + border, scale up (e.g., 1.15x), Z-order lift
  * unfocused: dimmed or reduced scale (if in carousel)
* **Usage**: Profile selection (angled), Continue Watching rail, EPG item cards
* **Props**: `onClick: () -> Unit`, `modifier: Modifier`, `content: @Composable () -> Unit`
* **States**: focused (glow + border), unfocused
* **Usage**: Profile selection, Top Results Grid, Continue Watching items

#### FocusableImageCard

* **Purpose**: Card with image + optional metadata
* **Props**: `imageUrl: String`, `title: String`, `onClick: () -> Unit`
* **Usage**: Cast grid, Season carousel, About logos

#### MediaCard

* **Purpose**: Content thumbnail with label + progress
* **Props**: `posterUrl`, `title`, `progress: Float?`, `onClick`
* **Usage**: Continue Watching rail, Favorites rail

#### ContentRail

* **Purpose**: Horizontal scrolling container for items
* **Props**: `title`, `items: List<Any>`, `itemContent: @Composable`
* **Usage**: Home screen rails, EPG overlay

#### SearchBar

* **Purpose**: Input + icon for keyboard/voice search
* **Props**: `value: String`, `onValueChange`, `onSearch`
* **Usage**: SearchResults screen

### 9.2\_data\_components

The following reusable components are shared across Room DAOs and test setups.

#### TestEntityFactory

* **Purpose**: Generate consistent test data for entities (`Stream`, `Episode`, `Category`, etc.)
* **Usage**: Shared across all layer tests

#### DbAssertionHelpers

* **Purpose**: Standardized test assertions for Room data
* **Includes**: rowCount, assertEntityEqual, assertListEqual
* **Usage**: DAO and migration test coverage

#### CoroutineTestUtils

* **Purpose**: Utility for controlling test coroutine scope
* **Includes**: `runTest`, `advanceUntilIdle`

#### Room Entity Pattern

* **Purpose**: Consistent pattern for Room data classes
* **Includes**: `@Entity`, `@PrimaryKey`, `@ColumnInfo`, `@TypeConverters` as needed
* **Guideline**: All mutations via DAO only, no direct SQL allowed

#### TmdbMetadata Normalizer (Planned)

* **Purpose**: Normalize `title + year` lookups and tag splits
* **Status**: To be defined with enrichment logic

## 10\_app\_config\_and\_policies

### Provider Credentials

* Test connection: implicit (on field input)
* Fields: host, port, username, password (validated inline)
* Save enabled only if all fields are valid
* Error handling:

  * Input errors = inline
  * Network errors = snackbar
  * Sync failure = top banner

### Profiles

* Persisted across host switch
* Max profiles: unbounded (UI-constrained)
* Avatar selection: none
* Editing: rename/delete/reorder all disabled

### Parental Controls

* PIN prompt = triggers on restricted content
* Format = 4-digit numeric
* Retry limit = 3 attempts
* Lockout = 5 minutes
* Change UI = exposed in settings
* Rating fallback = NotRated = locked

### EPG Overlay

* Trigger = remote or long-press
* Navigation = D-pad (vertical: channels, horizontal: time)
* Display = overlay with 5 visible rows, scrollable full list
* Fallback = "No info"

### Playback

* Controls: play, pause, seek (VOD-only)
* Quality: adaptive (auto)
* Resume point: VOD-only
* Error fallback:

  * 404 / stream fail = toast
  * Buffer failure = toast

### Search

* FTS: enabled (MATCH or LIKE only)
* Max results: 30
* Empty state: "No results found"
* Suggestions: none
* Voice: fallback to system intent
* Ranking = exact > fuzzy > boosted

### Favorites

* Toggle via content icon
* Max = 15 items
* Sort = recency
* Remove via menu or long-press

### Recommendations

* TMDB sync = nightly top-up (200/week)
* Personalization = watch history + genre/keyword
* Editorial = static JSON rails
* Fallback = TMDB trending

### Errors

* Minor = toast
* Global = banner (top)
* Retry = exponential backoff (max 3)
* Offline mode:

  * Checked on Splash
  * Shows blocking error screen with Retry only
  * Home UI = enabled
  * Playback = disabled
* Logging = Crashlytics or local logcat

### Settings

* Language: subtitle language selection (dropdown)
* Clear cache: allowed
* Provider config: accessible via Settings
* Accessibility: defers to Android TV system defaults

### Sync

* Cadence: EPG = every 24h
* Catalog sync: on app launch
* Manual refresh: enabled
* Partial sync: disabled (atomic only)
* EPG match threshold = 0.8

### Telemetry

* Consent = minimal
* Events tracked = playback start/stop, search, favorites
* Crash reporting = Firebase
* Opt-out = not available for MVP

### Localization

* Charset fallback = UTF-8, fallback label = "Unknown"
* UI language = system locale (fallback = en)
* TMDB language = system locale or fallback
