package com.supernova.search

import com.supernova.testing.EntityTestSuite
import com.supernova.data.TestEntityFactory
import com.supernova.search.EnhancedSearchRepository.SearchState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EnhancedSearchRepositoryTest : EntityTestSuite() {
    private lateinit var repo: EnhancedSearchRepository

    @Before
    override fun setUp() {
        super.setUp()
        repo = EnhancedSearchRepository(db.searchDao(), SuggestionEngine(db.watchHistoryDao(), db.reactionDao()))
    }

    @Test
    fun ranking_prefers_suggestions() = runTest {
        db.streamDao().insertStream(TestEntityFactory.stream(1, "Alpha"))
        db.streamDao().insertStreamFts(TestEntityFactory.streamFts(1, "Alpha"))
        db.streamDao().insertStream(TestEntityFactory.stream(2, "Beta"))
        db.streamDao().insertStreamFts(TestEntityFactory.streamFts(2, "Beta"))
        db.reactionDao().insert(TestEntityFactory.reaction(userId = 1, streamId = 2))
        val state = repo.search(MutableSharedFlow<String>(replay = 1).apply { tryEmit("a") }, 1)
            .first { it is SearchState.Success } as SearchState.Success
        assertEquals(2, state.results.first().id)
    }
}
