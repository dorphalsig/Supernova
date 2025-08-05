package com.supernova.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Mock player controller for testing playback scenarios without Android MediaPlayer.
 * Tracks state transitions and position for resume/seek testing.
 */
class MockPlayerController {
    private val _state = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _errors = MutableStateFlow<PlayerError?>(null)
    val errors: StateFlow<PlayerError?> = _errors.asStateFlow()

    // Test events log
    private val _events = mutableListOf<PlayerEvent>()
    val events: List<PlayerEvent> get() = _events.toList()

    fun prepare(url: String, duration: Long = 3600000L) {
        _duration.value = duration
        _position.value = 0L
        _state.value = PlayerState.Prepared
        _events.add(PlayerEvent.Prepared(url))
    }

    fun play() {
        require(_state.value != PlayerState.Idle) { "Cannot play from idle state" }
        _state.value = PlayerState.Playing
        _events.add(PlayerEvent.Play)
    }

    fun pause() {
        require(_state.value == PlayerState.Playing) { "Cannot pause from ${_state.value}" }
        _state.value = PlayerState.Paused
        _events.add(PlayerEvent.Pause)
    }

    fun seekTo(positionMs: Long) {
        require(positionMs >= 0 && positionMs <= _duration.value) {
            "Invalid seek position: $positionMs (duration: ${_duration.value})"
        }
        _position.value = positionMs
        _events.add(PlayerEvent.Seek(positionMs))
    }

    fun simulateProgress(positionMs: Long) {
        require(_state.value == PlayerState.Playing) { "Cannot progress when not playing" }
        _position.value = positionMs.coerceAtMost(_duration.value)
        if (_position.value >= _duration.value) {
            _state.value = PlayerState.Ended
            _events.add(PlayerEvent.Ended)
        }
    }

    fun simulateBuffering(buffering: Boolean) {
        _isBuffering.value = buffering
        _events.add(PlayerEvent.Buffering(buffering))
    }

    fun simulateError(error: PlayerError) {
        _errors.value = error
        _state.value = PlayerState.Error
        _events.add(PlayerEvent.Error(error))
    }

    fun stop() {
        _state.value = PlayerState.Idle
        _position.value = 0L
        _errors.value = null
        _events.add(PlayerEvent.Stop)
    }

    fun reset() {
        _state.value = PlayerState.Idle
        _position.value = 0L
        _duration.value = 0L
        _isBuffering.value = false
        _errors.value = null
        _events.clear()
    }

    // Test helpers
    fun getProgressPercent(): Float {
        return if (_duration.value > 0) {
            (_position.value.toFloat() / _duration.value) * 100f
        } else 0f
    }

    fun isAtResumeThreshold(): Boolean = getProgressPercent() in 5f..95f
    fun isCompleted(): Boolean = getProgressPercent() >= 95f
    fun shouldShowResumeDialog(): Boolean = isAtResumeThreshold()
}

sealed class PlayerState {
    object Idle : PlayerState()
    object Prepared : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    object Buffering : PlayerState()
    object Ended : PlayerState()
    object Error : PlayerState()
}

sealed class PlayerError(val message: String) {
    object NetworkError : PlayerError("Network connection failed")
    object NotFound : PlayerError("Stream not found (404)")
    object Unauthorized : PlayerError("Stream unauthorized (401)")
    object BufferTimeout : PlayerError("Buffer timeout")
    data class Unknown(val code: Int) : PlayerError("Unknown error: $code")
}

sealed class PlayerEvent {
    data class Prepared(val url: String) : PlayerEvent()
    object Play : PlayerEvent()
    object Pause : PlayerEvent()
    object Stop : PlayerEvent()
    object Ended : PlayerEvent()
    data class Seek(val position: Long) : PlayerEvent()
    data class Buffering(val isBuffering: Boolean) : PlayerEvent()
    data class Error(val error: PlayerError) : PlayerEvent()
}