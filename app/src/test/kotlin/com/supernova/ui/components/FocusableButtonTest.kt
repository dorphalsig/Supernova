package com.supernova.ui.components

import com.supernova.testing.JsonFixtureLoader
import com.supernova.testing.UiStateTestHelpers.FocusState
import com.supernova.testing.TestEntityFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FocusableButtonTest : TestEntityFactory() {
    private val loader = JsonFixtureLoader()

    @Test
    fun `load button label`() = runTest() {
        val jsonStr = loader.loadJsonFixture("button_label.json")
        val json = loader.loadAsMap("button_label.json")
        val label = (json["label"] as JsonPrimitive).content
        assertEquals("Play", label)
    }

    @Test
    fun `focus state navigation`() = runTest() {
        val focus = FocusState()
        focus.moveNext(3)
        focus.moveNext(3)
        focus.movePrevious()
        assertEquals(1, focus.index.value)
    }
}
