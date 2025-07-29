package com.supernova.domain.mapper

import com.supernova.network.dto.CategoryDto
import com.supernova.network.dto.StreamDto
import com.supernova.network.dto.ProgramDto
import com.supernova.testing.JsonFixtureLoader
import com.supernova.testing.TestEntityFactory
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MappersTest : TestEntityFactory() {
    private val loader = JsonFixtureLoader()

    @Test
    fun `category dto maps to domain`() = runTest {
        val fixture = loader.loadAsMap("mappers_fixture.json")
        val obj = fixture["category"]!!.jsonObject
        val dto = CategoryDto(
            categoryId = obj["category_id"]!!.jsonPrimitive.int,
            categoryName = obj["category_name"]!!.jsonPrimitive.content
        )
        val domain = dto.toDomain()
        assertEquals(1, domain.id)
        assertEquals("News", domain.name)
    }

    @Test
    fun `stream dto maps to domain`() = runTest {
        val fixture = loader.loadAsMap("mappers_fixture.json")
        val obj = fixture["stream"]!!.jsonObject
        val dto = StreamDto(
            streamId = obj["stream_id"]!!.jsonPrimitive.int,
            name = obj["name"]!!.jsonPrimitive.content,
            categoryId = obj["category_id"]!!.jsonPrimitive.int,
            streamType = obj["stream_type"]!!.jsonPrimitive.content
        )
        val domain = dto.toDomain()
        assertEquals(10, domain.id)
        assertEquals("CNN", domain.name)
        assertEquals(1, domain.categoryId)
        assertEquals("live", domain.streamType)
    }

    @Test
    fun `program dto maps to domain with valid dates`() = runTest {
        val fixture = loader.loadAsMap("mappers_fixture.json")
        val obj = fixture["program"]!!.jsonObject
        val dto = ProgramDto(
            id = obj["id"]!!.jsonPrimitive.int,
            epgChannelId = obj["epg_channel_id"]!!.jsonPrimitive.int,
            start = obj["start"]!!.jsonPrimitive.content,
            end = obj["end"]!!.jsonPrimitive.content,
            title = obj["title"]!!.jsonPrimitive.content,
            description = obj["description"]!!.jsonPrimitive.content
        )
        val domain = dto.toDomain()
        assertEquals(LocalDateTime.of(2024,1,1,10,0,0), domain.start)
        assertEquals(LocalDateTime.of(2024,1,1,11,0,0), domain.end)
        assertEquals("Morning News", domain.title)
    }

    @Test
    fun `program dto maps with invalid date uses min`() = runTest {
        val dto = ProgramDto(
            id = 2,
            epgChannelId = 5,
            start = "bad", end = "bad",
            title = "Bad", description = "Bad"
        )
        val domain = dto.toDomain()
        assertEquals(LocalDateTime.MIN, domain.start)
        assertEquals(LocalDateTime.MIN, domain.end)
    }
}
