package com.supernova

import com.supernova.di.NetworkModule
import com.supernova.network.XcApiService
import com.supernova.testing.BaseSyncTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NetworkModuleTest : BaseSyncTest() {
    private lateinit var service: XcApiService

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val client = NetworkModule.provideOkHttpClient()
        val moshi = NetworkModule.provideMoshi()
        val retrofit = NetworkModule.provideRetrofit(client, moshi).newBuilder()
            .baseUrl(server.url("/"))
            .build()
        service = NetworkModule.provideXcApiService(retrofit)
    }

    @Test
    fun `network service returns data`() = runTest {
        val json = loadJsonFixture("live_streams.json")
        enqueueJsonResponse(json)

        val result = service.getLiveStreams()

        assertEquals(2, result.size)
        assertRequest("/live/streams")
    }
}
