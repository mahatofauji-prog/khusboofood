package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
    lightColorScheme(
        primary = ORANGE,
        onPrimary = WHITE,
        primaryContainer = PRIMARY_YELLOW,
        onPrimaryContainer = TEXT_PRIMARY,
        secondary = DARK_ORANGE,
        onSecondary = WHITE,
        secondaryContainer = SECTION_BACKGROUND,
        onSecondaryContainer = TEXT_PRIMARY,
        tertiary = SuccessGreen,
        background = APP_BACKGROUND,
        surface = CARD_BACKGROUND,
        onBackground = TEXT_PRIMARY,
        onSurface = TEXT_PRIMARY,
        surfaceVariant = SECTION_BACKGROUND,
        onSurfaceVariant = TEXT_SECONDARY,
        outline = BORDER,
        error = ErrorRed
    )

// Enforce Light Yellow/Cream theme across system dark mode as well
private val DarkColorScheme = LightColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Always enforce Light Yellow Theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}
