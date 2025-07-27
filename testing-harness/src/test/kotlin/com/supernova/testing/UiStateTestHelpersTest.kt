package com.supernova.testing

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import com.supernova.testing.UiStateTestHelpers.collectStates
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UiStateTestHelpersTest {
    private fun loadFixture(): String =
        javaClass.classLoader!!.getResource("fixtures/ui_state_success.json")!!.readText()

    @Test
    fun `loading assertion`() = runTest() {
        UiStateTestHelpers.assertLoading(UiState.Loading)
    }

    @Test
    fun `success assertion`() = runTest() {
        val data = loadFixture()
        UiStateTestHelpers.assertSuccess(UiState.Success(data)) {
            assertEquals(data, it)
        }
    }

    @Test
    fun `error assertion`() = runTest() {
        val err = IllegalStateException("boom")
        UiStateTestHelpers.assertError(UiState.Error(err)) {
            assertEquals(err, it)
        }
    }

    @Test
    fun `collect states from flow`() = runTest() {
        val flow = MutableStateFlow<UiState<String>>(UiState.Loading)
        val updater = launch { flow.value = UiState.Success("done") }
        val states = flow.collectStates(2)
        updater.join()
        assertEquals(listOf(UiState.Loading, UiState.Success("done")), states)
    }

    @Test
    fun `viewmodel state test`() = runTest() {
        class ExampleVm : UiStateTestHelpers.TestViewModel<String>(UiState.Loading) {
            suspend fun emit() { setState(UiState.Success("ok")) }
        }
        val vm = ExampleVm()
        val states = vm.test { (this as ExampleVm).emit() }
        assertEquals(listOf(UiState.Loading, UiState.Success("ok")), states)
    }

    @Test
    fun `focus navigation`() = runTest() {
        val focus = UiStateTestHelpers.FocusState()
        focus.moveNext(5)
        focus.moveNext(5)
        focus.movePrevious()
        assertEquals(1, focus.index.value)
    }

    @Test
    fun `navigation tracker records routes`() = runTest() {
        val tracker = UiStateTestHelpers.NavigationTracker()
        tracker.navigate("details/1")
        tracker.navigate("player/1")
        assertEquals(listOf("details/1", "player/1"), tracker.history)
    }
}
