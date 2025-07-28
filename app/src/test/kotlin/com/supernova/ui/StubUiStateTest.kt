package com.supernova.ui

import com.supernova.testing.JsonFixtureLoader
import com.supernova.testing.UiState
import com.supernova.testing.UiStateTestHelpers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StubUiStateTest {
    private val loader = JsonFixtureLoader()

    @Test
    fun `view model emits success state`() = runTest {
        val json = loader.loadAsMap("stub_components_fixture.json")
        class Vm : UiStateTestHelpers.TestViewModel<String>(UiState.Loading) {
            suspend fun emit(title: String) { setState(UiState.Success(title)) }
        }
        val vm = Vm()
        val states = vm.test { (this as Vm).emit(json["title"].toString()) }
        UiStateTestHelpers.assertLoading(states.first())
        UiStateTestHelpers.assertSuccess(states.last()) {
            assertEquals("\"Test Title\"", it)
        }
    }
}
