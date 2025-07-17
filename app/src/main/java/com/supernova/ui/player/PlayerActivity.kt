package com.supernova.ui.player

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.PlaybackException
import com.supernova.ui.player.components.PlayerControlsOverlay
import com.supernova.databinding.ActivityPlayerBinding
import com.supernova.data.database.SupernovaDatabase
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var exoPlayer: ExoPlayer? = null
    private val viewModel: PlayerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = SupernovaDatabase.getDatabase(this@PlayerActivity).watchHistoryDao()
                @Suppress("UNCHECKED_CAST")
                return PlayerViewModel(dao) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.playerView.requestFocus()
        binding.controlsOverlay.setContent {
            PlayerControlsOverlay(viewModel)
        }
        observeVisibility()
        checkParentalControlsAndStart()
    }

    private fun observeVisibility() {
        lifecycleScope.launch {
            viewModel.controlsVisible.collect { visible ->
                if (!visible) binding.playerView.requestFocus()
            }
        }
    }

    private fun checkParentalControlsAndStart() {
        lifecycleScope.launch {
            val locked = SecureDataStore.getBoolean(SecureStorageKeys.PARENTAL_LOCK)
            val allowed = intent.getBooleanExtra(EXTRA_PARENTAL_ALLOWED, false)
            if (locked && !allowed) {
                Toast.makeText(this@PlayerActivity, "Parental lock enabled", Toast.LENGTH_LONG).show()
                finish()
            } else {
                initializePlayer()
            }
        }
    }

    private fun initializePlayer() {
        val url = intent.getStringExtra(EXTRA_URL) ?: return finish()
        val position = intent.getLongExtra(EXTRA_POSITION, 0L)
        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            binding.playerView.player = player
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.seekTo(position)
            player.prepare()
            player.playWhenReady = true
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    viewModel.onStateChanged(state, player.playWhenReady)
                    if (state == Player.STATE_ENDED) saveProgress()
                }

                override fun onPlayerError(error: PlaybackException) {
                    viewModel.onError(error.message ?: "error")
                    Toast.makeText(this@PlayerActivity, error.message, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun saveProgress() {
        val userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        val streamId = intent.getIntExtra(EXTRA_STREAM_ID, 0).takeIf { it != 0 }
        val episodeId = intent.getIntExtra(EXTRA_EPISODE_ID, 0).takeIf { it != 0 }
        val duration = exoPlayer?.duration ?: 0L
        val position = exoPlayer?.currentPosition ?: 0L
        viewModel.saveProgress(userId, streamId, episodeId, duration, position)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    viewModel.toggleControls()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (viewModel.controlsVisible.value) {
                        viewModel.showSettings()
                        return true
                    }
                }
                KeyEvent.KEYCODE_BACK -> {
                    if (viewModel.controlsVisible.value) {
                        viewModel.hideControls()
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onStop() {
        super.onStop()
        saveProgress()
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        const val EXTRA_URL = "stream_url"
        const val EXTRA_POSITION = "position"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_STREAM_ID = "stream_id"
        const val EXTRA_EPISODE_ID = "episode_id"
        const val EXTRA_PARENTAL_ALLOWED = "parental_allowed"
    }
}
