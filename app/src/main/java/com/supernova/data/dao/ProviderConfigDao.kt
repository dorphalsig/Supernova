package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.ProviderConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderConfigDao {
    @Query("SELECT * FROM provider_configs WHERE id = :id")
    suspend fun getById(id: Long): ProviderConfigEntity?

    @Query("SELECT * FROM provider_configs ORDER BY id ASC")
    fun getAll(): Flow<List<ProviderConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ProviderConfigEntity): Long

    @Update
    suspend fun update(config: ProviderConfigEntity)

    @Delete
    suspend fun delete(config: ProviderConfigEntity)

    @Query("DELETE FROM provider_configs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
