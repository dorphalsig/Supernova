package com.supernova.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.supernova.data.entities.RecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendation WHERE userId = :userId ORDER BY score DESC, recoAt DESC")
    fun getRecommendations(userId: Int): Flow<List<RecommendationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<RecommendationEntity>)

    @Query("DELETE FROM recommendation WHERE userId = :userId")
    suspend fun deleteForUser(userId: Int)
}
