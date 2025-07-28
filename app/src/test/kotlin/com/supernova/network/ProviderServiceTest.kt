package com.supernova.network

import com.supernova.testing.BaseSyncTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private object MockWebServerExtensions {
    fun MockWebServer.enqueueJson(
        json: String,
        code: Int = 200
    ) {
        enqueue(MockResponse().setResponseCode(code).setBody(json))
    }
}

class ProviderServiceTest : BaseSyncTest() {
    private lateinit var service: ProviderService
    private lateinit var fixture: Map<String, JsonElement>

    @BeforeEach
    override fun setUp() {
        super.setUp()
        service = retrofit.create(ProviderService::class.java)
    }

    @Test
    fun `get categories parses response`() = runTest {
        fixture = loadAsMap("provider_api_test.json")
        val json = fixture["categories"].toString()
        with(MockWebServerExtensions) { server.enqueueJson(json) }

        val result = service.getCategories(username = "u", password = "p")

        assertEquals(1, result.size)
        assertEquals("News", result.first().category_name)
        assertRequest("/player_api.php?action=get_live_categories&username=u&password=p")
    }

    @Test
    fun `get streams parses response`() = runTest {
        fixture = loadAsMap("provider_api_test.json")
        val json = fixture["streams"].toString()
        with(MockWebServerExtensions) { server.enqueueJson(json) }

        val result = service.getStreams(categoryId = 1, username = "u", password = "p")

        assertEquals(1, result.size)
        assertEquals(10, result.first().stream_id)
        assertRequest("/player_api.php?action=get_live_streams&category_id=1&username=u&password=p")
    }

    @Test
    fun `get programs parses response`() = runTest {
        fixture = loadAsMap("provider_api_test.json")
        val json = fixture["programs"].toString()
        with(MockWebServerExtensions) { server.enqueueJson(json) }

        val result = service.getPrograms(streamId = 10, username = "u", password = "p")

        assertEquals(1, result.size)
        assertEquals("Morning News", result.first().title)
        assertRequest("/player_api.php?action=get_short_epg&stream_id=10&limit=1&username=u&password=p")
    }
}
