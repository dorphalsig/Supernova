package com.supernova.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.supernova.data.dao.MovieDao
import com.supernova.data.dao.SeriesDao
import com.supernova.data.dao.EpgDao
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFailsWith
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class DatabaseRelationshipTest {

    private lateinit var db: SupernovaDatabase
    private lateinit var movieDao: MovieDao
    private lateinit var seriesDao: SeriesDao
    private lateinit var epgDao: EpgDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        movieDao = db.movieDao()
        seriesDao = db.seriesDao()
        epgDao = db.epgDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun series_backdropJson_persistsCorrectly() = runBlocking {
        val series = SeriesEntity(
            series_id = 1,
            num = 1,
            name = "Show",
            title = null,
            year = null,
            stream_type = null,
            cover = null,
            plot = null,
            cast = null,
            director = null,
            genre = null,
            release_date = null,
            releaseDate = null,
            last_modified = null,
            rating = null,
            rating_5based = null,
            backdrop_path = "[\"one.jpg\",\"two.jpg\"]",
            youtube_trailer = null,
            episode_run_time = null
        )

        seriesDao.insertSeries(series)
        val loaded = seriesDao.getSeriesById(1)
        assertNotNull(loaded)
        assertEquals(series.backdrop_path, loaded!!.backdrop_path)
    }

    @Test
    fun movieCategory_cascadeDeleteRemovesRelations() {
        runBlocking {
        val category = CategoryEntity("movie", 10, "Drama")
        val movie = MovieEntity(1, null, "Movie", null, null, null, null, null, null, null, null, null, null)
        db.categoryDao().insertCategory(category)
        movieDao.insertMovie(movie)
        val relation = MovieCategoryEntity(1, "movie", 10)
        movieDao.insertMovieCategory(relation)

        movieDao.deleteMovie(movie)
        val count = movieDao.getCategoriesForMovie(1).firstOrNull()?.size ?: 0
        assertEquals(0, count)
        }
    }

    @Test
    fun insertingMovieCategoryWithInvalidRefsFails() {
        runBlocking {
        val relation = MovieCategoryEntity(99, "movie", 99)
        assertFailsWith<Exception> {
            movieDao.insertMovieCategory(relation)
        }
        }
    }

    @Test
    fun seriesCategory_cascadeDeleteOnCategory() {
        runBlocking {
        val category = CategoryEntity("series", 5, "SciFi")
        val series = SeriesEntity(
            series_id = 2,
            num = 1,
            name = "Another",
            title = null,
            year = null,
            stream_type = null,
            cover = null,
            plot = null,
            cast = null,
            director = null,
            genre = null,
            release_date = null,
            releaseDate = null,
            last_modified = null,
            rating = null,
            rating_5based = null,
            backdrop_path = "[]",
            youtube_trailer = null,
            episode_run_time = null
        )
        db.categoryDao().insertCategory(category)
        seriesDao.insertSeries(series)
        val rel = SeriesCategoryEntity(2, "series", 5)
        seriesDao.insertSeriesCategory(rel)

        db.categoryDao().deleteCategory(category)
        val categories = seriesDao.getCategoriesForSeries(2).firstOrNull()
        assertTrue(categories.isNullOrEmpty())
        }
    }

    @Test
    fun epg_cascadeDeleteChannel() {
        runBlocking {
        val channel = ChannelEntity("ch1", "Channel")
        db.channelDao().insertChannels(listOf(channel))
        val entry = EpgEntity(channel_id = "ch1", start = 1L, end = 2L, title = "p", description = null)
        epgDao.insertPrograms(listOf(entry))

        db.channelDao().deleteAllChannels()
        val programs = epgDao.getProgramsForChannel("ch1").firstOrNull()
        assertTrue(programs.isNullOrEmpty())
        }
    }

    @Test
    fun epg_validTimeRangeStored() {
        runBlocking {
        val channel = ChannelEntity("ch2", "Channel")
        db.channelDao().insertChannels(listOf(channel))
        val entry = EpgEntity(channel_id = "ch2", start = 100L, end = 200L, title = "a", description = null)
        epgDao.insertPrograms(listOf(entry))
        val stored = epgDao.getProgramsForChannel("ch2").firstOrNull()
        assertNotNull(stored)
        assertTrue(stored!!.first().epg.start < stored.first().epg.end)
        }
    }

    @Test
    fun epg_invalidTimeRangeRejected() {
        runBlocking {
        val channel = ChannelEntity("ch3", "Channel")
        db.channelDao().insertChannels(listOf(channel))
        val entry = EpgEntity(channel_id = "ch3", start = 200L, end = 100L, title = "bad", description = null)
        assertFailsWith<Exception> {
            epgDao.insertPrograms(listOf(entry))
        }
        }
    }
}
