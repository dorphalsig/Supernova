package com.supernova.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.supernova.home.HomeRepository
import com.supernova.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Simple data model representing an item shown in home rails */
data class MediaItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val progress: Float? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {
    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome(userId: Int = 1) {
        viewModelScope.launch {
            repository.observeHome(userId).collect { _state.value = it }
        }
    }
}
