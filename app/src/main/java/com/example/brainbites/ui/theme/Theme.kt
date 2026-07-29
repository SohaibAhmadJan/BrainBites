package com.example.brainbites.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    onSecondary = Color.White,
    background = md_theme_light_background,
    surface = md_theme_light_surface,
    onSurface = DarkGreenPrimary,
    secondaryContainer = SoftBackground,
    onSecondaryContainer = md_theme_light_primary,
    tertiary = md_theme_light_accent
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    secondary = Color(0xFF1B4332), // Darker green for Hero Card in dark mode
    onSecondary = Color.White,
    background = md_theme_dark_background,
    surface = Color(0xFF162C25), // Solid charcoal green for cards
    onSurface = Color(0xFFF1FAEE),
    secondaryContainer = Color(0xFF1B4332),
    onSecondaryContainer = Color(0xFF52B788),
    tertiary = md_theme_dark_accent
)

@Composable
fun BrainBitesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    textScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val scaledTypography = getScaledTypography(textScale)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}

private fun getScaledTypography(scale: Float): Typography {
    return Typography(
        displayLarge = AppTypography.displayLarge.scale(scale),
        displayMedium = AppTypography.displayMedium.scale(scale),
        displaySmall = AppTypography.displaySmall.scale(scale),
        headlineLarge = AppTypography.headlineLarge.scale(scale),
        headlineMedium = AppTypography.headlineMedium.scale(scale),
        headlineSmall = AppTypography.headlineSmall.scale(scale),
        titleLarge = AppTypography.titleLarge.scale(scale),
        titleMedium = AppTypography.titleMedium.scale(scale),
        titleSmall = AppTypography.titleSmall.scale(scale),
        bodyLarge = AppTypography.bodyLarge.scale(scale),
        bodyMedium = AppTypography.bodyMedium.scale(scale),
        bodySmall = AppTypography.bodySmall.scale(scale),
        labelLarge = AppTypography.labelLarge.scale(scale),
        labelMedium = AppTypography.labelMedium.scale(scale),
        labelSmall = AppTypography.labelSmall.scale(scale)
    )
}

private fun TextStyle.scale(scale: Float): TextStyle {
    if (scale == 1.0f) return this
    
    val newFontSize = if (this.fontSize.isSp) this.fontSize * scale else this.fontSize
    val newLineHeight = if (this.lineHeight.isSp) this.lineHeight * scale else this.lineHeight
    
    return this.copy(
        fontSize = newFontSize,
        lineHeight = newLineHeight
    )
}
