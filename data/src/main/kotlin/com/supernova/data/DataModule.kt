package com.supernova.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module exposing the [SupernovaDatabase].
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SupernovaDatabase {
        return Room.databaseBuilder(
            context,
            SupernovaDatabase::class.java,
            "supernova.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
