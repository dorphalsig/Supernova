package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.SeriesEntity
import com.supernova.data.entities.SeriesCategoryEntity
import com.supernova.data.entities.CategoryEntity
import com.supernova.data.entities.SeriesWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {

    @Query("SELECT * FROM series ORDER BY name ASC")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE series_id = :seriesId")
    suspend fun getSeriesById(seriesId: Int): SeriesEntity?

    @Query("""
        SELECT s.* FROM series s
        INNER JOIN series_category sc ON s.series_id = sc.series_id
        WHERE sc.category_type = :categoryType AND sc.category_id = :categoryId
        ORDER BY s.name ASC
    """)
    fun getSeriesByCategory(categoryType: String, categoryId: Int): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE name LIKE '%' || :searchQuery || '%' OR title LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchSeries(searchQuery: String): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE year = :year ORDER BY name ASC")
    fun getSeriesByYear(year: String): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE genre LIKE '%' || :genre || '%' ORDER BY name ASC")
    fun getSeriesByGenre(genre: String): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series ORDER BY last_modified DESC LIMIT :limit")
    fun getRecentlyUpdatedSeries(limit: Int): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE rating_5based >= :minRating ORDER BY rating_5based DESC")
    fun getTopRatedSeries(minRating: Float): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesList(seriesList: List<SeriesEntity>)

    @Update
    suspend fun updateSeries(series: SeriesEntity)

    @Delete
    suspend fun deleteSeries(series: SeriesEntity)

    @Query("DELETE FROM series WHERE series_id = :seriesId")
    suspend fun deleteSeriesById(seriesId: Int)

    @Query("DELETE FROM series")
    suspend fun deleteAllSeries()

    @Query("SELECT COUNT(*) FROM series")
    suspend fun getSeriesCount(): Int

    @Transaction
    @Query("SELECT * FROM series WHERE series_id = :seriesId")
    suspend fun getSeriesWithDetails(seriesId: Int): SeriesWithDetails?

    @Query("SELECT * FROM series WHERE genres LIKE '%' || :genre || '%' ORDER BY name ASC")
    fun getByGenre(genre: String): Flow<List<SeriesEntity>>

    @Query("SELECT COUNT(*) FROM series WHERE series_id IN (SELECT series_id FROM series_category WHERE category_type = :categoryType AND category_id = :categoryId)")
    suspend fun getSeriesCountByCategory(categoryType: String, categoryId: Int): Int

    // Series-Category relationship operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesCategory(seriesCategory: SeriesCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesCategories(seriesCategories: List<SeriesCategoryEntity>)

    @Query("DELETE FROM series_category WHERE series_id = :seriesId")
    suspend fun deleteSeriesCategoriesBySeriesId(seriesId: Int)

    @Query("""
        SELECT c.* FROM category c
        INNER JOIN series_category sc ON c.type = sc.category_type AND c.id = sc.category_id
        WHERE sc.series_id = :seriesId
        ORDER BY c.name ASC
    """)
    fun getCategoriesForSeries(seriesId: Int): Flow<List<CategoryEntity>>

    @Transaction
    suspend fun insertSeriesWithCategories(series: SeriesEntity, categories: List<SeriesCategoryEntity>) {
        insertSeries(series)
        deleteSeriesCategoriesBySeriesId(series.series_id)
        insertSeriesCategories(categories)
    }
}