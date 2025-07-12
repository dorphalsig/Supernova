package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.LiveTvEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveTvDao {

    @Query("SELECT * FROM live_tv WHERE is_live = 1 ORDER BY name ASC")
    fun getAllChannels(): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE channel_id = :channelId")
    suspend fun getChannelById(channelId: Int): LiveTvEntity?

    @Query("""
        SELECT * FROM live_tv 
        WHERE category_type = :categoryType AND category_id = :categoryId AND is_live = 1
        ORDER BY name ASC
    """)
    fun getChannelsByCategory(categoryType: String, categoryId: Int): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE is_live = 1 AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchChannels(searchQuery: String): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE is_live = 1 AND tv_archive = 1 ORDER BY name ASC")
    fun getChannelsWithArchive(): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE epg_channel_id = :epgChannelId AND is_live = 1")
    suspend fun getChannelByEpgId(epgChannelId: String): LiveTvEntity?

    @Query("SELECT * FROM live_tv WHERE is_live = 1 ORDER BY added DESC LIMIT :limit")
    fun getRecentlyAddedChannels(limit: Int): Flow<List<LiveTvEntity>>

    @Query("SELECT * FROM live_tv WHERE is_live = 1 ORDER BY num ASC")
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

    // --- DML versioning helpers ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannelsStaging(channels: List<LiveTvEntity>)

    @Query(
        "INSERT INTO live_tv(channel_id, num, name, stream_type, stream_icon, epg_channel_id, added, custom_sid, tv_archive, direct_source, tv_archive_duration, category_type, category_id, thumbnail, is_live) " +
            "SELECT channel_id, num, name, stream_type, stream_icon, epg_channel_id, added, custom_sid, tv_archive, direct_source, tv_archive_duration, category_type, category_id, thumbnail, 0 FROM live_tv " +
            "WHERE is_live = 1 AND (:categoryId IS NULL OR category_id = :categoryId)"
    )
    suspend fun copyFromLive(categoryId: Int?)

    @Query("DELETE FROM live_tv WHERE category_id = :categoryId AND is_live = 0")
    suspend fun deleteStagingByCategory(categoryId: Int)

    @Query("DELETE FROM live_tv WHERE is_live = 0")
    suspend fun deleteStaging()

    @Query("DELETE FROM live_tv WHERE is_live = 1")
    suspend fun deleteLive()

    @Query("UPDATE live_tv SET is_live = 1 WHERE is_live = 0")
    suspend fun promoteStaging()

    @Transaction
    suspend fun swapStagingToLive() {
        deleteLive()
        promoteStaging()
    }
}