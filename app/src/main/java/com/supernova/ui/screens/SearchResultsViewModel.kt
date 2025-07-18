package com.supernova.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Content types supported in search */
enum class ContentType { MOVIE, SERIES, CHANNEL }

/** Simple content item used for mock search results */
data class ContentItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val type: ContentType,
)

sealed interface NavigationEvent {
    data class OpenItem(val type: ContentType, val id: String) : NavigationEvent
    data class OpenAll(val type: ContentType) : NavigationEvent
}

class SearchResultsViewModel : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _topResults = MutableStateFlow<List<ContentItem>>(emptyList())
    val topResults: StateFlow<List<ContentItem>> = _topResults.asStateFlow()

    private val _movies = MutableStateFlow<List<ContentItem>>(emptyList())
    val movies: StateFlow<List<ContentItem>> = _movies.asStateFlow()

    private val _series = MutableStateFlow<List<ContentItem>>(emptyList())
    val series: StateFlow<List<ContentItem>> = _series.asStateFlow()

    private val _channels = MutableStateFlow<List<ContentItem>>(emptyList())
    val channels: StateFlow<List<ContentItem>> = _channels.asStateFlow()

    private val _events = MutableSharedFlow<NavigationEvent>()
    val events = _events.asSharedFlow()

    private val sampleMovies = listOf(
        ContentItem("1", "The Matrix", null, ContentType.MOVIE),
        ContentItem("2", "Inception", null, ContentType.MOVIE),
        ContentItem("3", "Avatar", null, ContentType.MOVIE),
        ContentItem("4", "Interstellar", null, ContentType.MOVIE),
        ContentItem("5", "Up", null, ContentType.MOVIE)
    )

    private val sampleSeries = listOf(
        ContentItem("s1", "Breaking Bad", null, ContentType.SERIES),
        ContentItem("s2", "Lost", null, ContentType.SERIES),
        ContentItem("s3", "Foundation", null, ContentType.SERIES)
    )

    private val sampleChannels = listOf(
        ContentItem("c1", "BBC", null, ContentType.CHANNEL),
        ContentItem("c2", "Discovery", null, ContentType.CHANNEL),
        ContentItem("c3", "HBO", null, ContentType.CHANNEL)
    )

    fun onQueryChanged(q: String) {
        _query.value = q
        performSearch(q)
    }

    private fun performSearch(query: String) {
        val q = query.lowercase()
        val m = sampleMovies.filter { it.title.lowercase().contains(q) }
        val s = sampleSeries.filter { it.title.lowercase().contains(q) }
        val c = sampleChannels.filter { it.title.lowercase().contains(q) }

        _movies.value = m
        _series.value = s
        _channels.value = c
        _topResults.value = (m + s + c).take(4)
    }

    fun onItemSelected(type: ContentType, id: String) {
        viewModelScope.launch { _events.emit(NavigationEvent.OpenItem(type, id)) }
    }

    fun onSeeAll(type: ContentType) {
        viewModelScope.launch { _events.emit(NavigationEvent.OpenAll(type)) }
    }
}

