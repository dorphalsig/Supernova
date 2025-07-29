package com.supernova.domain.mapper

import com.supernova.network.dto.CategoryDto
import com.supernova.network.dto.StreamDto
import com.supernova.network.dto.ProgramDto
import com.supernova.testing.JsonFixtureLoader
import com.supernova.testing.TestEntityFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentSyncMapperTest : TestEntityFactory() {
    private val loader = JsonFixtureLoader()

    @Test
    fun `category dto maps to entity`() = runTest {
        val obj = loader.loadAsMap("content_sync_mapper.json")["category"]!!.jsonObject
        val dto = CategoryDto(
            categoryId = obj["category_id"]!!.jsonPrimitive.int,
            categoryName = obj["category_name"]!!.jsonPrimitive.content
        )
        val entity = dto.toEntity()
        assertEquals(2, entity.id)
        assertEquals("Sports", entity.name)
    }

    @Test
    fun `stream dto maps to entity`() = runTest {
        val obj = loader.loadAsMap("content_sync_mapper.json")["stream"]!!.jsonObject
        val dto = StreamDto(
            streamId = obj["stream_id"]!!.jsonPrimitive.int,
            name = obj["name"]!!.jsonPrimitive.content,
            categoryId = obj["category_id"]!!.jsonPrimitive.int,
            streamType = obj["stream_type"]!!.jsonPrimitive.content
        )
        val entity = dto.toEntity(obj["category_id"]!!.jsonPrimitive.int)
        assertEquals(20, entity.id)
        assertEquals("ESPN", entity.name)
        assertEquals(2, entity.categoryId)
        assertEquals("live", entity.streamType)
    }

    @Test
    fun `program dto maps to entity`() = runTest {
        val obj = loader.loadAsMap("content_sync_mapper.json")["program"]!!.jsonObject
        val dto = ProgramDto(
            id = obj["id"]!!.jsonPrimitive.int,
            epgChannelId = obj["epg_channel_id"]!!.jsonPrimitive.int,
            start = obj["start"]!!.jsonPrimitive.content,
            end = obj["end"]!!.jsonPrimitive.content,
            title = obj["title"]!!.jsonPrimitive.content,
            description = obj["description"]!!.jsonPrimitive.content
        )
        val entity = dto.toEntity(20)
        assertEquals(201, entity.id)
        assertEquals(20, entity.epgChannelId)
        assertEquals("2024-01-01 12:00:00", entity.start)
        assertEquals("2024-01-01 13:00:00", entity.end)
        assertEquals("Sports Center", entity.title)
        assertEquals("Highlights", entity.description)
    }
}
