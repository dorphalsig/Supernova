package com.supernova.data.entities

/** Unified search result across movies, series, live TV and episodes */
data class SearchResult(
    val id: Int,
    val title: String?,
    val type: String,
    val posterUrl: String?,
    val startAt: Long?,
    val relevance: Int
)
