package com.supernova.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supernova.data.dao.WatchHistoryDao
import com.supernova.data.entities.WatchHistoryEntity
import androidx.media3.common.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(private val historyDao: WatchHistoryDao) : ViewModel() {

    private val _playbackState = MutableStateFlow<PlaybackStatus>(PlaybackStatus.Idle)
    val playbackState: StateFlow<PlaybackStatus> = _playbackState

    data class SubtitleState(
        val enabled: Boolean = false,
        val language: String? = null,
        val languages: List<String> = emptyList()
    )

    data class AudioTrack(val language: String, val codec: String)

    data class AudioState(
        val track: AudioTrack? = null,
        val available: List<AudioTrack> = emptyList()
    )

    data class QualityState(
        val quality: String = "Auto",
        val available: List<String> = listOf("Auto")
    )

    private val _subtitleState = MutableStateFlow(SubtitleState())
    val subtitleState: StateFlow<SubtitleState> = _subtitleState

    private val _audioState = MutableStateFlow(AudioState())
    val audioState: StateFlow<AudioState> = _audioState

    private val _qualityState = MutableStateFlow(QualityState())
    val qualityState: StateFlow<QualityState> = _qualityState

    private val _controlsVisible = MutableStateFlow(false)
    val controlsVisible: StateFlow<Boolean> = _controlsVisible

    private val _settingsVisible = MutableStateFlow(false)
    val settingsVisible: StateFlow<Boolean> = _settingsVisible

    private var hideJob: Job? = null

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

    fun setAvailableSubtitles(languages: List<String>) {
        _subtitleState.value = _subtitleState.value.copy(languages = languages)
    }

    fun selectSubtitle(language: String?) {
        _subtitleState.value = _subtitleState.value.copy(
            enabled = language != null,
            language = language
        )
    }

    fun setAvailableAudio(tracks: List<AudioTrack>) {
        _audioState.value = _audioState.value.copy(available = tracks)
    }

    fun selectAudioTrack(track: AudioTrack) {
        _audioState.value = _audioState.value.copy(track = track)
    }

    fun setAvailableQualities(qualities: List<String>) {
        _qualityState.value = _qualityState.value.copy(available = qualities)
    }

    fun selectQuality(quality: String) {
        _qualityState.value = _qualityState.value.copy(quality = quality)
    }

    fun showControls(autoHide: Boolean = true) {
        _controlsVisible.value = true
        if (autoHide) {
            hideJob?.cancel()
            hideJob = viewModelScope.launch {
                delay(5000)
                _controlsVisible.value = false
            }
        }
    }

    fun hideControls() {
        hideJob?.cancel()
        _controlsVisible.value = false
        hideSettings()
    }

    fun toggleControls() {
        if (_controlsVisible.value) hideControls() else showControls()
    }

    fun showSettings() {
        _settingsVisible.value = true
    }

    fun hideSettings() {
        _settingsVisible.value = false
    }
}
