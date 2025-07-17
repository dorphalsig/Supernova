package com.supernova.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentLibraryTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun focusableButtonEmitsClick() {
        var clicked = false
        rule.setContent {
            FocusableButton(onClick = { clicked = true }, modifier = Modifier.testTag("btn")) {
                androidx.compose.material3.Text("Btn")
            }
        }
        rule.onNodeWithTag("btn").performClick()
        assert(clicked)
    }

    @Test
    fun categoryCardDisplaysName() {
        rule.setContent { TestCategoryCard() }
        rule.onNodeWithTag("category").assertIsDisplayed()
    }

    @Composable
    private fun TestCategoryCard() {
        CategoryCard(name = "Sports", imageUrl = null, onClick = {}, modifier = Modifier.testTag("category"))
    }
}
