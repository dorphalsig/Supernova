package com.supernova.ui.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.supernova.data.dao.WatchHistoryDao
import com.supernova.data.entities.WatchHistoryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var historyDao: WatchHistoryDao
    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        historyDao = mockk(relaxed = true)
        viewModel = PlayerViewModel(historyDao)
    }

    @Test
    fun saveProgress_insertsRecord() = runTest {
        val slot = slot<WatchHistoryEntity>()
        coEvery { historyDao.insert(capture(slot)) } returns 1L

        viewModel.saveProgress(
            userId = 1,
            streamId = 2,
            episodeId = null,
            durationMs = 1000L,
            positionMs = 500L
        )

        coVerify { historyDao.insert(any()) }
        assertEquals(0.5f, slot.captured.progress!!, 0.01f)
    }

    @Test
    fun onStateChanged_updatesState() {
        viewModel.onStateChanged(androidx.media3.common.Player.STATE_BUFFERING, false)
        assertEquals(PlaybackStatus.Buffering, viewModel.playbackState.value)
        viewModel.onStateChanged(androidx.media3.common.Player.STATE_READY, true)
        assertEquals(PlaybackStatus.Playing, viewModel.playbackState.value)
    }
}
