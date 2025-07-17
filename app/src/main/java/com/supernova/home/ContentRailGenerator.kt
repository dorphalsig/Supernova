package com.supernova.home

import com.supernova.data.dao.StreamDao
import com.supernova.data.dao.CategoryDao
import com.supernova.data.dao.RecommendationDao
import com.supernova.data.dao.WatchHistoryDao
import com.supernova.network.tmdb.TmdbService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContentRailGenerator @Inject constructor(
    private val streamDao: StreamDao,
    private val categoryDao: CategoryDao,
    private val recommendationDao: RecommendationDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val tmdbService: TmdbService
) {
    private val apiKey = "demo" // placeholder

    suspend fun generate(userId: Int): List<ContentRail> = withContext(Dispatchers.IO) {
        val rails = mutableListOf<ContentRail>()

        val cw = watchHistoryDao.getRecent(userId)
        if (cw.isNotEmpty()) {
            rails += ContentRail("Continue Watching", cw.mapNotNull { it.streamId?.let(streamDao::getStreamById) })
        }

        val trendingMovies = tmdbService.trendingMovies(apiKey).results.mapNotNull { streamDao.getStreamByTmdb(it.id) }
        if (trendingMovies.isNotEmpty()) {
            rails += ContentRail("Trending Movies", trendingMovies)
        }

        val trendingSeries = tmdbService.trendingSeries(apiKey).results.mapNotNull { streamDao.getStreamByTmdb(it.id) }
        if (trendingSeries.isNotEmpty()) {
            rails += ContentRail("Trending Series", trendingSeries)
        }

        val recs = recommendationDao.getRecommendations(userId)
        val recEntities = recs.firstOrNull() ?: emptyList()
        if (recEntities.isNotEmpty()) {
            rails += ContentRail("Recommended For You", recEntities.mapNotNull { streamDao.getStreamById(it.streamId) })
        }

        rails
    }
}
