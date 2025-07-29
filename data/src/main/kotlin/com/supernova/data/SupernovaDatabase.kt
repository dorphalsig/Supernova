package com.supernova.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.supernova.data.CategoryEntity
import com.supernova.data.StreamEntity
import com.supernova.data.ProgramEntity

/**
 * Empty Room database to be populated in later tasks.
 */
@Database(
    version = 1,
    entities = [CategoryEntity::class, StreamEntity::class, ProgramEntity::class],
    exportSchema = false
)
abstract class SupernovaDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun streamDao(): StreamDao
    abstract fun programDao(): ProgramDao
}
