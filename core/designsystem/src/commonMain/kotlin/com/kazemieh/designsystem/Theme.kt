package com.kazemieh.designsystem


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
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
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
)

private val DarkColors = darkColorScheme(
    primary = GlassGreen,
    onPrimary = GlassGreenDark, // Standardized contrast color
    primaryContainer = GlassGreenDeep, // For gradients
    onPrimaryContainer = GlassGreenSoft, // For semantic backgrounds
    secondary = GlassBlue,
    onSecondary = GlassBg0,
    secondaryContainer = GlassBlueSoft,
    onSecondaryContainer = GlassBlue,
    tertiary = GlassPurple,
    onTertiary = GlassBg0,
    background = GlassBg0,
    onBackground = GlassText,
    surface = GlassBgAccent,
    onSurface = GlassText,
    surfaceVariant = GlassColor, // Used for glass surfaces
    onSurfaceVariant = GlassText2,
    outline = GlassEdge,
    outlineVariant = GlassHairline,
    error = GlassRed,
    onError = GlassBg0,
    errorContainer = GlassRedSoft,
    onErrorContainer = GlassRed,
)


@Composable
fun FintrackTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(LocalSpacing provides Dimensions()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = fintrackTypography(),
            shapes = FintrackShapes,
            content = content
        )
    }

}
