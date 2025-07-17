package com.supernova.ui.screens

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
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun providerSettingsItemInvokesCallback() {
        var clicked = false
        composeRule.setContent {
            SupernovaTheme {
                SettingsMenuItem(
                    icon = androidx.compose.material.icons.Icons.Default.Tune,
                    title = "Provider Settings",
                    description = "",
                    onClick = { clicked = true },
                    tag = "provider"
                )
            }
        }
        composeRule.onNodeWithTag("menu_provider").performClick()
        assertTrue(clicked)
    }
}
