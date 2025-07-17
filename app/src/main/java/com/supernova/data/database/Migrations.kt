package com.supernova.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE movie ADD COLUMN backdrop_path TEXT")
        db.execSQL("ALTER TABLE movie ADD COLUMN poster_path TEXT")
        db.execSQL("ALTER TABLE movie ADD COLUMN overview TEXT")
        db.execSQL("ALTER TABLE movie ADD COLUMN genres TEXT")
        db.execSQL("ALTER TABLE movie ADD COLUMN runtime INTEGER")
        db.execSQL("ALTER TABLE movie ADD COLUMN spoken_languages TEXT")

        db.execSQL("ALTER TABLE series ADD COLUMN poster_path TEXT")
        db.execSQL("ALTER TABLE series ADD COLUMN overview TEXT")
        db.execSQL("ALTER TABLE series ADD COLUMN genres TEXT")
        db.execSQL("ALTER TABLE series ADD COLUMN first_air_date TEXT")
        db.execSQL("ALTER TABLE series ADD COLUMN last_air_date TEXT")
        db.execSQL("ALTER TABLE series ADD COLUMN number_of_seasons INTEGER")
        db.execSQL("ALTER TABLE series ADD COLUMN number_of_episodes INTEGER")

        db.execSQL("CREATE TABLE IF NOT EXISTS content_detail (tmdb_id INTEGER PRIMARY KEY NOT NULL, media_type TEXT NOT NULL, tagline TEXT, status TEXT, homepage TEXT, genres TEXT)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_content_detail_media_type ON content_detail(media_type)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_content_detail_tmdb_id ON content_detail(tmdb_id)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS recommendation (recId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId INTEGER NOT NULL, streamId INTEGER NOT NULL, recoAt INTEGER NOT NULL, score REAL, source TEXT, moodId INTEGER)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_recommendation_userId ON recommendation(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_recommendation_streamId ON recommendation(streamId)")
    }
}
