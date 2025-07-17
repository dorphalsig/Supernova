package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.ContentDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDetailDao {
    @Query("SELECT * FROM content_detail WHERE tmdb_id = :id")
    suspend fun getDetail(id: Int): ContentDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: ContentDetailEntity)

    @Query("SELECT * FROM content_detail WHERE media_type = :mediaType AND genres LIKE '%' || :genre || '%'")
    fun recommendationsByGenre(mediaType: String, genre: String): Flow<List<ContentDetailEntity>>
}
