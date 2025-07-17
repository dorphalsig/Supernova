package com.supernova.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.supernova.ui.theme.SupernovaTheme

@Preview
@Composable
private fun PreviewCategoryCard() {
    SupernovaTheme {
        CategoryCard(name = "Action", imageUrl = null, onClick = {})
    }
}

@Preview
@Composable
private fun PreviewProgramCard() {
    SupernovaTheme {
        ProgramCard(title = "Program")
    }
}
