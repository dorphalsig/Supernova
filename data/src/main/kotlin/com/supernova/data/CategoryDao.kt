package com.supernova.data

import androidx.room.Dao
import androidx.room.Query

/** Placeholder DAO for categories. */
@Dao
interface CategoryDao {
    @Query("SELECT 1")
    suspend fun noop(): Int
}
