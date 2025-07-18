package com.supernova.network

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.Context
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import com.supernova.network.models.CategoryResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class DataSyncServiceTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var service: DataSyncService
    private lateinit var api: ApiService

    @Before
    fun setup() {
        val context = mockk<Context>(relaxed = true)
        every { context.applicationContext } returns context
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = DataSyncService(db)
        api = mockk()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun rb(json: String): ResponseBody =
        ResponseBody.create("application/json".toMediaTypeOrNull(), json)

    @Test
    fun syncMovies_success() = runTest {
        val cats = listOf(CategoryResponse("1", "Drama", 0))
        coEvery { api.getVodCategories(any(), any(), any(), any()) } returns retrofit2.Response.success(cats)
        val json = """[{"stream_id":1,"name":"Movie1","category_id":"1"}]"""
        coEvery { api.getVodStreams(any(), any(), any(), any()) } returns retrofit2.Response.success(rb(json))

        service.syncMovies(api, "base", "u", "p")

        assertEquals(1, db.movieDao().getMovieCount())
        assertEquals(2, db.categoryDao().getCategoryCount("movie"))
    }

    @Test
    fun syncSeries_success() = runTest {
        val cats = listOf(CategoryResponse("1", "Series", 0))
        coEvery { api.getSeriesCategories(any(), any(), any(), any()) } returns retrofit2.Response.success(cats)
        val json = """[{"series_id":1,"name":"Show1"}]"""
        coEvery { api.getSeriesStreams(any(), any(), any(), any()) } returns retrofit2.Response.success(rb(json))

        service.syncSeries(api, "base", "u", "p")

        assertEquals(1, db.seriesDao().getSeriesCount())
    }

    @Test
    fun syncTV_success() = runTest {
        val cats = listOf(CategoryResponse("1", "Live", 0))
        coEvery { api.getLiveCategories(any(), any(), any(), any()) } returns retrofit2.Response.success(cats)
        val json = """[{"stream_id":1,"name":"Ch1"}]"""
        coEvery { api.getLiveStreams(any(), any(), any(), any()) } returns retrofit2.Response.success(rb(json))

        service.syncTV(api, "base", "u", "p")

        assertEquals(1, db.liveTvDao().getChannelCount())
    }

    @Test
    fun syncMovies_malformedJson_rollback() = runTest {
        val cats = listOf(CategoryResponse("1", "Drama", 0))
        coEvery { api.getVodCategories(any(), any(), any(), any()) } returns retrofit2.Response.success(cats)
        coEvery { api.getVodStreams(any(), any(), any(), any()) } returns retrofit2.Response.success(rb("{bad"))

        assertFailsWith<Exception> { service.syncMovies(api, "base", "u", "p") }
        assertEquals(0, db.movieDao().getMovieCount())
        assertEquals(0, db.categoryDao().getCategoryCount("movie"))
    }

    @Test
    fun syncMovies_authFailure() = runTest {
        val errorBody = ResponseBody.create("text/plain".toMediaTypeOrNull(), "error")
        coEvery { api.getVodCategories(any(), any(), any(), any()) } returns retrofit2.Response.error(401, errorBody)

        assertFailsWith<Exception> { service.syncMovies(api, "base", "u", "p") }
    }

    @Test
    fun syncMovies_timeout() = runTest {
        val cats = emptyList<CategoryResponse>()
        coEvery { api.getVodCategories(any(), any(), any(), any()) } returns retrofit2.Response.success(cats)
        coEvery { api.getVodStreams(any(), any(), any(), any()) } throws java.net.SocketTimeoutException()

        assertFailsWith<java.net.SocketTimeoutException> { service.syncMovies(api, "base", "u", "p") }
    }
}
