package com.example.brainbites.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    error = md_theme_light_error,
    onError = md_theme_light_onError
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError
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
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
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
