package com.supernova.data

import androidx.room.Dao
import androidx.room.Query

/** Placeholder DAO for programs. */
@Dao
interface ProgramDao {
    @Query("SELECT 1")
    suspend fun noop(): Int
}
