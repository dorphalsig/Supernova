package com.supernova.testing

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.http.GET
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BaseSyncTestTest : BaseSyncTest() {
    private interface EchoService {
        @GET("echo")
        suspend fun echo(): Echo
    }

    private data class Echo(val message: String)

    private lateinit var service: EchoService

    @BeforeEach
    override fun setUp() {
        super.setUp()
        service = retrofit.create(EchoService::class.java)
    }

    @Test
    fun `successful json response`() = runTest() {
        val json = loadJsonFixture("echo.json")
        enqueueJsonResponse(json)

        val result = service.echo()

        assertEquals("hello", result.message)
        assertRequest("/echo")
    }

    @Test
    fun `http error response`() = runTest() {
        enqueueJsonResponse("{}", code = 404)
        val response = runCatching { service.echo() }
        assertTrue(response.isFailure)
    }

    @Test
    fun `network failure`() = runTest() {
        enqueueNetworkFailure()
        assertFailsWith<IOException> { service.echo() }
    }
}
