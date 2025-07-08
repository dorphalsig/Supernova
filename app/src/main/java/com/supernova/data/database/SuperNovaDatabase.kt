package com.supernova.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.supernova.data.dao.ProfileDao
import com.supernova.data.dao.CategoryDao
import com.supernova.data.dao.MovieDao
import com.supernova.data.dao.LiveTvDao
import com.supernova.data.dao.SeriesDao
import com.supernova.data.entities.ProfileEntity
import com.supernova.data.entities.CategoryEntity
import com.supernova.data.entities.MovieEntity
import com.supernova.data.entities.MovieCategoryEntity
import com.supernova.data.entities.LiveTvEntity
import com.supernova.data.entities.SeriesEntity
import com.supernova.data.entities.SeriesCategoryEntity

@Database(
    entities = [
        ProfileEntity::class,
        CategoryEntity::class,
        MovieEntity::class,
        MovieCategoryEntity::class,
        LiveTvEntity::class,
        SeriesEntity::class,
        SeriesCategoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SupernovaDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun movieDao(): MovieDao
    abstract fun liveTvDao(): LiveTvDao
    abstract fun seriesDao(): SeriesDao

    companion object {
        @Volatile
        private var INSTANCE: SupernovaDatabase? = null

        fun getDatabase(context: Context): SupernovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SupernovaDatabase::class.java,
                    "supernova"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}