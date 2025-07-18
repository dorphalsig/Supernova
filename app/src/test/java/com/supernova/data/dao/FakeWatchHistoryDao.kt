package com.supernova.data.dao

import com.supernova.data.entities.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory implementation of [WatchHistoryDao] used for unit tests.
 */
class FakeWatchHistoryDao : WatchHistoryDao {
    /** Stored history items exposed as a flow. */
    private val items = MutableStateFlow<List<WatchHistoryEntity>>(emptyList())

    /**
     * Insert a new [WatchHistoryEntity] and return the generated id.
     */
    override suspend fun insert(history: WatchHistoryEntity): Long {
        items.value = items.value + history.copy(historyId = items.value.size + 1)
        return items.value.size.toLong()
    }

    /**
     * Update an existing [WatchHistoryEntity].
     * This fake implementation performs no-op.
     */
    override suspend fun update(history: WatchHistoryEntity) {}

    /**
     * Delete a [WatchHistoryEntity] from the store.
     * This fake implementation performs no-op.
     */
    override suspend fun delete(history: WatchHistoryEntity) {}

    /**
     * Return an item by its id or null if not found.
     */
    override suspend fun getById(id: Int): WatchHistoryEntity? =
        items.value.find { it.historyId == id }

    /**
     * Stream all history items sorted by insertion order.
     */
    override fun getAll(): Flow<List<WatchHistoryEntity>> = items.asStateFlow()

    /**
     * Stream history items for a specific user.
     */
    override fun getByUser(userId: Int): Flow<List<WatchHistoryEntity>> = items.asStateFlow()

    /**
     * Stream "continue watching" items for a user.
     */
    override fun getContinueWatching(userId: Int): Flow<List<WatchHistoryEntity>> = items.asStateFlow()

    /**
     * Return up to ten most recently watched items for the given user.
     */
    override suspend fun getRecent(userId: Int): List<WatchHistoryEntity> {
        return items.value
            .filter { it.userId == userId }
            .sortedByDescending { it.watchedAt }
            .take(10)
    }
}
