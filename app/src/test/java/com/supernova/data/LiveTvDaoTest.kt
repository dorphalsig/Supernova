package com.supernova.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.dao.LiveTvDao
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LiveTvDaoTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var dao: LiveTvDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.liveTvDao()
    }

    @After
    fun tearDown() {
        db.close()
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
