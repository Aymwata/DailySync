package com.kiberkot.dailysync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = OnGreenContainer,
    secondary = MintSecondary,
    secondaryContainer = MintContainer,
    background = SoftBackground,
    surface = CardSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenContainer,
    primaryContainer = GreenPrimary,
    secondary = MintContainer,
    background = OnGreenContainer
)

@Composable
fun DailySyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}