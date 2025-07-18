package com.supernova.search

import com.supernova.testing.EntityTestSuite
import com.supernova.data.TestEntityFactory
import com.supernova.search.SearchIntegrationService.SearchState
import com.supernova.data.dao.StreamDao
import com.supernova.data.dao.MovieDao
import com.supernova.data.dao.SeriesDao
import com.supernova.data.dao.LiveTvDao
import com.supernova.data.dao.EpgProgrammeDao
import com.supernova.data.entities.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchIntegrationServiceTest : EntityTestSuite() {

    private lateinit var service: SearchIntegrationService

    @BeforeTest
    override fun setUp() {
        super.setUp()
        service = SearchIntegrationService(
            db.streamDao(),
            db.movieDao(),
            db.seriesDao(),
            db.liveTvDao(),
            db.epgProgrammeDao(),
            StandardTestDispatcher()
        )
    }

    @Test
    fun basicSearchFindsStream() = runTest {
        db.streamDao().insertStream(TestEntityFactory.stream(1, "Hero"))
        db.streamDao().insertStreamFts(TestEntityFactory.streamFts(1, "Hero"))
        val state = service.search(MutableSharedFlow<String>(replay = 1).apply { tryEmit("Hero") }).first { it is SearchState.Success } as SearchState.Success
        assertTrue(state.results.streams.any { it.title == "Hero" })
    }

    @Test
    fun voiceSearchUsesSamePipeline() = runTest {
        db.movieDao().insertMovie(TestEntityFactory.movie(id = 2, name = "Voice"))
        val state = service.voiceSearch("voice").first { it is SearchState.Success } as SearchState.Success
        assertTrue(state.results.movies.any { it.name == "Voice" })
    }

    @Test
    fun debounceEmitsLastQuery() = runTest {
        val queries = MutableSharedFlow<String>()
        val states = mutableListOf<SearchState>()
        val job = launch { service.search(queries).collect { states.add(it) } }
        queries.emit("one")
        advanceTimeBy(100)
        queries.emit("two")
        advanceTimeBy(400)
        job.cancel()
        assertTrue(states.last() !is SearchState.Error)
    }

    @Test
    fun daoErrorEmitsErrorState() = runTest {
        val streamDao = mockk<StreamDao>()
        val movieDao = mockk<MovieDao>()
        val seriesDao = mockk<SeriesDao>()
        val liveTvDao = mockk<LiveTvDao>()
        val epgProgrammeDao = mockk<EpgProgrammeDao>()

        every { streamDao.searchStreams(any()) } returns flow<List<StreamEntity>> { throw IllegalStateException("fail") }
        every { movieDao.searchMovies(any()) } returns flow<List<MovieEntity>> { emit(emptyList()) }
        every { seriesDao.searchSeries(any()) } returns flow<List<SeriesEntity>> { emit(emptyList()) }
        every { liveTvDao.searchChannels(any()) } returns flow<List<LiveTvEntity>> { emit(emptyList()) }
        every { epgProgrammeDao.searchProgrammes(any()) } returns flow<List<EpgProgrammeEntity>> { emit(emptyList()) }

        val failingService = SearchIntegrationService(
            streamDao,
            movieDao,
            seriesDao,
            liveTvDao,
            epgProgrammeDao,
            StandardTestDispatcher()
        )
        val state = failingService.search(flowOf("boom")).first { it is SearchState.Error }
        assertTrue(state is SearchState.Error)
    }
}
