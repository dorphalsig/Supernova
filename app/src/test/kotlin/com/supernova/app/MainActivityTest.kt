package com.supernova.app

import com.supernova.testing.JsonFixtureLoader
import com.supernova.testing.UiStateTestHelpers
import com.supernova.ui.navigation.SupernovaDestinations
import com.supernova.testing.UiState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class MainActivityTest : JsonFixtureLoader() {
    @Test
    fun `navigation tracker records routes`() = runTest {
        val tracker = UiStateTestHelpers.NavigationTracker()
        tracker.navigate(SupernovaDestinations.Home.route)
        tracker.navigate(SupernovaDestinations.Details.route)
        assertEquals(listOf("home", "details"), tracker.history)
    }

    @Test
    fun `ui state success loads fixture`() = runTest {
        val json = loadJsonFixture("main_activity_navigation.json")
        UiStateTestHelpers.assertSuccess(UiState.Success(json)) { data ->
            assertEquals(json, data)
        }
    }
}
