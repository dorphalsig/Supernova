package com.supernova.ui.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.supernova.ui.player.PlayerViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerControlsOverlay(viewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val visible by viewModel.controlsVisible.collectAsState()
    val showSettings by viewModel.settingsVisible.collectAsState()
    if (!visible) return

    val focusRequester = FocusRequester()
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .focusRequester(focusRequester)
            .focusable()
    ) {
        Row(modifier = Modifier.align(Alignment.Center)) {
            Text("Playback", color = Color.White)
        }
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it })
        ) {
            PlayerSettingsPanel(viewModel, Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
fun PlayerSettingsPanel(viewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val subtitleState by viewModel.subtitleState.collectAsState()
    val audioState by viewModel.audioState.collectAsState()
    val qualityState by viewModel.qualityState.collectAsState()

    Surface(
        modifier = modifier
            .size(width = 300.dp, height = 400.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SubtitleSelector(
                state = subtitleState,
                onSelect = viewModel::selectSubtitle
            )
            AudioTrackSelector(
                state = audioState,
                onSelect = viewModel::selectAudioTrack
            )
            QualitySelector(
                state = qualityState,
                onSelect = viewModel::selectQuality
            )
        }
    }
}

@Composable
fun SubtitleSelector(state: PlayerViewModel.SubtitleState, onSelect: (String?) -> Unit) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text("Subtitles", color = Color.White)
        state.languages.forEach { lang ->
            Text(
                text = lang,
                color = if (state.language == lang) Color.Yellow else Color.White,
                modifier = Modifier
                    .padding(4.dp)
                    .focusable()
                    .clickable { onSelect(lang) }
            )
        }
        Text(
            text = "Off",
            color = if (!state.enabled) Color.Yellow else Color.White,
            modifier = Modifier
                .padding(4.dp)
                .focusable()
                .clickable { onSelect(null) }
        )
    }
}

@Composable
fun AudioTrackSelector(state: PlayerViewModel.AudioState, onSelect: (PlayerViewModel.AudioTrack) -> Unit) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text("Audio", color = Color.White)
        state.available.forEach { track ->
            val selected = state.track == track
            Text(
                text = "${track.language} (${track.codec})",
                color = if (selected) Color.Yellow else Color.White,
                modifier = Modifier
                    .padding(4.dp)
                    .focusable()
                    .clickable { onSelect(track) }
            )
        }
    }
}

@Composable
fun QualitySelector(state: PlayerViewModel.QualityState, onSelect: (String) -> Unit) {
    Column {
        Text("Quality", color = Color.White)
        state.available.forEach { q ->
            Text(
                text = q,
                color = if (state.quality == q) Color.Yellow else Color.White,
                modifier = Modifier
                    .padding(4.dp)
                    .focusable()
                    .clickable { onSelect(q) }
            )
        }
    }
}
