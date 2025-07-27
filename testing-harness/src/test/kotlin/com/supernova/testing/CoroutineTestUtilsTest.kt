package com.supernova.testing

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CoroutineTestUtilsTest : TestEntityFactory() {

    @Test
    fun `runTest executes coroutine block`() {
        CoroutineTestUtils.runTest {
            var result = 0
            launch { delay(100); result = 1 }
            CoroutineTestUtils.advanceTimeBy(this, 100)
            CoroutineTestUtils.advanceUntilIdle(this)
            assertEquals(1, result)
        }
    }

    @Test
    fun `viewModelScope launches work`() {
        CoroutineTestUtils.runTest {
            val scope = CoroutineTestUtils.viewModelScope()
            var output = ""
            scope.launch { delay(50); output = "done" }
            CoroutineTestUtils.advanceTimeBy(scope, 50)
            assertEquals("done", output)
        }
    }

    @Test
    fun `dispatcher provider uses injected dispatcher`() {
        CoroutineTestUtils.runTest {
            val d = CoroutineTestUtils.testDispatcher()
            val provider = CoroutineTestUtils.dispatcherProvider(d)
            assertEquals(d, provider.io)
            assertEquals(d, provider.main)
            assertEquals(d, provider.default)
        }
    }

    @Test
    fun `load fixture within runTest`() {
        CoroutineTestUtils.runTest {
            val loader = JsonFixtureLoader()
            val json = loader.loadJsonFixture("coroutine_sample.json")
            val obj = Json.parseToJsonElement(json).jsonObject
            assertEquals("ok", obj["result"]?.jsonPrimitive?.content)
        }
    }
}
