package com.supernova.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.Context
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.dao.MovieDao
import com.supernova.data.entities.ContentDetailEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDaoTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var dao: MovieDao

    @Before
    fun setup() {
        val context = mockk<Context>(relaxed = true)
        every { context.applicationContext } returns context
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.movieDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertMovieAndRetrieveById() = runTest {
        val movie = TestEntityFactory.movie(id = 1)
        dao.insertMovie(movie)
        val loaded = dao.getMovieById(1)
        assertEquals(movie.name, loaded?.name)
    }

    @Test
    fun insertMovieWithNullMetadata() = runTest {
        val movie = TestEntityFactory.movie(id = 2, streamType = null)
        dao.insertMovie(movie)
        val loaded = dao.getMovieById(2)
        assertNull(loaded?.stream_type)
    }

    @Test
    fun getMovieWithDetails_returnsRelation() = runTest {
        val movie = TestEntityFactory.movie(id = 3)
        dao.insertMovie(movie)
        db.contentDetailDao().insertDetail(
            ContentDetailEntity(3, "movie", tagline = "tag", status = null, homepage = null, genres = null)
        )
        val loaded = dao.getMovieWithDetails(3)
        assertEquals("tag", loaded?.details?.tagline)
    }

    @Test
    fun searchByGenre_filtersCorrectly() = runTest {
        val movie = TestEntityFactory.movie(id = 4).copy(
            genres = "Action,Drama",
            backdrop_path = null,
            poster_path = null,
            overview = null,
            runtime = null,
            spoken_languages = null
        )
        dao.insertMovie(movie)
        val results = dao.searchByGenre("Action").first()
        assertEquals(1, results.size)
    }
}
