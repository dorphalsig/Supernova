package com.supernova.di

import com.supernova.data.dao.CategoryDao
import com.supernova.data.dao.RecommendationDao
import com.supernova.data.dao.StreamDao
import com.supernova.data.dao.WatchHistoryDao
import com.supernova.home.ContentRailGenerator
import com.supernova.home.HomeRepository
import com.supernova.network.tmdb.TmdbService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideContentRailGenerator(
        streamDao: StreamDao,
        categoryDao: CategoryDao,
        recommendationDao: RecommendationDao,
        watchHistoryDao: WatchHistoryDao,
        tmdbService: TmdbService
    ) = ContentRailGenerator(streamDao, categoryDao, recommendationDao, watchHistoryDao, tmdbService)

    @Provides
    @Singleton
    fun provideHomeRepository(
        streamDao: StreamDao,
        categoryDao: CategoryDao,
        recommendationDao: RecommendationDao,
        watchHistoryDao: WatchHistoryDao,
        tmdbService: TmdbService
    ): HomeRepository = HomeRepository(
        streamDao,
        categoryDao,
        recommendationDao,
        watchHistoryDao,
        ContentRailGenerator(streamDao, categoryDao, recommendationDao, watchHistoryDao, tmdbService)
    )
}
