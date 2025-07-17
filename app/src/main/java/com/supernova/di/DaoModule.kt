package com.supernova.di

import com.supernova.data.database.SupernovaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides fun provideStreamDao(db: SupernovaDatabase) = db.streamDao()
    @Provides fun provideCategoryDao(db: SupernovaDatabase) = db.categoryDao()
    @Provides fun provideRecommendationDao(db: SupernovaDatabase) = db.recommendationDao()
    @Provides fun provideWatchHistoryDao(db: SupernovaDatabase) = db.watchHistoryDao()
}
