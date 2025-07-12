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
import com.supernova.data.entities.ProfileEntity
import com.supernova.data.entities.CategoryEntity
import com.supernova.data.entities.MovieEntity
import com.supernova.data.entities.MovieCategoryEntity
import com.supernova.data.entities.LiveTvEntity
import com.supernova.data.entities.SeriesEntity
import com.supernova.data.entities.SeriesCategoryEntity
import com.supernova.data.entities.EpgEntity
import com.supernova.data.entities.ChannelEntity
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
        EpgEntity::class
    ],
    version = 7,
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
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE category ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE movie ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE live_tv ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE series ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE movie_category ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE series_category ADD COLUMN is_live INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE category_new (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, id INTEGER NOT NULL, name TEXT NOT NULL, parent_id INTEGER NOT NULL, is_live INTEGER NOT NULL DEFAULT 1)")
                database.execSQL("INSERT INTO category_new (type, id, name, parent_id, is_live) SELECT type, id, name, parent_id, is_live FROM category")
                database.execSQL("DROP TABLE category")
                database.execSQL("ALTER TABLE category_new RENAME TO category")
                database.execSQL("CREATE UNIQUE INDEX index_category_type_id_is_live ON category(type,id,is_live)")
                database.execSQL("CREATE INDEX index_category_type ON category(type)")
                database.execSQL("CREATE INDEX index_category_is_live ON category(is_live)")
                database.execSQL("CREATE INDEX index_category_type_is_live ON category(type,is_live)")

                database.execSQL("CREATE TABLE movie_new (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, movie_id INTEGER NOT NULL, num INTEGER, name TEXT NOT NULL, title TEXT, year INTEGER, stream_type TEXT, stream_icon TEXT, rating REAL, rating_5based REAL, added INTEGER, container_extension TEXT, custom_sid TEXT, direct_source TEXT, is_live INTEGER NOT NULL DEFAULT 1)")
                database.execSQL("INSERT INTO movie_new (movie_id, num, name, title, year, stream_type, stream_icon, rating, rating_5based, added, container_extension, custom_sid, direct_source, is_live) SELECT movie_id, num, name, title, year, stream_type, stream_icon, rating, rating_5based, added, container_extension, custom_sid, direct_source, is_live FROM movie")
                database.execSQL("DROP TABLE movie")
                database.execSQL("ALTER TABLE movie_new RENAME TO movie")
                database.execSQL("CREATE UNIQUE INDEX index_movie_movie_id_is_live ON movie(movie_id,is_live)")
                database.execSQL("CREATE INDEX index_movie_movie_id ON movie(movie_id)")
                database.execSQL("CREATE INDEX index_movie_is_live ON movie(is_live)")
                database.execSQL("CREATE INDEX index_movie_name ON movie(name)")
                database.execSQL("CREATE INDEX index_movie_year ON movie(year)")
                database.execSQL("CREATE INDEX index_movie_added ON movie(added)")

                database.execSQL("CREATE TABLE live_tv_new (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, channel_id INTEGER NOT NULL, num INTEGER, name TEXT NOT NULL, stream_type TEXT, stream_icon TEXT, epg_channel_id TEXT, added INTEGER, custom_sid TEXT, tv_archive INTEGER, direct_source TEXT, tv_archive_duration INTEGER, category_type TEXT, category_id INTEGER, thumbnail TEXT, is_live INTEGER NOT NULL DEFAULT 1)")
                database.execSQL("INSERT INTO live_tv_new (channel_id, num, name, stream_type, stream_icon, epg_channel_id, added, custom_sid, tv_archive, direct_source, tv_archive_duration, category_type, category_id, thumbnail, is_live) SELECT channel_id, num, name, stream_type, stream_icon, epg_channel_id, added, custom_sid, tv_archive, direct_source, tv_archive_duration, category_type, category_id, thumbnail, is_live FROM live_tv")
                database.execSQL("DROP TABLE live_tv")
                database.execSQL("ALTER TABLE live_tv_new RENAME TO live_tv")
                database.execSQL("CREATE UNIQUE INDEX index_live_tv_channel_id_is_live ON live_tv(channel_id,is_live)")
                database.execSQL("CREATE INDEX index_live_tv_channel_id ON live_tv(channel_id)")
                database.execSQL("CREATE INDEX index_live_tv_is_live ON live_tv(is_live)")
                database.execSQL("CREATE INDEX index_live_tv_name ON live_tv(name)")
                database.execSQL("CREATE INDEX index_live_tv_category_id ON live_tv(category_id)")
                database.execSQL("CREATE INDEX index_live_tv_category_type_category_id ON live_tv(category_type,category_id)")

                database.execSQL("CREATE TABLE series_new (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, series_id INTEGER NOT NULL, num INTEGER, name TEXT NOT NULL, title TEXT, year TEXT, stream_type TEXT, cover TEXT, plot TEXT, cast TEXT, director TEXT, genre TEXT, release_date TEXT, releaseDate TEXT, last_modified TEXT, rating TEXT, rating_5based REAL, backdrop_path TEXT, youtube_trailer TEXT, episode_run_time TEXT, is_live INTEGER NOT NULL DEFAULT 1)")
                database.execSQL("INSERT INTO series_new (series_id, num, name, title, year, stream_type, cover, plot, cast, director, genre, release_date, releaseDate, last_modified, rating, rating_5based, backdrop_path, youtube_trailer, episode_run_time, is_live) SELECT series_id, num, name, title, year, stream_type, cover, plot, cast, director, genre, release_date, releaseDate, last_modified, rating, rating_5based, backdrop_path, youtube_trailer, episode_run_time, is_live FROM series")
                database.execSQL("DROP TABLE series")
                database.execSQL("ALTER TABLE series_new RENAME TO series")
                database.execSQL("CREATE UNIQUE INDEX index_series_series_id_is_live ON series(series_id,is_live)")
                database.execSQL("CREATE INDEX index_series_series_id ON series(series_id)")
                database.execSQL("CREATE INDEX index_series_is_live ON series(is_live)")
                database.execSQL("CREATE INDEX index_series_name ON series(name)")
                database.execSQL("CREATE INDEX index_series_year ON series(year)")
                database.execSQL("CREATE INDEX index_series_genre ON series(genre)")

                database.execSQL("CREATE TABLE movie_category_new (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, movie_id INTEGER NOT NULL, category_type TEXT NOT NULL, category_id INTEGER NOT NULL, is_live INTEGER NOT NULL DEFAULT 1)")
                database.execSQL("INSERT INTO movie_category_new (movie_id, category_type, category_id, is_live) SELECT movie_id, category_type, category_id, is_live FROM movie_category")
                database.execSQL("DROP TABLE movie_category")
                database.execSQL("ALTER TABLE movie_category_new RENAME TO movie_category")
                database.execSQL("CREATE UNIQUE INDEX index_movie_category_unique ON movie_category(movie_id,category_type,category_id,is_live)")
                database.execSQL("CREATE INDEX index_movie_category_movie_id ON movie_category(movie_id)")
                database.execSQL("CREATE INDEX index_movie_category_category_type_category_id ON movie_category(category_type,category_id)")
                database.execSQL("CREATE INDEX index_movie_category_is_live ON movie_category(is_live)")
                database.execSQL("CREATE INDEX index_movie_category_movie_id_is_live ON movie_category(movie_id,is_live)")

                database.execSQL("CREATE TABLE series_category_new (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, series_id INTEGER NOT NULL, category_type TEXT NOT NULL, category_id INTEGER NOT NULL, is_live INTEGER NOT NULL DEFAULT 1)")
                database.execSQL("INSERT INTO series_category_new (series_id, category_type, category_id, is_live) SELECT series_id, category_type, category_id, is_live FROM series_category")
                database.execSQL("DROP TABLE series_category")
                database.execSQL("ALTER TABLE series_category_new RENAME TO series_category")
                database.execSQL("CREATE UNIQUE INDEX index_series_category_unique ON series_category(series_id,category_type,category_id,is_live)")
                database.execSQL("CREATE INDEX index_series_category_series_id ON series_category(series_id)")
                database.execSQL("CREATE INDEX index_series_category_category_type_category_id ON series_category(category_type,category_id)")
                database.execSQL("CREATE INDEX index_series_category_is_live ON series_category(is_live)")
                database.execSQL("CREATE INDEX index_series_category_series_id_is_live ON series_category(series_id,is_live)")
            }
        }
    }
