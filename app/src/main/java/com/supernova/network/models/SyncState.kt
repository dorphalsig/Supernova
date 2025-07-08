package com.supernova.network.models

sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
    data class Progress(val step: String, val current: Int, val total: Int) : SyncResult()
}

data class SyncStats(
    val categoriesCount: Int = 0,
    val liveChannelsCount: Int = 0,
    val moviesCount: Int = 0,
    val seriesCount: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null
) {
    val totalItems: Int
        get() = categoriesCount + liveChannelsCount + moviesCount + seriesCount

    val duration: Long?
        get() = endTime?.let { it - startTime }
}