package com.supernova.ui.screens

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.supernova.ui.theme.SupernovaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class SearchResultsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun seeAllButtonEmitsCallback() {
        var clicked = false
        rule.setContent {
            SupernovaTheme {
                SearchResultsScreen(
                    onItemSelected = { _, _ -> },
                    onSeeAll = { clicked = true },
                    onBackPressed = {}
                )
            }
        }
        rule.onNodeWithTag("see_all").performClick()
        assertTrue(clicked)
    }

    @Test
    fun searchBarFocusedOnLaunch() {
        rule.setContent {
            SupernovaTheme {
                SearchResultsScreen(
                    onItemSelected = { _, _ -> },
                    onSeeAll = {},
                    onBackPressed = {}
                )
            }
        }
        rule.onNodeWithTag("search_bar").assertIsFocused()
    }
}
