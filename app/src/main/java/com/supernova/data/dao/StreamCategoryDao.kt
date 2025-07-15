package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.StreamCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamCategoryDao {

    @Query("SELECT * FROM stream_category")
    fun getAll(): Flow<List<StreamCategoryEntity>>

    @Query("SELECT * FROM stream_category WHERE streamId = :streamId AND categoryId = :categoryId")
    suspend fun getById(streamId: Int, categoryId: Int): StreamCategoryEntity?

    @Query("SELECT * FROM stream_category WHERE streamId = :streamId")
    fun getByStreamId(streamId: Int): Flow<List<StreamCategoryEntity>>

    @Query("SELECT * FROM stream_category WHERE categoryId = :categoryId")
    fun getByCategoryId(categoryId: Int): Flow<List<StreamCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StreamCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<StreamCategoryEntity>)

    @Update
    suspend fun update(entity: StreamCategoryEntity)

    @Delete
    suspend fun delete(entity: StreamCategoryEntity)

    @Query("DELETE FROM stream_category WHERE streamId = :streamId AND categoryId = :categoryId")
    suspend fun deleteById(streamId: Int, categoryId: Int)
}
