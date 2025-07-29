package com.supernova.domain.repository

import com.supernova.data.ProgramDao
import com.supernova.data.ProgramEntity
import com.supernova.data.SupernovaDatabase
import com.supernova.testing.BaseRoomTest
import com.supernova.testing.JsonFixtureLoader
import kotlinx.coroutines.flow.single
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private val fakeCtx = mockk<android.content.Context>(relaxed = true) {
    every { applicationContext } returns this
    every { filesDir } returns java.io.File("/tmp")
    every { noBackupFilesDir } returns java.io.File("/tmp")
    every { cacheDir } returns java.io.File("/tmp")
    every { getDatabasePath(any()) } returns java.io.File("/tmp/db")
}

class RoomProgramRepositoryTest : BaseRoomTest<SupernovaDatabase>(fakeCtx) {
    override val databaseClass = SupernovaDatabase::class

    private lateinit var dao: ProgramDao
    private lateinit var repository: ProgramRepository
    private val clock = TestClock()
    private val loader = JsonFixtureLoader()

    override fun initDaos(db: SupernovaDatabase) {
        dao = db.programDao()
        repository = RoomProgramRepository(dao, clock)
    }

    @Test
    fun `returns null when no matching program`() = runBlockingTest {
        clock.now = 1000L
        dao.insert(
            ProgramEntity(1, 1, 0, 500, "Old", "past")
        )
        val result = repository.nowPlaying(1).single()
        assertNull(result)
    }

    @Test
    fun `returns current program`() = runBlockingTest {
        val fixture = loader.loadAsMap("program_fixture.json")
        val obj = fixture["program"]!!.jsonObject
        val start = obj["start"]!!.jsonPrimitive.long
        val end = obj["end"]!!.jsonPrimitive.long
        val entity = ProgramEntity(
            id = obj["id"]!!.jsonPrimitive.int,
            epgChannelId = obj["epg_channel_id"]!!.jsonPrimitive.int,
            start = start,
            end = end,
            title = obj["title"]!!.jsonPrimitive.content,
            description = obj["description"]!!.jsonPrimitive.content
        )
        dao.insert(entity)
        clock.now = start + 100
        val result = repository.nowPlaying(entity.epgChannelId).single()!!
        assertEquals(entity.id, result.id)
        assertEquals(
            Instant.ofEpochMilli(start).atZone(ZoneOffset.UTC).toLocalDateTime(),
            result.start
        )
    }

    @Test
    fun `picks latest start on overlap`() = runBlockingTest {
        clock.now = 1500L
        val early = ProgramEntity(1, 1, 500, 2000, "A", "")
        val late = ProgramEntity(2, 1, 1000, 2000, "B", "")
        dao.insert(early)
        dao.insert(late)
        val result = repository.nowPlaying(1).single()!!
        assertEquals(late.id, result.id)
    }

    private class TestClock(var now: Long = 0L) : com.supernova.util.Clock {
        override fun currentTimeMillis(): Long = now
    }
}
