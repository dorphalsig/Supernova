package com.supernova.testing

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestEntityFactoryTest : TestEntityFactory() {
    private fun loadStreamFixture(): String =
        javaClass.classLoader!!.getResource("fixtures/stream.json")!!.readText()
    @Test
    fun `stream defaults`() = runTest {
        val json = loadStreamFixture()
        val s = TestEntityFactory.stream()
        assertEquals(1L, s.id)
        assertEquals("Stream 1", s.title)
        assertTrue(s.isLive)
        assertTrue(json.isNotEmpty())
    }

    @Test
    fun `stream overrides`() = runTest {
        val custom = TestEntityFactory.stream(id = 2L, title = "Custom", isLive = false)
        assertEquals(2L, custom.id)
        assertEquals("Custom", custom.title)
        assertFalse(custom.isLive)
    }

    @Test
    fun `program defaults`() = runTest {
        val p = TestEntityFactory.program()
        assertEquals(1L, p.id)
        assertEquals(p.start + 3_600_000L, p.end)
    }

    @Test
    fun `episode defaults`() = runTest {
        val e = TestEntityFactory.episode()
        assertEquals(1, e.seasonNum)
        assertEquals("poster_1.jpg", e.poster)
    }

    @Test
    fun `category defaults`() = runTest {
        val c = TestEntityFactory.category()
        assertEquals("Category 1", c.name)
    }

    @Test
    fun `profile defaults`() = runTest {
        val p = TestEntityFactory.profile()
        assertEquals("user1", p.username)
    }

    @Test
    fun `favorite defaults`() = runTest {
        val f = TestEntityFactory.favorite()
        assertEquals(1L, f.profileId)
    }

    @Test
    fun `watchHistory defaults`() = runTest {
        val w = TestEntityFactory.watchHistory()
        assertEquals(0, w.percentWatched)
    }

    @Test
    fun `recommendation consumed by lambda`() = runTest {
        val consumer = mockk<(Recommendation) -> Unit>(relaxed = true)
        val rec = TestEntityFactory.recommendation()
        consumer(rec)
        verify { consumer(rec) }
    }

    @Test
    fun `tmdbMetadata defaults`() = runTest {
        val t = TestEntityFactory.tmdbMetadata()
        assertEquals(100L, t.tmdbId)
        assertTrue(t.genres.isEmpty())
    }
}
