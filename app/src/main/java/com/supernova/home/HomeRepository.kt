package com.supernova.home

import com.supernova.data.dao.CategoryDao
import com.supernova.data.dao.RecommendationDao
import com.supernova.data.dao.StreamDao
import com.supernova.data.dao.WatchHistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val streamDao: StreamDao,
    private val categoryDao: CategoryDao,
    private val recommendationDao: RecommendationDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val generator: ContentRailGenerator
) {
    private val cacheDurationMs = 30 * 60_000L
    private var cacheTime = 0L
    private var cache: List<ContentRail>? = null

    suspend fun loadHome(userId: Int): List<ContentRail> {
        val now = System.currentTimeMillis()
        val cached = cache
        if (cached != null && now - cacheTime < cacheDurationMs) return cached

        val rails = generator.generate(userId)
        cache = rails
        cacheTime = now
        return rails
    }

    fun observeHome(userId: Int): Flow<HomeUiState> = flow {
        emit(HomeUiState.Loading)
        try {
            val data = loadHome(userId)
            emit(HomeUiState.Success(data))
        } catch (e: Exception) {
            emit(HomeUiState.Error(e.message ?: "Unknown error"))
        }
    }
}
