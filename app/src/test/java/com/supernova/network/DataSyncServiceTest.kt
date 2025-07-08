package com.supernova.network

import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.*
import com.supernova.network.models.*
import com.supernova.utils.SecureStorage
import com.supernova.utils.ApiUtils.toIntSafely
import com.supernova.utils.ApiUtils.toLongSafely
import com.supernova.utils.ApiUtils.takeIfNotBlank
import com.supernova.utils.ApiUtils.orDefault
import com.supernova.utils.ApiUtils.parseTimestamp
import com.supernova.utils.ApiUtils.normalizeUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataSyncService(
    private val database: SupernovaDatabase,
    private val secureStorage: SecureStorage,
    private val gson: Gson = Gson()
) {

    companion object {
        private const val UNCATEGORIZED_ID = 999999
        private const val UNCATEGORIZED_NAME = "Uncategorized"
    }

    suspend fun syncAll(): Flow<SyncResult> = flow {
        val portal = secureStorage.getPortal()
        val username = secureStorage.getUsername()
        val password = secureStorage.getPassword()

        if (portal == null || username == null || password == null) {
            emit(SyncResult.Error("No credentials found"))
            return@flow
        }

        try {
            val apiService = ApiService.create(portal)
            val baseUrl = ApiService.buildLoginUrl(portal)

            emit(SyncResult.Progress("Starting sync...", 0, 6))

            // Step 1: Sync Live TV Categories
            emit(SyncResult.Progress("Syncing Live TV categories...", 1, 6))
            try {
                val liveCategories = apiService.getLiveCategories(baseUrl, username, password)
                if (liveCategories.isSuccessful) {
                    syncCategories(liveCategories.body() ?: emptyList(), "live_tv")
                }
                ensureDefaultCategory("live_tv")
            } catch (e: Exception) {
                // Continue with other syncs even if one fails
                emit(SyncResult.Progress("Live TV categories failed, continuing...", 1, 6))
                ensureDefaultCategory("live_tv")
            }

            // Step 2: Sync VOD Categories
            emit(SyncResult.Progress("Syncing VOD categories...", 2, 6))
            try {
                val vodCategories = apiService.getVodCategories(baseUrl, username, password)
                if (vodCategories.isSuccessful) {
                    syncCategories(vodCategories.body() ?: emptyList(), "movie")
                }
                ensureDefaultCategory("movie")
            } catch (e: Exception) {
                emit(SyncResult.Progress("VOD categories failed, continuing...", 2, 6))
                ensureDefaultCategory("movie")
            }

            // Step 3: Sync Series Categories
            emit(SyncResult.Progress("Syncing Series categories...", 3, 6))
            try {
                val seriesCategories = apiService.getSeriesCategories(baseUrl, username, password)
                if (seriesCategories.isSuccessful) {
                    syncCategories(seriesCategories.body() ?: emptyList(), "series")
                }
                ensureDefaultCategory("series")
            } catch (e: Exception) {
                emit(SyncResult.Progress("Series categories failed, continuing...", 3, 6))
                ensureDefaultCategory("series")
            }

            // Step 4: Sync Live TV Streams
            emit(SyncResult.Progress("Syncing Live TV streams...", 4, 6))
            try {
                val liveStreams = apiService.getLiveStreams(baseUrl, username, password)
                if (liveStreams.isSuccessful) {
                    syncLiveStreams(liveStreams.body() ?: emptyList())
                }
            } catch (e: Exception) {
                emit(SyncResult.Progress("Live TV streams failed, continuing...", 4, 6))
            }

            // Step 5: Sync VOD Streams
            emit(SyncResult.Progress("Syncing VOD streams...", 5, 6))
            try {
                val vodStreams = apiService.getVodStreams(baseUrl, username, password)
                if (vodStreams.isSuccessful) {
                    syncVodStreams(vodStreams.body() ?: emptyList())
                }
            } catch (e: Exception) {
                emit(SyncResult.Progress("VOD streams failed, continuing...", 5, 6))
            }

            // Step 6: Sync Series Streams
            emit(SyncResult.Progress("Syncing Series streams...", 6, 6))
            try {
                val seriesStreams = apiService.getSeriesStreams(baseUrl, username, password)
                if (seriesStreams.isSuccessful) {
                    syncSeriesStreams(seriesStreams.body() ?: emptyList())
                }
            } catch (e: Exception) {
                emit(SyncResult.Progress("Series streams failed, continuing...", 6, 6))
            }

            emit(SyncResult.Success)

        } catch (e: Exception) {
            emit(SyncResult.Error("Sync failed: ${e.message}"))
        }
    }

    @VisibleForTesting
    internal suspend fun ensureDefaultCategory(type: String) {
        try {
            val existingCategory = database.categoryDao().getCategoryById(type, UNCATEGORIZED_ID)
            if (existingCategory == null) {
                val defaultCategory = CategoryEntity(
                    type = type,
                    id = UNCATEGORIZED_ID,
                    name = UNCATEGORIZED_NAME,
                    parent_id = 0
                )
                database.categoryDao().insertCategory(defaultCategory)
            }
        } catch (e: Exception) {
            // Log but don't fail sync
        }
    }

    private suspend fun syncCategories(categories: List<CategoryResponse>, type: String) {
        val entities = categories.mapNotNull { category ->
            try {
                // Skip if essential category fields are missing
                val categoryId = when {
                    category.categoryId.isNotBlank() -> category.categoryId.toIntSafely()
                    else -> null
                }

                val categoryName = when {
                    category.categoryName.isNotBlank() -> category.categoryName
                    else -> null // Skip if no category name
                }

                // Skip record if missing essential fields or if it's our default category ID
                if (categoryId == null || categoryId <= 0 || categoryName == null || categoryId == UNCATEGORIZED_ID) {
                    return@mapNotNull null
                }

                CategoryEntity(
                    type = type,
                    id = categoryId,
                    name = categoryName,
                    parent_id = category.parentId
                )
            } catch (e: Exception) {
                // Skip malformed categories - log could be added here
                null
            }
        }

        if (entities.isNotEmpty()) {
            // Don't delete all categories, only the ones we're updating
            // This preserves our default "Uncategorized" category
            val categoryIds = entities.map { it.id }
            for (entity in entities) {
                database.categoryDao().insertCategory(entity)
            }
        }
    }

    private suspend fun syncLiveStreams(streams: List<LiveTvResponse>) {
        val entities = streams.mapNotNull { stream ->
            try {
                // Skip if missing stream_id
                if (stream.streamId <= 0) return@mapNotNull null

                // Determine category - use default if none provided
                val categoryId = stream.categoryId.toIntSafely() ?:
                stream.categoryIds?.firstOrNull { it > 0 } ?:
                UNCATEGORIZED_ID

                // Handle missing name field
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
                // Skip malformed streams - log could be added here
                null
            }
        }

        if (entities.isNotEmpty()) {
            database.liveTvDao().deleteAllChannels()
            database.liveTvDao().insertChannels(entities)
        }
    }

    private suspend fun syncVodStreams(streams: List<VodResponse>) {
        val validStreams = streams.filter { it.streamId > 0 }

        val movieEntities = validStreams.mapNotNull { stream ->
            try {
                // Handle missing name field
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
                null
            }
        }

        val movieCategoryEntities = validStreams.flatMap { stream ->
            // Collect all valid category IDs from both fields
            val allCategoryIds = mutableListOf<Int>()

            // Add from category_id field
            stream.categoryId.toIntSafely()?.let { allCategoryIds.add(it) }

            // Add from category_ids array
            stream.categoryIds?.filter { it > 0 }?.let { allCategoryIds.addAll(it) }

            // If no valid categories found, use default
            if (allCategoryIds.isEmpty()) {
                allCategoryIds.add(UNCATEGORIZED_ID)
            }

            // Create entities for all unique valid categories
            allCategoryIds.distinct().mapNotNull { categoryId ->
                try {
                    MovieCategoryEntity(
                        movie_id = stream.streamId,
                        category_type = "movie",
                        category_id = categoryId
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }

        if (movieEntities.isNotEmpty()) {
            database.movieDao().deleteAllMovies()
            database.movieDao().insertMovies(movieEntities)
            if (movieCategoryEntities.isNotEmpty()) {
                database.movieDao().insertMovieCategories(movieCategoryEntities)
            }
        }
    }

    private suspend fun syncSeriesStreams(streams: List<SeriesResponse>) {
        val validStreams = streams.filter { it.seriesId > 0 }

        val seriesEntities = validStreams.mapNotNull { stream ->
            try {
                // Handle missing name field
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
                null
            }
        }

        val seriesCategoryEntities = validStreams.flatMap { stream ->
            // Collect all valid category IDs from both fields
            val allCategoryIds = mutableListOf<Int>()

            // Add from category_id field
            stream.categoryId.takeIfNotBlank()?.toIntSafely()?.let { allCategoryIds.add(it) }

            // Add from category_ids array
            stream.categoryIds?.filter { it > 0 }?.let { allCategoryIds.addAll(it) }

            // If no valid categories found, use default
            if (allCategoryIds.isEmpty()) {
                allCategoryIds.add(UNCATEGORIZED_ID)
            }

            // Create entities for all unique valid categories
            allCategoryIds.distinct().mapNotNull { categoryId ->
                try {
                    SeriesCategoryEntity(
                        series_id = stream.seriesId,
                        category_type = "series",
                        category_id = categoryId
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }

        if (seriesEntities.isNotEmpty()) {
            database.seriesDao().deleteAllSeries()
            database.seriesDao().insertSeriesList(seriesEntities)
            if (seriesCategoryEntities.isNotEmpty()) {
                database.seriesDao().insertSeriesCategories(seriesCategoryEntities)
            }
        }
    }
}