package com.supernova.home

import com.supernova.data.dao.StreamDao
import com.supernova.data.dao.CategoryDao
import com.supernova.data.dao.RecommendationDao
import com.supernova.data.dao.WatchHistoryDao
import com.supernova.network.tmdb.TmdbService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
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
            val cwStreams = mutableListOf<com.supernova.data.entities.StreamEntity>()
            for (entry in cw) {
                val id = entry.streamId ?: continue
                val stream = streamDao.getStreamById(id)
                if (stream != null) cwStreams += stream
            }
            if (cwStreams.isNotEmpty()) {
                rails += ContentRail("Continue Watching", cwStreams)
            }
        }

        val trendingMoviesApi = tmdbService.trendingMovies(apiKey).results
        val trendingMovies = mutableListOf<com.supernova.data.entities.StreamEntity>()
        for (item in trendingMoviesApi) {
            val stream = streamDao.getStreamByTmdb(item.id)
            if (stream != null) trendingMovies += stream
        }
        if (trendingMovies.isNotEmpty()) {
            rails += ContentRail("Trending Movies", trendingMovies)
        }

        val trendingSeriesApi = tmdbService.trendingSeries(apiKey).results
        val trendingSeries = mutableListOf<com.supernova.data.entities.StreamEntity>()
        for (item in trendingSeriesApi) {
            val stream = streamDao.getStreamByTmdb(item.id)
            if (stream != null) trendingSeries += stream
        }
        if (trendingSeries.isNotEmpty()) {
            rails += ContentRail("Trending Series", trendingSeries)
        }

        val recs = recommendationDao.getRecommendations(userId)
        val recEntities = recs.firstOrNull() ?: emptyList()
        if (recEntities.isNotEmpty()) {
            val recStreams = mutableListOf<com.supernova.data.entities.StreamEntity>()
            for (rec in recEntities) {
                val stream = streamDao.getStreamById(rec.streamId)
                if (stream != null) recStreams += stream
            }
            if (recStreams.isNotEmpty()) {
                rails += ContentRail("Recommended For You", recStreams)
            }
        }

        rails
    }
}
