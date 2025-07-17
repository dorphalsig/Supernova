package com.supernova.search

import com.supernova.data.dao.SearchDao
import com.supernova.search.SearchIntegrationService.SearchState
import com.supernova.utils.ApiUtils
import com.supernova.data.entities.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

class SearchIntegrationService(
    private val searchDao: SearchDao,
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
        val streams = async(dispatcher) { searchDao.searchStreams(query).first() }
        val movies = async(dispatcher) { searchDao.searchMovies(query).first() }
        val series = async(dispatcher) { searchDao.searchSeries(query).first() }
        val channels = async(dispatcher) { searchDao.searchChannels(query).first() }
        val programmes = async(dispatcher) { searchDao.searchProgrammes(query).first() }
        SearchResults(
            streams = streams.await(),
            movies = movies.await(),
            series = series.await(),
            channels = channels.await(),
            programmes = programmes.await()
        )
    }
}
