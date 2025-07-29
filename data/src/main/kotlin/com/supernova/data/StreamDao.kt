package com.supernova.data

import androidx.room.Dao
import androidx.room.Query

/** Placeholder DAO for streams. */
@Dao
interface StreamDao {
    @Query("SELECT 1")
    suspend fun noop(): Int
}
