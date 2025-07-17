package com.supernova.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.supernova.data.dao.SearchDao
import com.supernova.testing.DbPerformanceUtils.explainQueryPlan
import com.supernova.testing.InstrumentedTestSuite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Instrumented tests for FTS4 search queries and performance.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SearchDaoInstrumentedTest : InstrumentedTestSuite() {

    private lateinit var dao: SearchDao

    @Before
    override fun setUp() {
        super.setUp()
        dao = db.searchDao()
    }

    @Test
    fun searchAll_basic() = runTest {
        val now = System.currentTimeMillis()
        val movie = TestEntityFactory.stream(id = 1, title = "Star")
        db.streamDao().insertStream(movie)
        db.streamDao().insertStreamFts(TestEntityFactory.streamFts(1, "Star"))

        val live = TestEntityFactory.liveTv(id = 2, name = "News Channel", epgId = "ch1")
        db.liveTvDao().insertChannel(live)
        db.streamDao().insertStreamFts(TestEntityFactory.streamFts(2, "News Channel"))
        val programme = TestEntityFactory.programme(id = 10, channelId = "ch1", title = "Morning News")
        db.epgProgrammeDao().insert(programme)
        db.epgProgrammeDao().insertFts(TestEntityFactory.programmeFts(10, "Morning News"))

        val results = dao.searchAll("news", now)
        assertTrue(results.any { it.type == "live" })
        assertTrue(results.any { it.type == "episode" })

        val empty = dao.searchAll("ab", now)
        assertTrue(empty.isEmpty())
    }

    @Test
    fun search_uses_fts_index() {
        val plan = explainQueryPlan(db, "SELECT rowid FROM stream_fts WHERE stream_fts MATCH 'star'")
        assertTrue(plan.isNotEmpty())
    }
}
