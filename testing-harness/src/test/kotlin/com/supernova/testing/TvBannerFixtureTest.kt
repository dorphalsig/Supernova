package com.supernova.testing

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TvBannerFixtureTest : TestEntityFactory() {
    private val loader = JsonFixtureLoader()

    @Test
    fun `load banner fixture`() = runTest {
        val json = loader.loadJsonFixture("tv_banner.json")
        val map = loader.loadAsMap("tv_banner.json")
        assertEquals("banner", map["type"]?.jsonPrimitive?.content)
    }
}
