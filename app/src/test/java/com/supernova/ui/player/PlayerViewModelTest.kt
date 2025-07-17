package com.supernova.ui.player

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PlayerViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = PlayerViewModel(db.watchHistoryDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveProgress_insertsRecord() = runTest {
        viewModel.saveProgress(userId = 1, streamId = 2, episodeId = null, durationMs = 1000L, positionMs = 500L)
        val items = db.watchHistoryDao().getAll().first()
        assertEquals(1, items.size)
        assertEquals(0.5f, items[0].progress!!, 0.01f)
    }

    @Test
    fun onStateChanged_updatesState() {
        viewModel.onStateChanged(androidx.media3.common.Player.STATE_BUFFERING, false)
        assertEquals(PlaybackStatus.Buffering, viewModel.playbackState.value)
        viewModel.onStateChanged(androidx.media3.common.Player.STATE_READY, true)
        assertEquals(PlaybackStatus.Playing, viewModel.playbackState.value)
    }
}
