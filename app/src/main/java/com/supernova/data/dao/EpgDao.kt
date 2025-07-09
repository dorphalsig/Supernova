package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.EpgEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgDao {
    @Query("SELECT * FROM epg WHERE channel_id = :channelId ORDER BY start ASC")
    fun getProgramsForChannel(channelId: Int): Flow<List<EpgEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgEntity>)

    @Query("DELETE FROM epg")
    suspend fun deleteAllPrograms()

    @Query("DELETE FROM epg WHERE channel_id = :channelId")
    suspend fun deleteProgramsForChannel(channelId: Int)
}
