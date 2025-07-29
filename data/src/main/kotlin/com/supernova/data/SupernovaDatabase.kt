package com.supernova.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.supernova.data.PlaceholderEntity

/**
 * Empty Room database to be populated in later tasks.
 */
@Database(version = 1, entities = [PlaceholderEntity::class], exportSchema = false)
abstract class SupernovaDatabase : RoomDatabase() {
    abstract fun baseDao(): BaseDao<PlaceholderEntity>
}
