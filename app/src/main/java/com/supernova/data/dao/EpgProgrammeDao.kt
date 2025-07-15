package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.EpgProgrammeEntity
import com.supernova.data.entities.EpgProgrammeFts
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgProgrammeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(programme: EpgProgrammeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programmes: List<EpgProgrammeEntity>)

    @Update
    suspend fun update(programme: EpgProgrammeEntity)

    @Delete
    suspend fun delete(programme: EpgProgrammeEntity)

    @Query("DELETE FROM epg_programme WHERE programmeId = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM epg_programme WHERE programmeId = :id")
    suspend fun getById(id: Int): EpgProgrammeEntity?

    @Query("SELECT * FROM epg_programme ORDER BY startAt")
    fun getAll(): Flow<List<EpgProgrammeEntity>>

    @Query(
        """
            SELECT * FROM epg_programme
            WHERE epgChannelId = :channelId
            AND startAt >= :startAt AND endAt <= :endAt
            ORDER BY startAt
        """
    )
    fun getProgrammesForChannelInRange(
        channelId: String,
        startAt: Long,
        endAt: Long
    ): Flow<List<EpgProgrammeEntity>>

    @Query(
        """
            SELECT epg_programme.* FROM epg_programme
            JOIN epg_programme_fts ON epg_programme.rowid = epg_programme_fts.rowid
            WHERE epg_programme_fts MATCH :query
        """
    )
    fun searchProgrammes(query: String): Flow<List<EpgProgrammeEntity>>
}
