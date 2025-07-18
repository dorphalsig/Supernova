package com.supernova.search

import com.supernova.testing.EntityTestSuite
import com.supernova.data.TestEntityFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SuggestionEngineTest : EntityTestSuite() {
    private lateinit var engine: SuggestionEngine

    @Before
    override fun setUp() {
        super.setUp()
        engine = SuggestionEngine(db.watchHistoryDao(), db.reactionDao())
    }

    @Test
    fun reactions_weighted_higher() = runTest {
        db.watchHistoryDao().insert(TestEntityFactory.watchHistory(userId = 1, streamId = 1))
        db.watchHistoryDao().insert(TestEntityFactory.watchHistory(userId = 1, streamId = 2))
        db.reactionDao().insert(TestEntityFactory.reaction(userId = 1, streamId = 2))
        val result = engine.suggestions(1)
        assertEquals(2, result.first())
    }
}
