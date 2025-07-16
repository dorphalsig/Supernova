package com.supernova.network

import SyncResult
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.supernova.data.database.SupernovaDatabase
import androidx.room.withTransaction
import com.supernova.data.entities.*
import com.supernova.network.models.*
import com.supernova.utils.ApiUtils
import com.supernova.utils.ApiUtils.normalizeUrl
import com.supernova.utils.ApiUtils.parseTimestamp
import com.supernova.utils.ApiUtils.takeIfNotBlank
import com.supernova.utils.ApiUtils.toIntSafely
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DataSyncService(
    private val database: SupernovaDatabase,
    private val gson: Gson = Gson()
) {
    companion object {
        private const val TAG = "DataSyncService"
        private const val UNCATEGORIZED_ID = 999999
        private const val UNCATEGORIZED_NAME = "Uncategorized"

    }

    // In DataSyncService.kt


    suspend fun syncTV(
        apiService: ApiService,
        baseUrl: String,
        username: String,
        password: String
    ) {
        val catResp = apiService.getLiveCategories(baseUrl, username, password)
        if (!catResp.isSuccessful) throw Exception("Live category API ${catResp.code()}")
        val categories = mapCategories(catResp.body() ?: emptyList(), "live_tv")

        val response = apiService.getLiveStreams(baseUrl, username, password)
        if (!response.isSuccessful) throw Exception("Live stream API ${response.code()}")
        val responseBody = response.body()!!

        database.withTransaction {
            database.categoryDao().deleteCategoriesByType("live_tv")
            database.categoryDao().insertCategories(categories)
            database.categoryDao()
                .insertCategory(CategoryEntity("live_tv", UNCATEGORIZED_ID, UNCATEGORIZED_NAME))
            database.liveTvDao().deleteAllChannels()
            // Streaming: map and insert each batch
            ApiUtils.batchJsonStream<LiveTvResponse>(this, responseBody) { batch ->
                val entities = mapLiveStreams(batch)
                database.liveTvDao().insertChannels(entities) // Insert this batch
            }
        }
    }

    suspend fun syncSeries(
        apiService: ApiService,
        baseUrl: String,
        username: String,
        password: String
    ) {
        val catResp = apiService.getSeriesCategories(baseUrl, username, password)
        if (!catResp.isSuccessful) throw Exception("Series category API ${catResp.code()}")
        val categories = mapCategories(catResp.body() ?: emptyList(), "series")

        val streamResp = apiService.getSeriesStreams(baseUrl, username, password)
        if (!streamResp.isSuccessful) throw Exception("Series stream API ${streamResp.code()}")
        val responseBody = streamResp.body()!!

        database.withTransaction {
            database.categoryDao().deleteCategoriesByType("series")
            database.categoryDao().insertCategories(categories)
            database.categoryDao()
                .insertCategory(CategoryEntity("series", UNCATEGORIZED_ID, UNCATEGORIZED_NAME))
            database.seriesDao().deleteAllSeries()
            ApiUtils.batchJsonStream<SeriesResponse>(this, responseBody) { batch ->
                val (series, seriesCats) = mapSeriesStreams(batch)
                database.seriesDao().insertSeriesList(series)
                if (seriesCats.isNotEmpty()) database.seriesDao().insertSeriesCategories(seriesCats)
            }
        }
    }

    suspend fun syncMovies(
        apiService: ApiService,
        baseUrl: String,
        username: String,
        password: String
    ) {
        val catResp = apiService.getVodCategories(baseUrl, username, password)
        if (!catResp.isSuccessful) throw Exception("VOD category API ${catResp.code()}")
        val categories = mapCategories(catResp.body() ?: emptyList(), "movie")

        val streamResp = apiService.getVodStreams(baseUrl, username, password)
        if (!streamResp.isSuccessful) throw Exception("VOD stream API ${streamResp.code()}")
        val responseBody = streamResp.body()!!

        database.withTransaction {
            database.categoryDao().deleteCategoriesByType("movie")
            database.categoryDao().insertCategories(categories)
            database.categoryDao()
                .insertCategory(CategoryEntity("movie", UNCATEGORIZED_ID, UNCATEGORIZED_NAME))
            database.movieDao().deleteAllMovies()
            ApiUtils.batchJsonStream<SeriesResponse>(this, responseBody) { batch ->
                val (movies, moviesCats) = mapSeriesStreams(batch)
                database.seriesDao().insertSeriesList(movies)
                if (moviesCats.isNotEmpty()) database.seriesDao().insertSeriesCategories(moviesCats)
            }
        }
    }

    suspend fun syncEPG(
        apiService: ApiService,
        portal: String,
        username: String,
        password: String
    ) {
        val epgUrl = "$portal/xmltv.php"
        val resp = apiService.downloadEpg(epgUrl, username, password)
        if (!resp.isSuccessful) throw Exception("EPG download failed ${resp.code()}")
        val body = resp.body() ?: throw Exception("Empty EPG body")
        database.withTransaction {
            database.epgDao().deleteAllPrograms()
            database.channelDao().deleteAllChannels()
            ApiUtils.batchXmlStream(
                this, body,
                insertChannels = { database.channelDao().insertChannels(it) },
                insertPrograms = { database.epgDao().insertPrograms(it) })
        }
    }


    fun syncAll(): Flow<SyncResult> = flow {
        Log.d(TAG, "Starting sync process")

        val portal = SecureDataStore.getString(SecureStorageKeys.PORTAL)
        val username = SecureDataStore.getString(SecureStorageKeys.USERNAME)
        val password = SecureDataStore.getString(SecureStorageKeys.PASSWORD)

        if (portal == null || username == null || password == null) {
            Log.e(
                TAG,
                "No credentials found - portal: $portal, username: $username, password: ${
                    password?.let {
                        "*".repeat(it.length)
                    }
                }"
            )
            emit(SyncResult.Error("No credentials found"))
            return@flow
        }

        Log.d(TAG, "Using credentials - portal: $portal, username: $username")

        try {
            val apiService = ApiService.create(portal)
            val baseUrl = ApiService.buildLoginUrl(portal)
            Log.d(TAG, "Created API service with base URL: $baseUrl")
            emit(SyncResult.Progress("Starting sync...", 0, 7))

            emit(SyncResult.Progress("Syncing live TV...", 1, 7))
            try {
                syncTV(apiService, baseUrl, username, password)
            } catch (e: Exception) {
                emit(SyncResult.Error("Live TV sync failed: ${e.message}"))
                return@flow
            }

            emit(SyncResult.Progress("Syncing movies...", 2, 7))
            try {
                syncMovies(apiService, baseUrl, username, password)
            } catch (e: Exception) {
                emit(SyncResult.Error("VOD sync failed: ${e.message}"))
                return@flow
            }

            emit(SyncResult.Progress("Syncing series...", 3, 7))
            try {
                syncSeries(apiService, baseUrl, username, password)
            } catch (e: Exception) {
                emit(SyncResult.Error("Series sync failed: ${e.message}"))
                return@flow
            }

            emit(SyncResult.Progress("Syncing EPG...", 4, 7))
            try {
                syncEPG(apiService, portal, username, password)
            } catch (e: Exception) {
                emit(SyncResult.Error("EPG sync failed: ${e.message}"))
                return@flow
            }

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

    private fun mapCategories(
        categories: List<CategoryResponse>,
        type: String
    ): List<CategoryEntity> {
        Log.d(TAG, "Mapping ${categories.size} categories for type: $type")

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
                Log.e(
                    TAG,
                    "Failed to parse category: ${category.categoryId} - ${category.categoryName}",
                    e
                )
                null
            }
        }

        Log.d(TAG, "Parsed ${entities.size} valid categories for type: $type")
        return entities
    }

    private fun mapLiveStreams(streams: List<LiveTvResponse>): List<LiveTvEntity> {
        Log.d(TAG, "Mapping ${streams.size} live TV streams")

        val entities = streams.mapNotNull { stream ->
            try {
                if (stream.streamId <= 0) {
                    Log.d(TAG, "Skipping stream with invalid ID: ${stream.streamId}")
                    return@mapNotNull null
                }

                val categoryId =
                    stream.categoryId.toIntSafely() ?: stream.categoryIds?.firstOrNull { it > 0 }
                    ?: UNCATEGORIZED_ID

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
        return entities
    }

    private fun mapVodStreams(streams: List<VodResponse>): Pair<List<MovieEntity>, List<MovieCategoryEntity>> {
        Log.d(TAG, "Mapping ${streams.size} VOD streams")

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
                    Log.e(
                        TAG,
                        "Failed to create movie category entity: ${stream.streamId} - $categoryId",
                        e
                    )
                    null
                }
            }
        }

        Log.d(
            TAG,
            "Parsed ${movieEntities.size} movies and ${movieCategoryEntities.size} movie-category relations"
        )
        return movieEntities to movieCategoryEntities
    }

    private fun mapSeriesStreams(streams: List<SeriesResponse>): Pair<List<SeriesEntity>, List<SeriesCategoryEntity>> {
        Log.d(TAG, "Mapping ${streams.size} series streams")

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
                        try {
                            gson.toJson(it)
                        } catch (e: Exception) {
                            null
                        }
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
                    Log.e(
                        TAG,
                        "Failed to create series category entity: ${stream.seriesId} - $categoryId",
                        e
                    )
                    null
                }
            }
        }

        Log.d(
            TAG,
            "Parsed ${seriesEntities.size} series and ${seriesCategoryEntities.size} series-category relations"
        )
        return seriesEntities to seriesCategoryEntities
    }


}