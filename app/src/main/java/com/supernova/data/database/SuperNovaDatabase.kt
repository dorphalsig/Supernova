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
import com.supernova.data.entities.ProfileEntity
import com.supernova.data.entities.CategoryEntity
import com.supernova.data.entities.MovieEntity
import com.supernova.data.entities.MovieCategoryEntity
import com.supernova.data.entities.LiveTvEntity
import com.supernova.data.entities.SeriesEntity
import com.supernova.data.entities.SeriesCategoryEntity
import com.supernova.data.entities.EpgEntity
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
        EpgEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SupernovaDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun categoryDao(): CategoryDao
    abstract fun movieDao(): MovieDao
    abstract fun liveTvDao(): LiveTvDao
    abstract fun seriesDao(): SeriesDao
    abstract fun epgDao(): EpgDao

    companion object {
        @Volatile
        private var INSTANCE: SupernovaDatabase? = null

        // Migration from version 2 to 3 - change avatar from BLOB to TEXT
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table with avatar as TEXT
                database.execSQL("""
                    CREATE TABLE profiles_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        pin INTEGER,
                        avatar TEXT NOT NULL
                    )
                """.trimIndent())

                // Get current timestamp for generating new avatar URLs
                val currentTime = System.currentTimeMillis()

                // Copy existing data and generate new avatar URLs
                database.execSQL("""
                    INSERT INTO profiles_new (id, name, pin, avatar)
                    SELECT id, name, pin, 'https://api.dicebear.com/7.x/bottts/png?backgroundColor=191b22,23253a&radius=50&seed=' || (${currentTime} + id)
                    FROM profiles
                """.trimIndent())

                // Drop old table
                database.execSQL("DROP TABLE profiles")

                // Rename new table
                database.execSQL("ALTER TABLE profiles_new RENAME TO profiles")
            }
        }

        // Migration from version 3 to 4 - add EPG table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS epg (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            channel_id INTEGER NOT NULL,
                            channel_name TEXT,
                            start INTEGER NOT NULL,
                            end INTEGER NOT NULL,
                            title TEXT,
                            description TEXT,
                            FOREIGN KEY(channel_id) REFERENCES live_tv(channel_id) ON DELETE CASCADE
                        )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_epg_start ON epg(start)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_epg_end ON epg(end)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_epg_channel_id ON epg(channel_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_epg_channel_name ON epg(channel_name)")
            }
        }

        fun getDatabase(context: Context): SupernovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SupernovaDatabase::class.java,
                    "supernova"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }}