package com.supernova.testing

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonFixtureLoaderTest {
    private val loader = JsonFixtureLoader()

    @Test
    fun `loadRaw caches fixture`() = runTest {
        val first = loader.loadRaw("sample_response.json")
        val second = loader.loadRaw("sample_response.json")
        assertEquals(first, second)
        assertEquals(1, loader.cache.size)
    }

    @Test
    fun `loadAsMap parses json`() = runTest {
        val map = loader.loadAsMap("sample_response.json")
        val status = map["status"]
        assertTrue(status is JsonPrimitive)
        assertEquals("ok", (status as JsonPrimitive).content)
        assertTrue(loader.validateFixture("sample_response.json", setOf("status", "results")))
    }

    @Test
    fun `missing fixture throws`() = runTest {
        assertFailsWith<IllegalArgumentException> { loader.loadRaw("missing.json") }
    }

    @Test
    fun `invalid json fails validation`() = runTest {
        assertFalse(loader.validateJson("{ invalid", setOf("status")))
    }
}
