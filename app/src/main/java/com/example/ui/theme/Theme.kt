package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArcadeColorScheme = darkColorScheme(
    primary = RiftGreen,
    onPrimary = Color(0xFF101908),
    primaryContainer = Color(0xFF263914),
    onPrimaryContainer = RiftGreen,
    secondary = FlameCrimson,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF45140E),
    onSecondaryContainer = FlameOrange,
    tertiary = CyberPurple,
    onTertiary = Color.White,
    background = ArcadeDarkBg,
    onBackground = ArcadeTextLight,
    surface = ArcadeDarkSurface,
    onSurface = ArcadeTextLight,
    surfaceVariant = ArcadeDarkSurfaceVariant,
    onSurfaceVariant = ArcadeTextMuted,
    outline = ArcadeBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcadeColorScheme,
        typography = Typography,
        content = content
    )
}
