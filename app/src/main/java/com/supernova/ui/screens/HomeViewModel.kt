package com.supernova.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Simple data model representing an item shown in home rails */
data class MediaItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val progress: Float? = null
)

data class HomeState(
    val isLoading: Boolean = false,
    val continueWatching: List<MediaItem> = emptyList(),
    val trendingMovies: List<MediaItem> = emptyList(),
    val trendingSeries: List<MediaItem> = emptyList(),
    val forYou: List<MediaItem> = emptyList()
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState(isLoading = true))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun loadHome() {
        // Simulate loading with mock data
        viewModelScope.launch {
            _state.value = HomeState(isLoading = true)
            delay(300)
            val items = (1..8).map {
                MediaItem(
                    id = it,
                    title = "Item $it",
                    posterUrl = null,
                    progress = null
                )
            }
            val cwItems = items.take(6).mapIndexed { index, m ->
                m.copy(progress = (index + 1) / 8f)
            }
            _state.value = HomeState(
                isLoading = false,
                continueWatching = cwItems,
                trendingMovies = items,
                trendingSeries = items,
                forYou = items
            )
        }
    }
}
