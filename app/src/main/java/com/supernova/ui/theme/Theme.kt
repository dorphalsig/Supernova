package com.supernova.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Custom Supernova Color Palette
object SupernovaColors {
    val Background = Color(0xFF191b22)
    val Surface = Color(0xFF23253a)
    val SurfaceVariant = Color(0xFF313358)
    val OnSurface = Color(0xFFf0f2fd)
    val OnSurfaceVariant = Color(0xFFaaaad6)
    val Primary = Color(0xFF829afe)
    val PrimaryContainer = Color(0xFF363c5c)
    val Focus = Color(0xFF70b3fe)
    val Accent = Color(0xFFff7cae)
    val Border = Color(0x99829afe)
    val LockBackground = Color(0xFF232344)
    val Shadow = Color(0x660b0b1a)
}

// Dark color scheme using custom Supernova colors
private val SupernovaDarkColorScheme = darkColorScheme(
    primary = SupernovaColors.Primary,
    onPrimary = SupernovaColors.Background,
    primaryContainer = SupernovaColors.PrimaryContainer,
    onPrimaryContainer = SupernovaColors.OnSurface,

    secondary = SupernovaColors.Focus,
    onSecondary = SupernovaColors.Background,
    secondaryContainer = SupernovaColors.SurfaceVariant,
    onSecondaryContainer = SupernovaColors.OnSurface,

    tertiary = SupernovaColors.Accent,
    onTertiary = SupernovaColors.Background,
    tertiaryContainer = SupernovaColors.LockBackground,
    onTertiaryContainer = SupernovaColors.OnSurface,

    surface = SupernovaColors.Surface,
    onSurface = SupernovaColors.OnSurface,
    surfaceVariant = SupernovaColors.SurfaceVariant,
    onSurfaceVariant = SupernovaColors.OnSurfaceVariant,

    background = SupernovaColors.Background,
    onBackground = SupernovaColors.OnSurface,

    error = SupernovaColors.Accent,
    onError = SupernovaColors.OnSurface,
    errorContainer = SupernovaColors.LockBackground,
    onErrorContainer = SupernovaColors.OnSurface,

    outline = SupernovaColors.Border,
    outlineVariant = SupernovaColors.OnSurfaceVariant,

    surfaceTint = SupernovaColors.Primary,
    inverseSurface = SupernovaColors.OnSurface,
    inverseOnSurface = SupernovaColors.Surface,
    inversePrimary = SupernovaColors.PrimaryContainer,

    scrim = SupernovaColors.Shadow
)

// Light color scheme (keeping dark theme dominant but providing fallback)
private val SupernovaLightColorScheme = lightColorScheme(
    primary = SupernovaColors.Primary,
    onPrimary = Color.White,
    primaryContainer = SupernovaColors.PrimaryContainer,
    onPrimaryContainer = SupernovaColors.OnSurface,

    secondary = SupernovaColors.Focus,
    onSecondary = Color.White,
    secondaryContainer = SupernovaColors.SurfaceVariant,
    onSecondaryContainer = SupernovaColors.OnSurface,

    surface = Color.White,
    onSurface = SupernovaColors.Background,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = SupernovaColors.OnSurfaceVariant,

    background = Color.White,
    onBackground = SupernovaColors.Background,

    error = SupernovaColors.Accent,
    onError = Color.White,

    outline = SupernovaColors.Border,
    outlineVariant = SupernovaColors.OnSurfaceVariant
)

@Composable
fun SupernovaTheme(
    darkTheme: Boolean = true, // Default to dark theme for TV
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        SupernovaDarkColorScheme
    } else {
        SupernovaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}