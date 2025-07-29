package com.supernova.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** DAO for program lookups. */
@Dao
interface ProgramDao {
    @Query(
        """
            SELECT * FROM program
            WHERE epg_channel_id = :streamId
              AND start <= :now AND end > :now
            ORDER BY start DESC
            LIMIT 1
        """
    )
    suspend fun nowPlaying(streamId: Int, now: Long): ProgramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ProgramEntity)
}
