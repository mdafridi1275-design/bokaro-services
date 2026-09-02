package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BokaroBlueLight,
    onPrimary = BokaroBlueDark,
    primaryContainer = BokaroBlueDark,
    onPrimaryContainer = BokaroBlueLight,
    secondary = BokaroSky,
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
)

private val LightColorScheme = lightColorScheme(
    primary = BokaroBluePrimary,
    onPrimary = Color.White,
    primaryContainer = BokaroBlueContainer,
    onPrimaryContainer = BokaroBlueDark,
    secondary = BokaroSky,
    onSecondary = Color.White,
    secondaryContainer = BokaroSkyContainer,
    onSecondaryContainer = Color(0xFF004953),
    background = CanvasBg,
    onBackground = TextPrimary,
    surface = CardWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
