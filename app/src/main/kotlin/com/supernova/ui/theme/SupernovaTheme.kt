package com.supernova.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

private val LightColors = lightColorScheme(
    primary = Color(0xFF00BCD4),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color.Black,
    background = Color(0xFFF5F5F5),
    onBackground = Color.Black,
    surface = Color(0xFFFFFFFF),
    onSurface = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF00BCD4),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF006064),
    onPrimaryContainer = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White
)

@Composable
fun SupernovaTheme(
    darkTheme: Boolean = false,
    colorScheme: ColorScheme = if (darkTheme) DarkColors else LightColors,
    typography: Typography = Typography(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Preview
@Composable
private fun ThemePreviewLight() {
    SupernovaTheme(darkTheme = false) {}
}

@Preview
@Composable
private fun ThemePreviewDark() {
    SupernovaTheme(darkTheme = true) {}
}
