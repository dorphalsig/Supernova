package com.supernova.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.supernova.data.dao.SearchDao
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SearchDaoTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var dao: SearchDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.searchDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun searchAll_basic() = runTest {
        val now = System.currentTimeMillis()
        val movie = StreamEntity(
            streamId = 1,
            title = "Star",
            year = 2020,
            streamType = "movie",
            thumbnailUrl = null,
            bannerUrl = null,
            tmdbId = null,
            mediaType = null,
            tmdbSyncedAt = null,
            providerId = null,
            containerExtension = null,
            epgChannelId = null,
            tvArchive = null,
            tvArchiveDuration = null,
            directSource = null,
            customSid = null,
            rating = null,
            rating5Based = null,
            added = null,
            plot = null,
            cast = null,
            director = null,
            genre = null
        )
        db.streamDao().insertStream(movie)
        db.streamDao().insertStreamFts(StreamFts(1, "Star", null, null, null, null))

        val live = LiveTvEntity(2, 1, "News Channel", "live", null, "ch1", null, null, null, null, null, null, null, null)
        db.liveTvDao().insertChannel(live)
        db.streamDao().insertStreamFts(StreamFts(2, "News Channel", null, null, null, null))
        val programme = EpgProgrammeEntity(10, "ch1", now + 60000, now + 120000, "Morning News", null, null, null, null)
        db.epgProgrammeDao().insert(programme)

        val results = dao.searchAll("news", now)
        assertTrue(results.any { it.type == "live" })
        assertTrue(results.any { it.type == "episode" })

        val empty = dao.searchAll("ab", now)
        assertTrue(empty.isEmpty())
    }
}
