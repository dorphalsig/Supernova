package com.supernova.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supernova.data.dao.WatchHistoryDao
import com.supernova.data.entities.WatchHistoryEntity
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(private val historyDao: WatchHistoryDao) : ViewModel() {

    private val _playbackState = MutableStateFlow<PlaybackStatus>(PlaybackStatus.Idle)
    val playbackState: StateFlow<PlaybackStatus> = _playbackState

    fun onStateChanged(state: Int, playing: Boolean) {
        _playbackState.value = when (state) {
            Player.STATE_BUFFERING -> PlaybackStatus.Buffering
            Player.STATE_READY -> if (playing) PlaybackStatus.Playing else PlaybackStatus.Paused
            Player.STATE_ENDED -> PlaybackStatus.Ended
            else -> PlaybackStatus.Idle
        }
    }

    fun onError(message: String) {
        _playbackState.value = PlaybackStatus.Error(message)
    }

    fun saveProgress(
        userId: Int,
        streamId: Int?,
        episodeId: Int?,
        durationMs: Long,
        positionMs: Long
    ) {
        val durationSec = if (durationMs > 0) (durationMs / 1000).toInt() else null
        val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else null
        viewModelScope.launch {
            historyDao.insert(
                WatchHistoryEntity(
                    userId = userId,
                    streamId = streamId,
                    episodeId = episodeId,
                    watchedAt = System.currentTimeMillis(),
                    durationSec = durationSec,
                    progress = progress
                )
            )
        }
    }
}
