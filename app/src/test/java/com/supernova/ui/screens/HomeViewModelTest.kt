package com.supernova.ui.screens

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @Test
    fun loadHome_populatesRails() = runTest {
        val vm = HomeViewModel()
        vm.loadHome()
        advanceUntilIdle()
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertTrue(state.continueWatching.isNotEmpty())
        assertEquals(8, state.trendingMovies.size)
    }
}
