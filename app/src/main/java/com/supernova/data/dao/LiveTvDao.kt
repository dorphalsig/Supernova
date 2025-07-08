package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.LiveTvEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveTvDao {

    @Query("SELECT * FROM live_tv ORDER BY name ASC")
    fun getAllChannels(): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE channel_id = :channelId")
    suspend fun getChannelById(channelId: Int): LiveTvEntity?

    @Query("""
        SELECT * FROM live_tv 
        WHERE category_type = :categoryType AND category_id = :categoryId
        ORDER BY name ASC
    """)
    fun getChannelsByCategory(categoryType: String, categoryId: Int): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchChannels(searchQuery: String): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE tv_archive = 1 ORDER BY name ASC")
    fun getChannelsWithArchive(): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE epg_channel_id = :epgChannelId")
    suspend fun getChannelByEpgId(epgChannelId: String): LiveTvEntity?

    @Query("SELECT * FROM live_tv ORDER BY added DESC LIMIT :limit")
    fun getRecentlyAddedChannels(limit: Int): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv ORDER BY num ASC")
    fun getChannelsOrderedByNumber(): Flow<List<LiveTvEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: LiveTvEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<LiveTvEntity>)

    @Update
    suspend fun updateChannel(channel: LiveTvEntity)

    @Delete
    suspend fun deleteChannel(channel: LiveTvEntity)

    @Query("DELETE FROM live_tv WHERE channel_id = :channelId")
    suspend fun deleteChannelById(channelId: Int)

    @Query("DELETE FROM live_tv")
    suspend fun deleteAllChannels()

    @Query("SELECT COUNT(*) FROM live_tv")
    suspend fun getChannelCount(): Int

    @Query("SELECT COUNT(*) FROM live_tv WHERE category_type = :categoryType AND category_id = :categoryId")
    suspend fun getChannelCountByCategory(categoryType: String, categoryId: Int): Int
}