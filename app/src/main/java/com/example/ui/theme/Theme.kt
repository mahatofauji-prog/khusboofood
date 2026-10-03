package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlackGoldColorScheme =
    darkColorScheme(
        primary = PRIMARY_GOLD,
        onPrimary = Color(0xFF0B0B0B),
        primaryContainer = GOLD_CONTAINER,
        onPrimaryContainer = BRIGHT_GOLD,
        secondary = BRIGHT_GOLD,
        onSecondary = Color(0xFF0B0B0B),
        secondaryContainer = SECTION_BACKGROUND,
        onSecondaryContainer = TEXT_PRIMARY,
        tertiary = SUCCESS_GREEN,
        onTertiary = Color(0xFF0B0B0B),
        background = APP_BACKGROUND,
        onBackground = TEXT_PRIMARY,
        surface = CARD_BACKGROUND,
        onSurface = TEXT_PRIMARY,
        surfaceVariant = SECTION_BACKGROUND,
        onSurfaceVariant = TEXT_SECONDARY,
        outline = BORDER,
        outlineVariant = BORDER_GOLD,
        error = ERROR_RED,
        onError = Color(0xFFFFFFFF)
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BlackGoldColorScheme,
        typography = Typography,
        content = content
    )
}
