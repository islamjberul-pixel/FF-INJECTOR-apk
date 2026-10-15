package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FFDataInjectorColorScheme = darkColorScheme(
    primary = GamingOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = GamingOrangeContainer,
    onPrimaryContainer = Color(0xFFFFDBC8),

    secondary = GamingNeonBlue,
    onSecondary = Color(0xFF041E28),
    secondaryContainer = GamingBlueContainer,
    onSecondaryContainer = Color(0xFFBAE6FD),

    tertiary = GamingCobaltBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1E3A8A),
    onTertiaryContainer = Color(0xFFDBEAFE),

    background = GamingDarkBg,
    onBackground = GamingTextPrimary,
    surface = GamingSurface,
    onSurface = GamingTextPrimary,
    surfaceVariant = GamingSurfaceElevated,
    onSurfaceVariant = GamingTextSecondary,

    error = GamingRedError,
    onError = Color.White,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFCA5A5),

    outline = GamingBorder,
    outlineVariant = Color(0xFF1E293B)
)

@Composable
fun FFDataInjectorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FFDataInjectorColorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for template compatibility if needed
@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    FFDataInjectorTheme(content = content)
}
