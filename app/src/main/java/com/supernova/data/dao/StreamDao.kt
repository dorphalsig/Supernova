package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.StreamEntity
import com.supernova.data.entities.StreamFts
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamDao {

    @Query("SELECT * FROM stream ORDER BY title ASC")
    fun getAllStreams(): Flow<List<StreamEntity>>

    @Query("SELECT * FROM stream WHERE stream_id = :id")
    suspend fun getStreamById(id: Int): StreamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: StreamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreams(streams: List<StreamEntity>)

    @Update
    suspend fun updateStream(stream: StreamEntity)

    @Delete
    suspend fun deleteStream(stream: StreamEntity)

    @Query("DELETE FROM stream WHERE stream_id = :id")
    suspend fun deleteStreamById(id: Int)

    // FTS operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreamFts(fts: StreamFts)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreamFtsList(ftsList: List<StreamFts>)

    @Query(
        """
        SELECT s.* FROM stream s
        JOIN stream_fts fts ON s.stream_id = fts.rowid
        WHERE stream_fts MATCH :query
        ORDER BY s.title ASC
        """
    )
    fun searchStreams(query: String): Flow<List<StreamEntity>>
}
