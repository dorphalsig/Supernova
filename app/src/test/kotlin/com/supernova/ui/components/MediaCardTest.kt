package com.supernova.ui.components

import com.supernova.testing.UiStateTestHelpers
import com.supernova.testing.UiState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class MediaProgressVm : UiStateTestHelpers.TestViewModel<Float?>(UiState.Loading) {
    suspend fun emit(progress: Float?) { setState(UiState.Success(progress)) }
}

class MediaCardTest {
    @Test
    fun `progress state emitted`() = runTest {
        val vm = MediaProgressVm()
        val states = vm.test { (this as MediaProgressVm).emit(0.3f) }
        UiStateTestHelpers.assertLoading(states.first())
        UiStateTestHelpers.assertSuccess(states.last()) { assertEquals(0.3f, it) }
    }
}
