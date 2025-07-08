package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.MovieEntity
import com.supernova.data.entities.MovieCategoryEntity
import com.supernova.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movie ORDER BY name ASC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE movie_id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query("""
        SELECT m.* FROM movie m
        INNER JOIN movie_category mc ON m.movie_id = mc.movie_id
        WHERE mc.category_type = :categoryType AND mc.category_id = :categoryId
        ORDER BY m.name ASC
    """)
    fun getMoviesByCategory(categoryType: String, categoryId: Int): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchMovies(searchQuery: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie WHERE year = :year ORDER BY name ASC")
    fun getMoviesByYear(year: Int): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie ORDER BY added DESC LIMIT :limit")
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

    @Transaction
    suspend fun insertMovieWithCategories(movie: MovieEntity, categories: List<MovieCategoryEntity>) {
        insertMovie(movie)
        deleteMovieCategoriesByMovieId(movie.movie_id)
        insertMovieCategories(categories)
    }
}