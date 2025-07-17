package com.supernova.search

import com.supernova.data.dao.SearchDao
import com.supernova.data.entities.SearchResult
import com.supernova.utils.SearchUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EnhancedSearchRepository @Inject constructor(
    private val searchDao: SearchDao,
    private val suggestionEngine: SuggestionEngine,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    sealed class SearchState {
        object Loading : SearchState()
        object VoiceListening : SearchState()
        data class Success(val results: List<SearchResult>) : SearchState()
        data class Error(val throwable: Throwable) : SearchState()
    }

    fun search(queryFlow: Flow<String>, userId: Int): Flow<SearchState> =
        queryFlow.debounce(300).mapLatest { raw ->
            val q = SearchUtils.normalize(raw)
            if (q.isBlank()) return@mapLatest SearchState.Success(emptyList())
            try {
                val results = withContext(dispatcher) { searchDao.searchAll(q, System.currentTimeMillis()) }
                val suggestions = suggestionEngine.suggestions(userId)
                val ranked = rank(results, suggestions)
                SearchState.Success(ranked)
            } catch (e: Exception) {
                SearchState.Error(e)
            }
        }.onStart { emit(SearchState.Loading) }

    private fun rank(results: List<SearchResult>, suggestions: List<Int>): List<SearchResult> {
        if (suggestions.isEmpty()) return results
        val weights = suggestions.withIndex().associate { it.value to (suggestions.size - it.index) }
        return results.sortedWith(compareByDescending<SearchResult> { weights[it.id] ?: 0 }.thenBy { it.relevance })
    }
}
