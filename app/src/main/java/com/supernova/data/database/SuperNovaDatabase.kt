package com.supernova.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.supernova.data.dao.CategoryDao
import com.supernova.data.dao.ChannelDao
import com.supernova.data.dao.EpgDao
import com.supernova.data.dao.LiveTvDao
import com.supernova.data.dao.MovieDao
import com.supernova.data.dao.ProfileDao
import com.supernova.data.dao.ProviderConfigDao
import com.supernova.data.dao.SearchDao
import com.supernova.data.dao.StreamDao
import com.supernova.data.dao.SeriesDao
import com.supernova.data.dao.EpgProgrammeDao
import com.supernova.data.dao.WatchHistoryDao
import com.supernova.data.dao.RecommendationDao
import com.supernova.data.dao.ContentDetailDao
import com.supernova.data.database.MIGRATION_7_8
import com.supernova.data.database.MIGRATION_8_9
import com.supernova.data.entities.CategoryEntity
import com.supernova.data.entities.ChannelEntity
import com.supernova.data.entities.EpgEntity
import com.supernova.data.entities.EpgProgrammeEntity
import com.supernova.data.entities.EpgProgrammeFts
import com.supernova.data.entities.SearchQueryEntity
import com.supernova.data.entities.LiveTvEntity
import com.supernova.data.entities.MovieCategoryEntity
import com.supernova.data.entities.MovieEntity
import com.supernova.data.entities.ContentDetailEntity
import com.supernova.data.entities.RecommendationEntity
import com.supernova.data.entities.ProfileEntity
import com.supernova.data.entities.ProviderConfigEntity
import com.supernova.data.entities.SeriesCategoryEntity
import com.supernova.data.entities.SeriesEntity
import com.supernova.data.entities.StreamEntity
import com.supernova.data.entities.StreamFts
import com.supernova.data.entities.UserProfileEntity
import com.supernova.data.entities.WatchHistoryEntity
import com.supernova.data.entities.StreamFts
import com.supernova.data.entities.StreamEntity

@Database(
    entities = [
        ProfileEntity::class,
        CategoryEntity::class,
        MovieEntity::class,
        MovieCategoryEntity::class,
        LiveTvEntity::class,
        StreamEntity::class,
        SeriesEntity::class,
        SeriesCategoryEntity::class,
        ChannelEntity::class,
        EpgEntity::class,
        WatchHistoryEntity::class,
        EpgProgrammeEntity::class,
        EpgProgrammeFts::class,
        StreamFts::class,
        SearchQueryEntity::class,
        ProviderConfigEntity::class,
        UserProfileEntity::class,
        ContentDetailEntity::class,
        RecommendationEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class SupernovaDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun movieDao(): MovieDao
    abstract fun liveTvDao(): LiveTvDao
    abstract fun streamDao(): StreamDao
    abstract fun seriesDao(): SeriesDao
    abstract fun channelDao(): ChannelDao
    abstract fun epgDao(): EpgDao
    abstract fun providerConfigDao(): ProviderConfigDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun searchDao(): SearchDao
    abstract fun epgProgrammeDao(): EpgProgrammeDao
    abstract fun contentDetailDao(): ContentDetailDao

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
                    .addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
