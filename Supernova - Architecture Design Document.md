# IPTV Client MVP – Developer Architecture Document

**Last updated:** 2025-07-13 12:47:16 UTC

---

## 1. PROJECT GOAL & SCOPE

### Goal:

- Provider-agnostic IPTV client for Android TV and variants (Chromecast with Google TV, Fire TV,
  etc).
- Focus on fast, resilient catalog/EPG ingest, on-device search, and personalized recommendations.
- All syncs, parsing, and entity validation are memory- and token-efficient.

### Scope:

- Works with any XC API-compatible provider.
- All data lives in local Room DB.
- Sync and FTS search are core features.
- Minimal server infrastructure.

---

## 2. TARGET PLATFORMS

- Android TV (Jetpack Compose)
- Chromecast with Google TV
- Amazon Fire TV (Leanback UI compatible)
- Modern Android TV boxes
- Mobile/tablet companion app (idea bucket)

---

## 3. CRITICAL NFRs (Non-Functional Requirements)

- Responsive D-pad/focus navigation.
- Streaming JSON ingest (constant memory).
- Resilient to partial/malformed provider data.
- GDPR-light: minimal consent/policy, no crash on missing info.
- Sync and DB encoding always UTF-8 (fallback to HTTP charset if needed).
- FTS4 only (Room compatible), never FTS5.
- No EPG grid in MVP.

---

## 4. DATA ARCHITECTURE

### Entities (main fields only, see schema for all):
- `ProviderConfig(id PK, baseUrl, username, password, updatedAt)`
- `Category(categoryId PK, name, parentId, type)`
- `Stream(streamId PK, title, year, streamType, thumbnailUrl, bannerUrl, tmdbId, mediaType, tmdbSyncedAt, providerId FK, containerExtension, epgChannelId, tvArchive, tvArchiveDuration, directSource, customSid, rating, rating5Based)`
- `StreamCategory(streamId FK, categoryId FK, PK(streamId, categoryId))`
- `Season(seriesId FK, seasonNumber PK, name, overview, cover, coverBig, airDate, voteAverage)`
- `Episode(episodeId PK, seriesId FK, seasonNumber FK, episodeNum, title, fileUrl, addedAt, containerExt, plot, releaseDate, durationSecs, rating, movieImage, coverBig, tmdbId)`
- `EpgChannel(epgChannelId PK, displayName, iconUrl)`
- `EpgProgramme(programmeId PK, epgChannelId FK, startAt, endAt, title, subTitle, description, category, iconUrl, rating)`
- `EpgProvider(providerId PK, name, feedUrl, lastSync)`
- `EpgChannelExternal(externalId PK, providerId FK, providerChannelId, displayName, iconUrl)`
- `EpgChannelMap(epgChannelId FK, externalId FK, confidence, mappedAt, PK(epgChannelId, externalId))`
- `EpgProgrammeExternal(progExtId PK, externalId FK, startAt, endAt, title, subTitle, description, category, iconUrl, rating)`
- `UserProfile(userId PK, username, email, pin4, createdAt)`
- `WatchHistory(historyId PK, userId FK, streamId FK, episodeId FK, watchedAt, durationSec, progress)`
- `Reaction(reactionId PK, userId FK, streamId FK, reactionType, createdAt)`
- `Keyword(keywordId PK, name)`
- `StreamKeyword(streamId FK, keywordId FK, PK(streamId, keywordId))`
- `Mood(moodId PK, name)`
- `MoodKeyword(moodId FK, keywordId FK, PK(moodId, keywordId))`
- `Recommendation(recId PK, userId FK, streamId FK, recoAt, score, source, moodId FK)`
- FTS:
    - `StreamFts(FTS4, title, description, director, cast, genre, plot, rowid=streamId)`
    - `EpgProgrammeFts(FTS4, title, subTitle, description, rowid=programmeId)`

### DAO List (core only):

- `StreamDao`, `EpisodeDao`, `CategoryDao`, `SearchDao`, `EpgDao`, `RecommendationDao`,
  `WatchHistoryDao`, `MoodDao`, `ProfileDao`
- Optional: `ReactionDao`, `TmdbCacheDao`

### Minimum required fields per entity:

- **Category:** id, name, type
- **Stream:** id, streamType, containerExtension, categoryIds
- **EPG:** channelId, startAt, endAt, title
- **Season:** seriesId, seasonNumber
- **Episode:** episodeId, seriesId, seasonNumber, episodeNum, title

> All others optional; incomplete rows skipped, no crash/fail.

---

## 5. FUNCTIONAL ARCHITECTURE & GLOBAL FLOW

### Detailed Application Flow

- **Core Application States**:
    - Configuration: Credentials and host stored securely
    - Synchronization: Previous data sync completed successfully
    - Validation: API credentials verified as valid
    - Profiles: At least one user profile exists
    - Host Change: New vs. previous host endpoint

- **Navigation Logic**:
    - **At Application Launch (Splash Screen)**:
        - If configuration is missing or invalid → Guide user to Configuration Screen
        - If properly configured with successful sync and profiles → Show Profile Selection
        - If configured but no profiles exist → Direct to Profile Creation
        - If configured with profiles but sync needed → Display Loading Screen

    - **During Configuration**:
        - For invalid credentials → Prompt user to retry
        - When host remains unchanged with existing profiles → Proceed to Profile Selection
        - When host changes with existing profiles → Preserve profiles but reset content data,
          initiate sync, then show Loading Screen
        - When host changes without profiles → Reset content data, sync, then guide to Profile
          Creation

    - **Profile Creation Experience**:
        - Avatar options pre-loaded for smooth selection experience

    - **Loading Experience**:
        - Display progress while initial data sync completes
        - Once sync finishes → Transition to Profile Selection

### Ongoing:

- Time-based syncs with atomic swap of active/inactive tables
- All UI uses local Room DB
- Full-screen error + retry on network issues
- Parental lock gate for adding/editing profiles

---

## 6. DATA SYNC & ATOMIC SWAP LOGIC

    - Syncs run in parallel via WorkManager

### XC API ingest ###

    - Fetch categories
    - Split into 5 buckets, each parsed in parallel
    - New streams → "inactive" → atomic swap to "active"

### EPG: ### 

- Sync both internal/external
- 24h window atomic swap

**EPG Channel Matching**

- **Automated Matching**: Map external channels via EpgChannelMap if confidence ≥ 0.8
- **Low Confidence Handling**: < 0.8 confidence triggers flag in admin matching tool (deferred to
  idea bucket)
- **Unmatched Channels**: Render as "Unknown Channel" and skip detailed EPG

### TMDB: ### 

Fill missing metadata for streams post-ingest

**API Endpoints Used**:

- Trending: `/trending/movie/day` and `/trending/tv/week` for daily and weekly trending rails
- Details & Cast: `/movie/{movie_id}` and `/tv/{tv_id}` for metadata, cast, and ratings
- Search & Fallbacks: `/search/movie` and `/search/tv` for matching by title/year
- Keywords & Recommendations: `/movie/{movie_id}/keywords` and `/movie/{movie_id}/recommendations`
  for content enrichment

### Title & Category Normalization

    - **Text Cleaning Pipeline**:
    - Strip punctuation and diacritics
    - Collapse whitespace and lowercase
    - Remove parenthetical years
    - Special regex to drop prefixes like "Live", "24/7", "Replay" before normalizing
    - **Channel Name Handling**:
    - Skip channels where `stream.name` is empty or only whitespace
    - Use raw name from `stream.name` field as fallback
    - Parser applies normalization pipeline to all names
    - **Category Mapping**:
    - Static mapping table from provider categories → our Category.type
    - Unmapped categories default to "uncategorized"
    - **Matching Strategy**:
    - Primary: Title + year exact match (for movies & series)
    - Secondary: Fuzzy search (Levenshtein ≤ 2), highest popularity wins
    - Tertiary: On no match, skip enrichment and mark `tmdbSyncedAt` to avoid re-querying    

### FTS: ### 

    - On-device search using Room FTS4 for performance.
    - User queries normalized through the same pipeline as content.
    - Results grouped by content type with relevance ranking.

### FTS Implementation

    - **Index**: Room FTS4 on StreamFts and EpgProgrammeFts
    - **Query Processing**: Apply the same normalization pipeline as content
    - **Result Sections**: "On-Demand", "Live Channels", and "Episodes"

### Ranking Logic

    - **Exact Match**: Boost via MATCH syntax
    - **Fuzzy**: Edit-distance tolerance
    - **Live Content**: Boost if airing soon

### Voice Flow

    - **Implementation**: Android Speech API → normalize transcription → query DAO

### Recommendations: ### 

    - Weekly updates, filtered to local catalog

### Sync Frequencies: ###

- **Periodic**: Every 12 hours (EPG needs only once/day)
- **Setup**:

```kotlin
val periodicSync = PeriodicWorkRequestBuilder<LiveSyncWorker>(12, TimeUnit.HOURS)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()

WorkManager.getInstance(context)
    .enqueueUniquePeriodicWork(
        "LiveSyncWorker",
        ExistingPeriodicWorkPolicy.KEEP,
        periodicSync
    )
```

### Streaming Parsing: ###

    - JSON parsed via Gson/Moshi + JsonReader (streaming)
    - Encoding: UTF-8 preferred; fallback to HTTP charset

### Favorites

- **Implementation**: Leverage Reaction(reactionType = favorite)
- **UI**: Show "Favorites" rail on Home screen

### Continue Watching

- **Criterion**: Any WatchHistory progress > 5%
- **UI**: Surface in top Home rail

### Parallel Sync Implementation

- **Concurrency**: Use Kotlin Coroutines with 5 parallel tasks
- **Implementation**:

```kotlin
class LiveSyncWorker(
    ctx: Context, params: WorkerParameters
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = coroutineScope {
        // 1. Fetch category list
        val categories = fetchCategories()
        // 2. Launch up to 5 concurrent category fetches
        val results = categories.map { category ->
            async(Dispatchers.IO.limitedParallelism(5)) {
                runCatching { fetchAndStoreCategory(category) }
                    .onFailure { recordFailure(category.name, it) }
            }
        }.awaitAll()
        // 3. Show errors if any
        if (failedCategories.isNotEmpty()) {
            showNotificationForFailures()
            // return SUCCESS so WM doesn't retry endlessly
        }
        Result.success()
    }
}
```

### Error Handling

- **Error Logging**:
  ```kotlin
  Log.e("LiveSyncWorker", "Failed to download live category \"$categoryName\"", exception)
  ```
- **User Notifications**:
    - 1 failure → "Could not download live category 'USA News'."
    - 2–5 failures → "Failed to sync categories: A, B, C."
    - > 5 failures → "Sync failed. Please try again later."
- **UI Integration**:
    - Error Screen: Use `activity_loading.xml` layout for error states
    - "Try Again" Button:
  ```kotlin
  binding.tryAgainButton.setOnClickListener {
    WorkManager.getInstance(context)
      .enqueueUniqueWork(
        "LiveSyncWorker",
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<LiveSyncWorker>()
          .setConstraints(
            Constraints.Builder()
              .setRequiredNetworkType(NetworkType.CONNECTED)
              .build()
          )
          .build()
      )
  }

### Error Handling Details

- **Retry Strategy**:
    - WorkManager exponential backoff (1 min → 1 hr max)
    - Initial failures retry quickly, then gradually increase interval
    - Prevents excessive battery drain while ensuring eventual success
- **Fallback Strategy**:
    - If a category fetch fails, keep last successful data in Room cache
    - Show stale data rather than blank rails
    - Visual indicator for stale content with last sync timestamp

### Image Preloading Strategy

- **Smart Preloading**:
    - Preload thumbnails for 2× visible rails (half before + current rail + half after)
    - Optimize memory usage by limiting active preload scope
    - Discard cached images outside active viewing area when memory pressure high
- **Prioritization**:
    - Main screen content receives highest preloading priority
    - Recently viewed categories get second priority
    - All images eventually preloaded in background during idle time
- **Implementation**:
    - Use Glide/Coil with custom preloading strategy
    - Memory-based LRU cache with size based on device tier
    - Disk caching for offline viewing of previously loaded images

---

## 7. XC IPTV API REFERENCE ## 

- All XC API requests use the standard player_api.php endpoints
- Authentication required for all endpoints (username/password params)
- JSON responses parsed via streaming for memory efficiency

### Core Endpoints

- **Authentication**: `player_api.php?username=XXX&password=XXX`
    - Returns user_info and server_info objects
    - Sample response:
  ```json
  {
    "user_info": {
      "username": "user123",
      "password": "pass123",
      "message": "",
      "auth": 1,
      "status": "Active",
      "exp_date": "1640995200",
      "is_trial": "0",
      "active_cons": "1",
      "created_at": "1609459200",
      "max_connections": "1",
      "allowed_output_formats": ["ts", "m3u8"]
    },
    "server_info": {
      "url": "http://domain:port",
      "port": "8080",
      "https_port": "8080",
      "server_protocol": "http",
      "rtmp_port": "1935",
      "timezone": "Europe/London",
      "timestamp_now": 1640995200,
      "time_now": "2021-12-31 12:00:00"
    }
  }
  ```

### Category Endpoints

- **Live TV Categories**: `player_api.php?username=XXX&password=XXX&action=get_live_categories`
    - Sample response:
  ```json
  [
    {
      "category_id": "1",
      "category_name": "Entertainment",
      "parent_id": 0
    },
    {
      "category_id": "2", 
      "category_name": "Sports",
      "parent_id": 0
    }
  ]
  ```
- **VOD Categories**: `player_api.php?username=XXX&password=XXX&action=get_vod_categories`
- **Series Categories**: `player_api.php?username=XXX&password=XXX&action=get_series_categories`

### Content List Endpoints

- **Live TV Streams**:
  `player_api.php?username=XXX&password=XXX&action=get_live_streams&category_id=XXX`
    - Sample response:
  ```json
  [
    {
      "num": 1,
      "name": "Channel Name",
      "stream_type": "live",
      "stream_id": 12345,
      "stream_icon": "http://domain/images/channel.png",
      "epg_channel_id": "channel.uk",
      "added": "1609459200",
      "category_id": "1",
      "custom_sid": "",
      "tv_archive": 1,
      "direct_source": "",
      "tv_archive_duration": 7
    }
  ]
  ```
- **VOD Streams**: `player_api.php?username=XXX&password=XXX&action=get_vod_streams&category_id=XXX`
    - Sample response:
  ```json
  [
    {
      "num": 1,
      "name": "Movie Title",
      "stream_type": "movie",
      "stream_id": 54321,
      "stream_icon": "http://domain/images/movie.jpg",
      "rating": "7.5",
      "rating_5based": 4,
      "added": "1609459200",
      "category_id": "1",
      "container_extension": "mp4",
      "custom_sid": "",
      "direct_source": ""
    }
  ]
  ```
- **Series List**: `player_api.php?username=XXX&password=XXX&action=get_series&category_id=XXX`

### Content Detail Endpoints

- **VOD Info**: `player_api.php?username=XXX&password=XXX&action=get_vod_info&vod_id=XXX`
    - Returns detailed movie metadata including cast, director, plot
- **Series Info**: `player_api.php?username=XXX&password=XXX&action=get_series_info&series_id=XXX`
    - Returns seasons, episodes, and series metadata

### EPG Endpoints

- **Simple EPG**:
  `player_api.php?username=XXX&password=XXX&action=get_simple_data_table&stream_id=XXX`
    - Sample response:
  ```json
  {
    "epg_listings": [
      {
        "id": "1234567890",
        "epg_id": "12345",
        "title": "Program Title",
        "lang": "en",
        "start": "2021-12-31 20:00:00",
        "end": "2021-12-31 22:00:00",
        "description": "Program description",
        "channel_id": "channel.uk",
        "start_timestamp": "1640995200",
        "stop_timestamp": "1641002400",
        "now_playing": 0,
        "has_archive": 1
      }
    ]
  }
  ```
- **Short EPG**:
  `player_api.php?username=XXX&password=XXX&action=get_short_epg&stream_id=XXX&limit=XXX`
- **Full XMLTV**: `xmltv.php?username=XXX&password=XXX` (returns XML format)

### Stream URLs

- **Live TV**: `http://domain:port/live/username/password/stream_id.ext`
- **VOD**: `http://domain:port/movie/username/password/stream_id.ext`
- **Series**: `http://domain:port/series/username/password/episode_id.ext`
- **Archive**:
  `http://domain:port/timeshift/username/password/stream_id/start_timestamp/duration.ext`

### Playlist Generation

- **Extended M3U**: `get.php?username=XXX&password=XXX&type=m3u_plus&output=ts`
    - Sample response:
  ```
  #EXTM3U
  #EXTINF:-1 tvg-id="channel.uk" tvg-name="Channel Name" tvg-logo="http://domain/images/channel.png" group-title="Entertainment",Channel Name
  http://domain:port/live/username/password/12345.ts
  ```
- **Basic M3U**: `get.php?username=XXX&password=XXX&type=m3u&output=m3u8`

## 7a. TMDB API REFERENCE ##

All TMDB API requests use the standard api.themoviedb.org/3 endpoints.
Authentication is via API key (query param or header).
JSON responses are parsed using Moshi/Gson (streaming not required).

---
### Trending

* **Trending Movies (Day):** `/trending/movie/day`
* **Trending TV (Week):** `/trending/tv/week`

  * Returns a list of trending movies or TV shows for the day/week.
  * **Optional location support:**

    * If region/language is required, include the `region` and/or `language` query parameters, e.g.:

      * `/trending/movie/day?region=US&language=en-US`
      * `/trending/tv/week?region=GB&language=en-GB`
    * If not provided, TMDB defaults to global trending.
  * **API Key inclusion:**

    * Always include the API key using the `api_key` query parameter:

      * Example: `/trending/movie/day?api_key=YOUR_API_KEY`
    * For added security, you may also use the `Authorization: Bearer YOUR_API_KEY` header.
  * Key fields in each result:

    * `id` (TMDB ID, required)
    * `title`/`name`
    * `release_date`/`first_air_date` (extract year)
    * `genre_ids` (category IDs)
    * (other metadata)
  * Sample response:

    ```json
    [
      {
        "id": 634649,
        "title": "Spider-Man: No Way Home",
        "release_date": "2021-12-15",
        "genre_ids": [28, 12, 878],
        "overview": "...",
        "poster_path": "/path.jpg",
        "vote_average": 8.4
      }
    ]
    ```

---

### Details & Metadata

* **Movie Details:** `/movie/{movie_id}`
* **TV Details:** `/tv/{tv_id}`

  * Returns full metadata for a movie or TV series.
  * **API Key inclusion:**

    * Use `api_key` query parameter or `Authorization` header as above.
  * Key fields:

    * `id` (TMDB ID, required)
    * `title`/`name`
    * `release_date`/`first_air_date` (year)
    * `genres` (list of `{id, name}`)
    * (all extended metadata fields)
  * Sample response:

    ```json
    {
      "id": 634649,
      "title": "Spider-Man: No Way Home",
      "release_date": "2021-12-15",
      "genres": [
        { "id": 28, "name": "Action" },
        { "id": 12, "name": "Adventure" }
      ]
      // ...
    }
    ```
---

### Search

* **Movie Search:** `/search/movie?query=...&year=...`
* **TV Search:** `/search/tv?query=...&first_air_date_year=...`

  * Returns a list of matching movies or TV series by title/year.
  * **API Key inclusion:**

    * Use `api_key` query parameter or `Authorization` header.
  * Key fields:

    * `id` (TMDB ID, required)
    * `title`/`name`
    * `release_date`/`first_air_date` (year)
    * `genre_ids`
  * Sample response:

    ```json
    [
      {
        "id": 634649,
        "title": "Spider-Man: No Way Home",
        "release_date": "2021-12-15",
        "genre_ids": [28, 12, 878]
      }
    ]
    ```

---

### Keywords

* **Movie Keywords:** `/movie/{movie_id}/keywords`
* **TV Keywords:** `/tv/{tv_id}/keywords`

  * Returns keywords for a given movie or TV series.
  * **API Key inclusion:**

    * Use `api_key` query parameter or `Authorization` header.
  * Key fields:

    * `id` (TMDB ID, required)
    * `keywords`/`results` (list of `{id, name}`)
  * Sample response:

    ```json
    {
      "id": 634649,
      "keywords": [
        { "id": 123, "name": "superhero" },
        { "id": 456, "name": "marvel" }
      ]
    }
    ```

---

### Recommendations

* **Movie Recommendations:** `/movie/{movie_id}/recommendations`
* **TV Recommendations:** `/tv/{tv_id}/recommendations`

  * Returns a list of recommended movies or TV series for a given title.
  * **API Key inclusion:**

    * Use `api_key` query parameter or `Authorization` header.
  * Key fields:

    * `id` (TMDB ID, required)
    * `title`/`name`
    * `release_date`/`first_air_date` (year)
    * `genre_ids`
  * Sample response:

    ```json
    [
      {
        "id": 634648,
        "title": "Another Movie",
        "release_date": "2020-06-10",
        "genre_ids": [28, 12]
      }
    ]
    ```

---

### Genres (Categories)

* **Movie Genres:** `/genre/movie/list`
* **TV Genres:** `/genre/tv/list`

  * Returns all genre mappings for movies or TV.
  * **API Key inclusion:**

    * Use `api_key` query parameter or `Authorization` header.
  * Key fields:

    * `id`, `name`
  * Sample response:

    ```json
    {
      "genres": [
        { "id": 28, "name": "Action" },
        { "id": 12, "name": "Adventure" }
      ]
    }
    ```

---

**Field Inclusion Note:**
For all movies/series enriched via TMDB, always fetch and store:

* `id` (TMDB ID)
* `year` (from `release_date` or `first_air_date`)
* `genres` (categories, resolved to names)
* `keywords` (from keywords endpoint)

**API Key inclusion is mandatory for all requests:**

* Use the `api_key` parameter in the query string, or the `Authorization: Bearer` header for better security in server-to-server communication.
* For trending and most endpoints, `region` and `language` can be optionally specified to localize results.

## 8. RECOMMENDATIONS LOGIC

- "Personalized Recommendations" = TMDB recs/trending, matched by title/year
- Mood-based recs = future idea (not MVP)
- Stored in `Recommendation` table with source (`tmdb`, `mood`)
- TMDB star rating shown on UI

### Favorites

- **Implementation**: Leverage Reaction(reactionType = favorite)
- **UI**: Show "Favorites" rail on Home screen

### Continue Watching

- **Criterion**: Any WatchHistory progress > 5%
- **UI**: Surface in top Home rail

---

## 9. OPEN QUESTIONS

1. TMDB API Usage: which endpoints for recs/trending/detail/etc?
2. Title/Category Cleaning: rules for normalization
3. Search Architecture: FTS config, result ranking, voice queries
4. EPG Matching: fallback/confidence/manual mapping
5. TMDB Fallbacks: what to do if no good match?
6. Favorites/Continue Watching: MVP behavior?
7. GDPR Export/Delete: required or future?
8. Sync Error Handling: backoff/retry patterns?
9. Advanced Settings: any dev toggles?

---

## 10. IDEA BUCKET / FUTURE

- Mood-based recs
- EPG overlay (5-channel view)
- Channel grouping by region/genre
- Advanced episode/series detail
- Manual EPG match tool
- GDPR export/erase
- Mobile/tablet companion app
- Home screen rec API
- Offline content
- Dynamic theming, RTL support
- Analytics/telemetry

---

**END OF DOC**


