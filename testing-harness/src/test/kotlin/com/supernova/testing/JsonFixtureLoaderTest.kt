package com.supernova.testing

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonFixtureLoaderTest {
    private val loader = JsonFixtureLoader()

    private suspend fun loadFixture(name: String) = loader.loadRaw(name)

    @Test
    fun `loadRaw caches fixture`() = runTest(timeout = 1.seconds) {
        val first = loadFixture("sample_response.json")
        val second = loadFixture("sample_response.json")
        assertEquals(first, second)
        assertEquals(1, loader.cache.size)
    }

    @Test
    fun `loadAsMap parses json`() = runTest(timeout = 1.seconds) {
        val map = loader.loadAsMap("sample_response.json")
        val status = map["status"]
        assertTrue(status is JsonPrimitive)
        assertEquals("ok", (status as JsonPrimitive).content)
        assertTrue(loader.validateFixture("sample_response.json", setOf("status", "results")))
    }

    @Test
    fun `missing fixture throws`() = runTest(timeout = 1.seconds) {
        assertFailsWith<IllegalArgumentException> { loadFixture("missing.json") }
    }

    @Test
    fun `invalid json fails validation`() = runTest(timeout = 1.seconds) {
        assertFalse(loader.validateJson("{ invalid", setOf("status")))
    }
}
