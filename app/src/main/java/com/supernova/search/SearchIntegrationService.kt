package com.supernova.search

import com.supernova.data.dao.StreamDao
import com.supernova.data.dao.MovieDao
import com.supernova.data.dao.SeriesDao
import com.supernova.data.dao.LiveTvDao
import com.supernova.data.dao.EpgProgrammeDao
import com.supernova.search.SearchIntegrationService.SearchState
import com.supernova.utils.ApiUtils
import com.supernova.data.entities.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.first

class SearchIntegrationService(
    private val streamDao: StreamDao,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val liveTvDao: LiveTvDao,
    private val epgProgrammeDao: EpgProgrammeDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    sealed class SearchState {
        object Loading : SearchState()
        data class Success(val results: SearchResults) : SearchState()
        data class Error(val throwable: Throwable) : SearchState()
    }

    data class SearchResults(
        val streams: List<StreamEntity> = emptyList(),
        val movies: List<MovieEntity> = emptyList(),
        val series: List<SeriesEntity> = emptyList(),
        val channels: List<LiveTvEntity> = emptyList(),
        val programmes: List<EpgProgrammeEntity> = emptyList()
    )

    fun search(queryFlow: Flow<String>): Flow<SearchState> {
        return queryFlow
            .debounce(300)
            .mapLatest { raw ->
                val q = ApiUtils.normalizeSearchQuery(raw)
                if (q.isBlank()) return@mapLatest SearchState.Success(SearchResults())
                try {
                    val results = performSearch(q)
                    SearchState.Success(results)
                } catch (e: Exception) {
                    SearchState.Error(e)
                }
            }
            .onStart { emit(SearchState.Loading) }
    }

    fun voiceSearch(transcription: String): Flow<SearchState> = search(flowOf(transcription))

    private suspend fun performSearch(query: String): SearchResults = coroutineScope {
        val streams = async(dispatcher) { streamDao.searchStreams(query).first() }
        val movies = async(dispatcher) { movieDao.searchMovies(query).first() }
        val series = async(dispatcher) { seriesDao.searchSeries(query).first() }
        val channels = async(dispatcher) { liveTvDao.searchChannels(query).first() }
        val programmes = async(dispatcher) { epgProgrammeDao.searchProgrammes(query).first() }
        SearchResults(
            streams = streams.await(),
            movies = movies.await(),
            series = series.await(),
            channels = channels.await(),
            programmes = programmes.await()
        )
    }
}
