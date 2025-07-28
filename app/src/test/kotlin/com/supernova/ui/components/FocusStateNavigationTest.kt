package com.supernova.ui.components

import com.supernova.testing.JsonFixtureLoader
import com.supernova.testing.UiStateTestHelpers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FocusStateNavigationTest {
    private val loader = JsonFixtureLoader()
    @Test
    fun `focus moves next and previous`() = runTest(dispatchTimeoutMs = 5000) {
        loader.loadJsonFixture("stub_components_fixture.json")
        val focus = UiStateTestHelpers.FocusState()
        focus.moveNext(3)
        focus.moveNext(3)
        focus.movePrevious()
        assertEquals(1, focus.index.value)
    }
}
