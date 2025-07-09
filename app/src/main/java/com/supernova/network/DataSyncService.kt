package com.supernova.network

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.*
import com.supernova.network.models.*
import com.supernova.utils.ApiUtils.normalizeUrl
import com.supernova.utils.ApiUtils.parseTimestamp
import com.supernova.utils.ApiUtils.takeIfNotBlank
import com.supernova.utils.ApiUtils.toIntSafely
import com.supernova.utils.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataSyncService(
    private val database: SupernovaDatabase,
    private val secureStorage: SecureStorage,
    private val gson: Gson = Gson()
) {
    companion object {
        private const val TAG = "DataSyncService"
        private const val UNCATEGORIZED_ID = 999999
        private const val UNCATEGORIZED_NAME = "Uncategorized"
    }

    suspend fun syncAll(): Flow<SyncResult> = flow {
        Log.d(TAG, "Starting sync process")

        val portal = secureStorage.getPortal()
        val username = secureStorage.getUsername()
        val password = secureStorage.getPassword()

        if (portal == null || username == null || password == null) {
            Log.e(TAG, "No credentials found - portal: $portal, username: $username, password: ${password?.let { "*".repeat(it.length) }}")
            emit(SyncResult.Error("No credentials found"))
            return@flow
        }

        Log.d(TAG, "Using credentials - portal: $portal, username: $username")

        try {
            val apiService = ApiService.create(portal)
            val baseUrl = ApiService.buildLoginUrl(portal)
            Log.d(TAG, "Created API service with base URL: $baseUrl")

            emit(SyncResult.Progress("Starting sync...", 0, 6))

            // Step 1: Sync Live TV Categories
            emit(SyncResult.Progress("Syncing Live TV categories...", 1, 6))
            try {
                Log.d(TAG, "Fetching live TV categories...")
                val liveCategories = apiService.getLiveCategories(baseUrl, username, password)
                Log.d(TAG, "Live categories response - success: ${liveCategories.isSuccessful}, code: ${liveCategories.code()}")

                if (liveCategories.isSuccessful) {
                    val categories = liveCategories.body() ?: emptyList()
                    Log.d(TAG, "Retrieved ${categories.size} live TV categories")
                    syncCategories(categories, "live_tv")
                } else {
                    Log.e(TAG, "Live categories API failed: ${liveCategories.errorBody()?.string()}")
                }
                ensureDefaultCategory("live_tv")
            } catch (e: Exception) {
                Log.e(TAG, "Live TV categories sync failed", e)
                emit(SyncResult.Progress("Live TV categories failed, continuing...", 1, 6))
                ensureDefaultCategory("live_tv")
            }

            // Step 2: Sync VOD Categories
            emit(SyncResult.Progress("Syncing VOD categories...", 2, 6))
            try {
                Log.d(TAG, "Fetching VOD categories...")
                val vodCategories = apiService.getVodCategories(baseUrl, username, password)
                Log.d(TAG, "VOD categories response - success: ${vodCategories.isSuccessful}, code: ${vodCategories.code()}")

                if (vodCategories.isSuccessful) {
                    val categories = vodCategories.body() ?: emptyList()
                    Log.d(TAG, "Retrieved ${categories.size} VOD categories")
                    syncCategories(categories, "movie")
                } else {
                    Log.e(TAG, "VOD categories API failed: ${vodCategories.errorBody()?.string()}")
                }
                ensureDefaultCategory("movie")
            } catch (e: Exception) {
                Log.e(TAG, "VOD categories sync failed", e)
                emit(SyncResult.Progress("VOD categories failed, continuing...", 2, 6))
                ensureDefaultCategory("movie")
            }

            // Step 3: Sync Series Categories
            emit(SyncResult.Progress("Syncing Series categories...", 3, 6))
            try {
                Log.d(TAG, "Fetching series categories...")
                val seriesCategories = apiService.getSeriesCategories(baseUrl, username, password)
                Log.d(TAG, "Series categories response - success: ${seriesCategories.isSuccessful}, code: ${seriesCategories.code()}")

                if (seriesCategories.isSuccessful) {
                    val categories = seriesCategories.body() ?: emptyList()
                    Log.d(TAG, "Retrieved ${categories.size} series categories")
                    syncCategories(categories, "series")
                } else {
                    Log.e(TAG, "Series categories API failed: ${seriesCategories.errorBody()?.string()}")
                }
                ensureDefaultCategory("series")
            } catch (e: Exception) {
                Log.e(TAG, "Series categories sync failed", e)
                emit(SyncResult.Progress("Series categories failed, continuing...", 3, 6))
                ensureDefaultCategory("series")
            }

            // Step 4: Sync Live TV Streams
            emit(SyncResult.Progress("Syncing Live TV streams...", 4, 6))
            try {
                Log.d(TAG, "Fetching live TV streams...")
                val liveStreams = apiService.getLiveStreams(baseUrl, username, password)
                Log.d(TAG, "Live streams response - success: ${liveStreams.isSuccessful}, code: ${liveStreams.code()}")

                if (liveStreams.isSuccessful) {
                    val streams = liveStreams.body() ?: emptyList()
                    Log.d(TAG, "Retrieved ${streams.size} live TV streams")
                    syncLiveStreams(streams)
                } else {
                    Log.e(TAG, "Live streams API failed: ${liveStreams.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Live TV streams sync failed", e)
                emit(SyncResult.Progress("Live TV streams failed, continuing...", 4, 6))
            }

            // Step 5: Sync VOD Streams
            emit(SyncResult.Progress("Syncing VOD streams...", 5, 6))
            try {
                Log.d(TAG, "Fetching VOD streams...")
                val vodStreams = apiService.getVodStreams(baseUrl, username, password)
                Log.d(TAG, "VOD streams response - success: ${vodStreams.isSuccessful}, code: ${vodStreams.code()}")

                if (vodStreams.isSuccessful) {
                    val streams = vodStreams.body() ?: emptyList()
                    Log.d(TAG, "Retrieved ${streams.size} VOD streams")
                    syncVodStreams(streams)
                } else {
                    Log.e(TAG, "VOD streams API failed: ${vodStreams.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "VOD streams sync failed", e)
                emit(SyncResult.Progress("VOD streams failed, continuing...", 5, 6))
            }

            // Step 6: Sync Series Streams
            emit(SyncResult.Progress("Syncing Series streams...", 6, 6))
            try {
                Log.d(TAG, "Fetching series streams...")
                val seriesStreams = apiService.getSeriesStreams(baseUrl, username, password)
                Log.d(TAG, "Series streams response - success: ${seriesStreams.isSuccessful}, code: ${seriesStreams.code()}")

                if (seriesStreams.isSuccessful) {
                    val streams = seriesStreams.body() ?: emptyList()
                    Log.d(TAG, "Retrieved ${streams.size} series streams")
                    syncSeriesStreams(streams)
                } else {
                    Log.e(TAG, "Series streams API failed: ${seriesStreams.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Series streams sync failed", e)
                emit(SyncResult.Progress("Series streams failed, continuing...", 6, 6))
            }

            Log.d(TAG, "Sync completed successfully")
            emit(SyncResult.Success)

        } catch (e: Exception) {
            Log.e(TAG, "Sync failed with exception", e)
            emit(SyncResult.Error("Sync failed: ${e.message}"))
        }
    }

    @VisibleForTesting
    internal suspend fun ensureDefaultCategory(type: String) {
        try {
            Log.d(TAG, "Ensuring default category for type: $type")
            val existingCategory = database.categoryDao().getCategoryById(type, UNCATEGORIZED_ID)
            if (existingCategory == null) {
                val defaultCategory = CategoryEntity(
                    type = type,
                    id = UNCATEGORIZED_ID,
                    name = UNCATEGORIZED_NAME,
                    parent_id = 0
                )
                database.categoryDao().insertCategory(defaultCategory)
                Log.d(TAG, "Created default category for type: $type")
            } else {
                Log.d(TAG, "Default category already exists for type: $type")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to ensure default category for type: $type", e)
        }
    }

    private suspend fun syncCategories(categories: List<CategoryResponse>, type: String) {
        Log.d(TAG, "Syncing ${categories.size} categories for type: $type")

        val entities = categories.mapNotNull { category ->
            try {
                val categoryId = when {
                    category.categoryId.isNotBlank() -> category.categoryId.toIntSafely()
                    else -> null
                }

                val categoryName = when {
                    category.categoryName.isNotBlank() -> category.categoryName
                    else -> null
                }

                if (categoryId == null || categoryId <= 0 || categoryName == null || categoryId == UNCATEGORIZED_ID) {
                    Log.d(TAG, "Skipping invalid category - ID: $categoryId, Name: $categoryName")
                    return@mapNotNull null
                }

                CategoryEntity(
                    type = type,
                    id = categoryId,
                    name = categoryName,
                    parent_id = category.parentId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse category: ${category.categoryId} - ${category.categoryName}", e)
                null
            }
        }

        Log.d(TAG, "Parsed ${entities.size} valid categories for type: $type")

        if (entities.isNotEmpty()) {
            try {
                for (entity in entities) {
                    database.categoryDao().insertCategory(entity)
                }
                Log.d(TAG, "Successfully inserted ${entities.size} categories for type: $type")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert categories for type: $type", e)
            }
        }
    }

    private suspend fun syncLiveStreams(streams: List<LiveTvResponse>) {
        Log.d(TAG, "Syncing ${streams.size} live TV streams")

        val entities = streams.mapNotNull { stream ->
            try {
                if (stream.streamId <= 0) {
                    Log.d(TAG, "Skipping stream with invalid ID: ${stream.streamId}")
                    return@mapNotNull null
                }

                val categoryId = stream.categoryId.toIntSafely() ?:
                stream.categoryIds?.firstOrNull { it > 0 } ?:
                UNCATEGORIZED_ID

                val channelName = when {
                    stream.name.isNotBlank() -> stream.name
                    else -> "Unknown Channel"
                }

                LiveTvEntity(
                    channel_id = stream.streamId,
                    num = stream.num,
                    name = channelName,
                    stream_type = stream.streamType.takeIfNotBlank(),
                    stream_icon = stream.streamIcon.normalizeUrl(),
                    epg_channel_id = stream.epgChannelId.takeIfNotBlank(),
                    added = stream.added.parseTimestamp(),
                    custom_sid = stream.customSid.takeIfNotBlank(),
                    tv_archive = stream.tvArchive,
                    direct_source = stream.directSource.takeIfNotBlank(),
                    tv_archive_duration = stream.tvArchiveDuration,
                    category_type = "live_tv",
                    category_id = categoryId,
                    thumbnail = stream.thumbnail.normalizeUrl()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse live stream: ${stream.streamId} - ${stream.name}", e)
                null
            }
        }

        Log.d(TAG, "Parsed ${entities.size} valid live TV streams")

        if (entities.isNotEmpty()) {
            try {
                database.liveTvDao().deleteAllChannels()
                database.liveTvDao().insertChannels(entities)
                Log.d(TAG, "Successfully synced ${entities.size} live TV streams")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync live TV streams to database", e)
            }
        }
    }

    private suspend fun syncVodStreams(streams: List<VodResponse>) {
        Log.d(TAG, "Syncing ${streams.size} VOD streams")

        val validStreams = streams.filter { it.streamId > 0 }
        Log.d(TAG, "Found ${validStreams.size} valid VOD streams")

        val movieEntities = validStreams.mapNotNull { stream ->
            try {
                val movieName = when {
                    stream.name.isNotBlank() -> stream.name
                    else -> "Unknown Movie"
                }

                MovieEntity(
                    movie_id = stream.streamId,
                    num = stream.num,
                    name = movieName,
                    title = stream.title.takeIfNotBlank(),
                    year = stream.year,
                    stream_type = stream.streamType.takeIfNotBlank(),
                    stream_icon = stream.streamIcon.normalizeUrl(),
                    rating = stream.rating,
                    rating_5based = stream.rating5based,
                    added = stream.added.parseTimestamp(),
                    container_extension = stream.containerExtension.takeIfNotBlank(),
                    custom_sid = stream.customSid.takeIfNotBlank(),
                    direct_source = stream.directSource.takeIfNotBlank()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse VOD stream: ${stream.streamId} - ${stream.name}", e)
                null
            }
        }

        val movieCategoryEntities = validStreams.flatMap { stream ->
            val allCategoryIds = mutableListOf<Int>()

            stream.categoryId.toIntSafely()?.let { allCategoryIds.add(it) }
            stream.categoryIds?.filter { it > 0 }?.let { allCategoryIds.addAll(it) }

            if (allCategoryIds.isEmpty()) {
                allCategoryIds.add(UNCATEGORIZED_ID)
            }

            allCategoryIds.distinct().mapNotNull { categoryId ->
                try {
                    MovieCategoryEntity(
                        movie_id = stream.streamId,
                        category_type = "movie",
                        category_id = categoryId
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create movie category entity: ${stream.streamId} - $categoryId", e)
                    null
                }
            }
        }

        Log.d(TAG, "Parsed ${movieEntities.size} movies and ${movieCategoryEntities.size} movie-category relations")

        if (movieEntities.isNotEmpty()) {
            try {
                database.movieDao().deleteAllMovies()
                database.movieDao().insertMovies(movieEntities)
                if (movieCategoryEntities.isNotEmpty()) {
                    database.movieDao().insertMovieCategories(movieCategoryEntities)
                }
                Log.d(TAG, "Successfully synced ${movieEntities.size} VOD streams")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync VOD streams to database", e)
            }
        }
    }

    private suspend fun syncSeriesStreams(streams: List<SeriesResponse>) {
        Log.d(TAG, "Syncing ${streams.size} series streams")

        val validStreams = streams.filter { it.seriesId > 0 }
        Log.d(TAG, "Found ${validStreams.size} valid series streams")

        val seriesEntities = validStreams.mapNotNull { stream ->
            try {
                val seriesName = when {
                    stream.name.isNotBlank() -> stream.name
                    else -> "Unknown Series"
                }

                SeriesEntity(
                    series_id = stream.seriesId,
                    num = stream.num,
                    name = seriesName,
                    title = stream.title.takeIfNotBlank(),
                    year = stream.year.takeIfNotBlank(),
                    stream_type = stream.streamType.takeIfNotBlank(),
                    cover = stream.cover.normalizeUrl(),
                    plot = stream.plot.takeIfNotBlank(),
                    cast = stream.cast.takeIfNotBlank(),
                    director = stream.director.takeIfNotBlank(),
                    genre = stream.genre.takeIfNotBlank(),
                    release_date = stream.releaseDate.takeIfNotBlank(),
                    releaseDate = stream.releaseDateAlt.takeIfNotBlank(),
                    last_modified = stream.lastModified.takeIfNotBlank(),
                    rating = stream.rating.takeIfNotBlank(),
                    rating_5based = stream.rating5based,
                    backdrop_path = stream.backdropPath?.takeIf { it.isNotEmpty() }?.let {
                        try { gson.toJson(it) } catch (e: Exception) { null }
                    },
                    youtube_trailer = stream.youtubeTrailer.takeIfNotBlank(),
                    episode_run_time = stream.episodeRunTime.takeIfNotBlank()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse series stream: ${stream.seriesId} - ${stream.name}", e)
                null
            }
        }

        val seriesCategoryEntities = validStreams.flatMap { stream ->
            val allCategoryIds = mutableListOf<Int>()

            stream.categoryId.takeIfNotBlank()?.toIntSafely()?.let { allCategoryIds.add(it) }
            stream.categoryIds?.filter { it > 0 }?.let { allCategoryIds.addAll(it) }

            if (allCategoryIds.isEmpty()) {
                allCategoryIds.add(UNCATEGORIZED_ID)
            }

            allCategoryIds.distinct().mapNotNull { categoryId ->
                try {
                    SeriesCategoryEntity(
                        series_id = stream.seriesId,
                        category_type = "series",
                        category_id = categoryId
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create series category entity: ${stream.seriesId} - $categoryId", e)
                    null
                }
            }
        }

        Log.d(TAG, "Parsed ${seriesEntities.size} series and ${seriesCategoryEntities.size} series-category relations")

        if (seriesEntities.isNotEmpty()) {
            try {
                database.seriesDao().deleteAllSeries()
                database.seriesDao().insertSeriesList(seriesEntities)
                if (seriesCategoryEntities.isNotEmpty()) {
                    database.seriesDao().insertSeriesCategories(seriesCategoryEntities)
                }
                Log.d(TAG, "Successfully synced ${seriesEntities.size} series streams")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync series streams to database", e)
            }
        }
    }
}