package com.supernova.network

import com.supernova.testing.BaseSyncTest
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class XcApiServiceTest : BaseSyncTest() {
    private lateinit var service: XcApiService

    @BeforeEach
    override fun setUp() {
        super.setUp()
        okHttpClient = OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(15))
            .build()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        service = retrofit.create(XcApiService::class.java)
    }

    @Test
    fun `get live streams success`() = runTest {
        val json = loadJsonFixture("live_streams.json")
        enqueueJsonResponse(json)

        val result = service.getLiveStreams()

        assertEquals(2, result.size)
        assertEquals("News Channel", result[0].title)
        assertRequest("/live/streams")
    }

    @Test
    fun `get categories success`() = runTest {
        val json = loadJsonFixture("categories.json")
        enqueueJsonResponse(json)

        val result = service.getCategories()

        assertEquals(2, result.size)
        assertEquals("News", result[0].name)
        assertRequest("/categories")
    }

    @Test
    fun `http error`() = runTest {
        enqueueJsonResponse("{}", code = 404)
        val response = runCatching { service.getLiveStreams() }
        assertTrue(response.isFailure)
    }

    @Test
    fun `network failure`() = runTest {
        enqueueNetworkFailure()
        assertFailsWith<Exception> { service.getLiveStreams() }
    }
}
