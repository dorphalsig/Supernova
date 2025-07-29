package com.supernova.ui.splash

import android.app.Application
import com.supernova.sync.OneShotSyncWorker
import com.supernova.ui.navigation.NavigationEvent
import com.supernova.testing.JsonFixtureLoader
import com.supernova.testing.TestEntityFactory
import com.supernova.ui.UiState
import io.mockk.coEvery
import io.mockk.mockkObject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SplashViewModelTest : TestEntityFactory() {
    private val context: Application = Application()
    private val loader = JsonFixtureLoader()

    @Test
    fun `success triggers navigation`() = runTest {
        loader.loadJsonFixture("splash_worker_fixture.json")
        mockkObject(OneShotSyncWorker)
        coEvery { OneShotSyncWorker.trigger(any()) } returns Result.success(Unit)

        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = SplashViewModel(context, dispatcher)

        val event = withTimeoutOrNull(100) { vm.events.first() }
        advanceUntilIdle()
        assertEquals(NavigationEvent.NavigateToHome, event)
        advanceUntilIdle()
        assertEquals(true, vm.state.value is UiState.Success)
        vm.viewModelScope.cancel()
        io.mockk.unmockkAll()
    }

    @Test
    fun `failure emits error state`() = runTest {
        loader.loadJsonFixture("splash_worker_fixture.json")
        mockkObject(OneShotSyncWorker)
        coEvery { OneShotSyncWorker.trigger(any()) } returns Result.failure(RuntimeException("boom"))

        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = SplashViewModel(context, dispatcher)

        advanceUntilIdle()
        vm.viewModelScope.cancel()
        advanceUntilIdle()
        io.mockk.unmockkAll()

        assertTrue(vm.events.replayCache.isEmpty())
        assertEquals(true, vm.state.value is UiState.Error)
    }
}
