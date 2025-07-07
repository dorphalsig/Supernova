package com.supernova.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.supernova.data.dao.ProfileDao
import com.supernova.data.entities.ProfileEntity

@Database(
    entities = [ProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SupernovaDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

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