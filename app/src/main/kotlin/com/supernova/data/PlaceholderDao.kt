package com.supernova.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaceholderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PlaceholderEntity)

    @Query("SELECT * FROM placeholder WHERE id = :id")
    suspend fun get(id: Int): PlaceholderEntity?
}
