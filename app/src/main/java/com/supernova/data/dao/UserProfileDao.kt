package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserProfileEntity): Long

    @Update
    suspend fun update(user: UserProfileEntity)

    @Delete
    suspend fun delete(user: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE userId = :id")
    suspend fun getById(id: Int): UserProfileEntity?

    @Query("SELECT * FROM user_profile ORDER BY userId ASC")
    fun getAll(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profile WHERE username = :username")
    suspend fun getByUsername(username: String): UserProfileEntity?
}
