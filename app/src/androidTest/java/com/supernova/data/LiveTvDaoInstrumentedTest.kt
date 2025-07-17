package com.supernova.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.supernova.data.dao.LiveTvDao
import com.supernova.testing.InstrumentedTestSuite
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * Instrumented tests for [LiveTvDao] using real Room database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LiveTvDaoInstrumentedTest : InstrumentedTestSuite() {

    private lateinit var dao: LiveTvDao

    @Before
    override fun setUp() {
        super.setUp()
        dao = db.liveTvDao()
    }

    @Test
    fun insertChannelAndQueryByEpg() = runTest {
        val channel = TestEntityFactory.liveTv(id = 1, epgId = "epg1")
        dao.insertChannel(channel)
        val loaded = dao.getChannelByEpgId("epg1")
        assertEquals(channel.channel_id, loaded?.channel_id)
    }

    @Test
    fun observeChannelsWithArchive() = runTest {
        val collector = mockk<(List<com.supernova.data.entities.LiveTvEntity>) -> Unit>(relaxed = true)
        val job = launch { dao.getChannelsWithArchive().collect { collector.invoke(it) } }
        dao.insertChannel(TestEntityFactory.liveTv(id = 2, archive = 1))
        verify { collector.invoke(any()) }
        job.cancel()
    }
}
