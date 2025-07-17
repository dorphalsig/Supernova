package com.supernova.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.dao.SeriesDao
import com.supernova.data.entities.ContentDetailEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SeriesDaoTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var dao: SeriesDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.seriesDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getSeriesWithDetails_returnsRelation() = runTest {
        val series = TestEntityFactory.series(id = 1)
        dao.insertSeries(series)
        db.contentDetailDao().insertDetail(ContentDetailEntity(1, "tv", tagline = "hello", status = null, homepage = null, genres = null))
        val loaded = dao.getSeriesWithDetails(1)
        assertEquals("hello", loaded?.details?.tagline)
    }

    @Test
    fun getByGenre_filtersCorrectly() = runTest {
        val series = TestEntityFactory.series(id = 2).copy(
            genres = "Drama",
            poster_path = null,
            overview = null,
            first_air_date = null,
            last_air_date = null,
            number_of_seasons = null,
            number_of_episodes = null
        )
        dao.insertSeries(series)
        val results = dao.getByGenre("Drama").first()
        assertEquals(1, results.size)
    }
}
