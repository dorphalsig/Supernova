package com.supernova.data.dao

import com.supernova.data.entities.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWatchHistoryDao : WatchHistoryDao {
    private val items = MutableStateFlow<List<WatchHistoryEntity>>(emptyList())

    override suspend fun insert(history: WatchHistoryEntity): Long {
        items.value = items.value + history.copy(historyId = items.value.size + 1)
        return items.value.size.toLong()
    }

    override suspend fun update(history: WatchHistoryEntity) {}

    override suspend fun delete(history: WatchHistoryEntity) {}

    override suspend fun getById(id: Int): WatchHistoryEntity? = items.value.find { it.historyId == id }

    override fun getAll(): Flow<List<WatchHistoryEntity>> = items.asStateFlow()

    override fun getByUser(userId: Int): Flow<List<WatchHistoryEntity>> = items.asStateFlow()

    override fun getContinueWatching(userId: Int): Flow<List<WatchHistoryEntity>> = items.asStateFlow()
}
