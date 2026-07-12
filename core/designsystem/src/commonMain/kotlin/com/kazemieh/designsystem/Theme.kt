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
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
)

private val GlassDarkColors = darkColorScheme(
    primary = GlassGreen,
    onPrimary = GlassGreenDark,
    primaryContainer = GlassGreenDeep,
    onPrimaryContainer = GlassGreenSoft,
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
    surfaceVariant = GlassColor,
    onSurfaceVariant = GlassText2,
    outline = GlassEdge,
    outlineVariant = GlassHairline,
    error = GlassRed,
    onError = GlassBg0,
    errorContainer = GlassRedSoft,
    onErrorContainer = GlassRed,
)

private val GlassLightColors = lightColorScheme(
    primary = GlassGreenDeep,
    onPrimary = Color.White,
    primaryContainer = GlassGreenSoft,
    onPrimaryContainer = GlassGreenDeep,
    secondary = GlassBlue,
    onSecondary = Color.White,
    secondaryContainer = GlassBlueSoft,
    onSecondaryContainer = GlassBlue,
    tertiary = GlassPurple,
    onTertiary = Color.White,
    background = Color(0xFFF0F4F3),
    onBackground = Color(0xFF06100E),
    surface = Color.White,
    onSurface = Color(0xFF06100E),
    surfaceVariant = Color(0x0B000000),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFF3F4F6),
    error = GlassRed,
    onError = Color.White,
    errorContainer = GlassRedSoft,
    onErrorContainer = GlassRed,
)


@Composable
fun FintrackTheme(
    theme: AppTheme = AppTheme.GLASS_DARK,
    accent: AccentPalette = AccentPalette.Default,
    content: @Composable () -> Unit
) {
    val baseColorScheme = when (theme) {
        AppTheme.GLASS_DARK -> GlassDarkColors
        AppTheme.GLASS_LIGHT -> GlassLightColors
        AppTheme.PLAIN_DARK -> DarkColors
        AppTheme.PLAIN_LIGHT -> LightColors
    }

    val baseGlassColors = when (theme) {
        AppTheme.GLASS_DARK -> DarkGlassColors
        AppTheme.GLASS_LIGHT -> LightGlassColors
        AppTheme.PLAIN_DARK -> PlainDarkGlassColors
        AppTheme.PLAIN_LIGHT -> PlainLightGlassColors
    }

    // Recolor the primary/accent roles from the user's chosen palette. On light
    // themes the deeper shade keeps contrast against bright backgrounds.
    val isLight = theme == AppTheme.GLASS_LIGHT || theme == AppTheme.PLAIN_LIGHT
    val primaryColor = if (isLight) accent.accentDeep else accent.accent
    val onPrimaryColor = if (isLight) Color.White else accent.onAccent

    val colorScheme = baseColorScheme.copy(
        primary = primaryColor,
        onPrimary = onPrimaryColor,
        primaryContainer = accent.accentSoft,
        onPrimaryContainer = if (isLight) accent.accentDeep else accent.accent
    )

    val glassColors = baseGlassColors.copy(
        accent = accent.accent,
        accentSoft = accent.accentSoft,
        accentDeep = accent.accentDeep,
        onAccent = accent.onAccent
    )

    SystemBarsAppearance(darkTheme = !isLight)

    CompositionLocalProvider(
        LocalSpacing provides Dimensions(),
        LocalGlassColors provides glassColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = fintrackTypography(),
            shapes = FintrackShapes,
            content = content
        )
    }
}
