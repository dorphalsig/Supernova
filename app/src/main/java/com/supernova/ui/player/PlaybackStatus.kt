package com.supernova.ui.player

sealed class PlaybackStatus {
    object Idle : PlaybackStatus()
    object Playing : PlaybackStatus()
    object Paused : PlaybackStatus()
    object Buffering : PlaybackStatus()
    object Ended : PlaybackStatus()
    data class Error(val message: String) : PlaybackStatus()
}
