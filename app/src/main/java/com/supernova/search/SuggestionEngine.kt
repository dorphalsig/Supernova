package com.supernova.search

import com.supernova.data.dao.ReactionDao
import com.supernova.data.dao.WatchHistoryDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SuggestionEngine @Inject constructor(
    private val historyDao: WatchHistoryDao,
    private val reactionDao: ReactionDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val cacheDurationMs = 60 * 60 * 1000L
    private var lastUpdated = 0L
    private var cached: Map<Int, Int> = emptyMap()

    suspend fun suggestions(userId: Int): List<Int> = withContext(dispatcher) {
        val now = System.currentTimeMillis()
        if (now - lastUpdated < cacheDurationMs && cached.isNotEmpty()) {
            return@withContext cached.toList().sortedByDescending { it.value }.map { it.first }
        }
        val history = historyDao.getByUser(userId).first()
        val reactions = reactionDao.getByUser(userId)
        val counts = mutableMapOf<Int, Int>()
        history.forEach { it.streamId?.let { id -> counts[id] = counts.getOrDefault(id, 0) + 1 } }
        reactions.forEach { it.streamId?.let { id -> counts[id] = counts.getOrDefault(id, 0) + 3 } }
        lastUpdated = now
        cached = counts
        counts.toList().sortedByDescending { it.second }.map { it.first }
    }

    fun invalidate() { lastUpdated = 0L }
}
