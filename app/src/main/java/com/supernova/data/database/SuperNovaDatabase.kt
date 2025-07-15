package com.supernova.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.supernova.data.dao.ProfileDao
import com.supernova.data.dao.CategoryDao
import com.supernova.data.dao.MovieDao
import com.supernova.data.dao.LiveTvDao
import com.supernova.data.dao.SeriesDao
import com.supernova.data.dao.EpgDao
import com.supernova.data.dao.ChannelDao
import com.supernova.data.dao.ProviderConfigDao
import com.supernova.data.entities.ProfileEntity
import com.supernova.data.entities.CategoryEntity
import com.supernova.data.entities.MovieEntity
import com.supernova.data.entities.MovieCategoryEntity
import com.supernova.data.entities.LiveTvEntity
import com.supernova.data.entities.SeriesEntity
import com.supernova.data.entities.SeriesCategoryEntity
import com.supernova.data.entities.EpgEntity
import com.supernova.data.entities.ChannelEntity
import com.supernova.data.entities.ProviderConfigEntity
import com.supernova.network.AvatarService

@Database(
    entities = [
        ProfileEntity::class,
        CategoryEntity::class,
        MovieEntity::class,
        MovieCategoryEntity::class,
        LiveTvEntity::class,
        SeriesEntity::class,
        SeriesCategoryEntity::class,
        ChannelEntity::class,
        EpgEntity::class,
        ProviderConfigEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class SupernovaDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun movieDao(): MovieDao
    abstract fun liveTvDao(): LiveTvDao
    abstract fun seriesDao(): SeriesDao
    abstract fun channelDao(): ChannelDao
    abstract fun epgDao(): EpgDao
    abstract fun providerConfigDao(): ProviderConfigDao

    companion object {
        @Volatile
        private var INSTANCE: SupernovaDatabase? = null

        fun getDatabase(context: Context): SupernovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SupernovaDatabase::class.java,
                    "supernova"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }}