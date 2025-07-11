package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.EpgEntity
import com.supernova.data.entities.EpgWithChannel
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgDao {
    @Query("""
        SELECT e.*, c.name FROM epg e
        INNER JOIN channel c ON e.channel_id = c.channel_id
        WHERE e.channel_id = :channelId
        ORDER BY e.start ASC
    """)
    fun getProgramsForChannel(channelId: String): Flow<List<EpgWithChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgEntity>)

    @Query("DELETE FROM epg")
    suspend fun deleteAllPrograms()

    @Query("DELETE FROM epg WHERE channel_id = :channelId")
    suspend fun deleteProgramsForChannel(channelId: String)
}
