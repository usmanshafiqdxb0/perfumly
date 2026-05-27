package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LuxuryDarkColorScheme = darkColorScheme(
    primary = LuxuryGold,
    secondary = WarmBronze,
    tertiary = ImperialOdu,
    background = ObsidianBlack,
    surface = OnyxSurface,
    onPrimary = ObsidianBlack,
    onSecondary = ObsidianBlack,
    onBackground = PlatinumWhite,
    onSurface = PlatinumWhite,
    surfaceVariant = CharcoalGray,
    onSurfaceVariant = PureSilver
)

@Composable
fun MyApplicationTheme(
    // We strictly enforce the premium noir-lux dark mode as requested by user
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = LuxuryDarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ObsidianBlack.toArgb()
            window.navigationBarColor = ObsidianBlack.toArgb()
            
            // False means status bar text color will be light (white/silver) 
            // since status bar is black/dark
            val decorView = window.decorView
            val wic = WindowCompat.getInsetsController(window, decorView)
            wic.isAppearanceLightStatusBars = false
            wic.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
