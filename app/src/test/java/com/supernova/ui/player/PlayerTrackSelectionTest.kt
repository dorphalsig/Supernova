package com.supernova.ui.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerTrackSelectionTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val viewModel = PlayerViewModel(historyDao = com.supernova.data.dao.FakeWatchHistoryDao())

    @Test
    fun selectSubtitle_updatesState() = runTest {
        viewModel.setAvailableSubtitles(listOf("en", "es"))
        viewModel.selectSubtitle("en")
        assertEquals("en", viewModel.subtitleState.value.language)
        assertEquals(true, viewModel.subtitleState.value.enabled)
    }

    @Test
    fun selectAudioTrack_updatesState() {
        val track = PlayerViewModel.AudioTrack("en", "aac")
        viewModel.setAvailableAudio(listOf(track))
        viewModel.selectAudioTrack(track)
        assertEquals(track, viewModel.audioState.value.track)
    }

    @Test
    fun selectQuality_updatesState() {
        viewModel.setAvailableQualities(listOf("Auto", "720p"))
        viewModel.selectQuality("720p")
        assertEquals("720p", viewModel.qualityState.value.quality)
    }
}
