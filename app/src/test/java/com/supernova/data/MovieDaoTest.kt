package com.supernova.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.dao.MovieDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MovieDaoTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var dao: MovieDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
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
}
