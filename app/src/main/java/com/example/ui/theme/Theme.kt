package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRedDark,
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = SecondaryOrange,
    onSecondary = OnSecondaryGold,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF3E2C29),
    onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDC3129),
    onPrimaryContainer = Color.White,
    secondary = SecondaryOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFA504),
    onSecondaryContainer = OnSecondaryGold,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = Color(0xFFFFF8F7),
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerHighestLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = Color(0xFF906F6B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
