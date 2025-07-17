package com.supernova.network.tmdb

data class TmdbTrendingResponse(
    val results: List<TmdbItem>
)

data class TmdbItem(
    val id: Int,
    val title: String?,
    val name: String?,
    val poster_path: String?
)
