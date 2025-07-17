package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.ReactionEntity

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reaction: ReactionEntity): Long

    @Query("SELECT * FROM reaction WHERE userId = :userId")
    suspend fun getByUser(userId: Int): List<ReactionEntity>
}
