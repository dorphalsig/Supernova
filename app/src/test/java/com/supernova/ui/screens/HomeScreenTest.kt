package com.supernova.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for [HomeScreen].
 *
 * These tests validate that the screen renders expected headers when the
 * [HomeViewModel] loads its mock content. No Robolectric components are used.
 */
class HomeScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun headersDisplayed() {
        val vm = HomeViewModel().apply { loadHome() }
        rule.setContent {
            HomeScreen(
                onContentSelected = { _, _ -> },
                onRailExpanded = {},
                onSearchClicked = {},
                onProfileClicked = {},
                viewModel = vm
            )
        }
        rule.onNodeWithTag("header_Continue Watching").assertIsDisplayed()
        rule.onNodeWithTag("header_Trending Movies").assertIsDisplayed()
    }
}
