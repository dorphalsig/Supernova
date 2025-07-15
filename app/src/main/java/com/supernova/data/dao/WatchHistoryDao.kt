package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WatchHistoryEntity): Long

    @Update
    suspend fun update(history: WatchHistoryEntity)

    @Delete
    suspend fun delete(history: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE historyId = :id")
    suspend fun getById(id: Int): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAll(): Flow<List<WatchHistoryEntity>>

    @Query(
        "SELECT * FROM watch_history WHERE userId = :userId ORDER BY watchedAt DESC"
    )
    fun getByUser(userId: Int): Flow<List<WatchHistoryEntity>>

    @Query(
        "SELECT * FROM watch_history WHERE userId = :userId AND progress > 0.05 ORDER BY watchedAt DESC"
    )
    fun getContinueWatching(userId: Int): Flow<List<WatchHistoryEntity>>
}
