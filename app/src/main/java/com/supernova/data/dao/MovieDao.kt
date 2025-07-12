package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.MovieEntity
import com.supernova.data.entities.MovieCategoryEntity
import com.supernova.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movie WHERE is_live = 1 ORDER BY name ASC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE movie_id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query("""
        SELECT m.* FROM movie m
        INNER JOIN movie_category mc ON m.movie_id = mc.movie_id
        WHERE mc.category_type = :categoryType AND mc.category_id = :categoryId AND m.is_live = 1 AND mc.is_live = 1
        ORDER BY m.name ASC
    """)
    fun getMoviesByCategory(categoryType: String, categoryId: Int): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE is_live = 1 AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchMovies(searchQuery: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE is_live = 1 AND year = :year ORDER BY name ASC")
    fun getMoviesByYear(year: Int): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE is_live = 1 ORDER BY added DESC LIMIT :limit")
    fun getRecentlyAddedMovies(limit: Int): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("DELETE FROM movie WHERE movie_id = :movieId")
    suspend fun deleteMovieById(movieId: Int)

    @Query("DELETE FROM movie")
    suspend fun deleteAllMovies()

    // Movie-Category relationship operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieCategory(movieCategory: MovieCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieCategories(movieCategories: List<MovieCategoryEntity>)

    @Query("DELETE FROM movie_category WHERE movie_id = :movieId")
    suspend fun deleteMovieCategoriesByMovieId(movieId: Int)

    @Query("""
        SELECT c.* FROM category c
        INNER JOIN movie_category mc ON c.type = mc.category_type AND c.id = mc.category_id
        WHERE mc.movie_id = :movieId
        ORDER BY c.name ASC
    """)
    fun getCategoriesForMovie(movieId: Int): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM movie")
    suspend fun getMovieCount(): Int

    // --- DML versioning helpers ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesStaging(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieCategoriesStaging(categories: List<MovieCategoryEntity>)

    @Query(
        "INSERT INTO movie(movie_id, num, name, title, year, stream_type, stream_icon, rating, rating_5based, added, container_extension, custom_sid, direct_source, is_live) " +
            "SELECT movie_id, num, name, title, year, stream_type, stream_icon, rating, rating_5based, added, container_extension, custom_sid, direct_source, 0 FROM movie " +
            "WHERE is_live = 1 AND (:categoryId IS NULL OR movie_id IN (SELECT movie_id FROM movie_category WHERE category_id = :categoryId AND category_type = 'movie' AND is_live = 1))"
    )
    suspend fun copyFromLive(categoryId: Int?)

    @Query(
        "INSERT INTO movie_category(movie_id, category_type, category_id, is_live) " +
            "SELECT movie_id, category_type, category_id, 0 FROM movie_category " +
            "WHERE is_live = 1 AND (:categoryId IS NULL OR category_id = :categoryId)"
    )
    suspend fun copyCategoriesFromLive(categoryId: Int?)

    @Query("DELETE FROM movie WHERE is_live = 0")
    suspend fun deleteStagingMovies()

    @Query("DELETE FROM movie_category WHERE is_live = 0")
    suspend fun deleteStagingCategories()

    @Query("DELETE FROM movie WHERE is_live = 1")
    suspend fun deleteLiveMovies()

    @Query("DELETE FROM movie_category WHERE is_live = 1")
    suspend fun deleteLiveMovieCategories()

    @Query("UPDATE movie SET is_live = 1 WHERE is_live = 0")
    suspend fun promoteStagingMovies()

    @Query("UPDATE movie_category SET is_live = 1 WHERE is_live = 0")
    suspend fun promoteStagingCategories()

    @Transaction
    suspend fun deleteStaging() {
        deleteStagingMovies()
        deleteStagingCategories()
    }

    @Transaction
    suspend fun swapStagingToLive() {
        deleteLiveMovies()
        deleteLiveMovieCategories()
        promoteStagingMovies()
        promoteStagingCategories()
    }

    @Transaction
    suspend fun insertMovieWithCategories(movie: MovieEntity, categories: List<MovieCategoryEntity>) {
        insertMovie(movie)
        deleteMovieCategoriesByMovieId(movie.movie_id)
        insertMovieCategories(categories)
    }
}