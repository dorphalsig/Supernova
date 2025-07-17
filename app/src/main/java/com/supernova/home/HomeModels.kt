package com.supernova.home

import com.supernova.data.entities.StreamEntity

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val rails: List<ContentRail>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

data class ContentRail(val title: String, val items: List<StreamEntity>)
