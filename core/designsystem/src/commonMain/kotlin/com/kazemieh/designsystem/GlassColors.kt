package com.kazemieh.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Glass Design Tokens from §1.1 of COMPONENTS-AND-TOKENS.md
 */
val GlassBg0 = Color(0xFF06100E)
val GlassBg1 = Color(0xFF0A1714)
val GlassBgAccent = Color(0xFF0C1D18)
val GlassColor = Color(0x0BFFFFFF) // C.glass
val GlassHover = Color(0x12FFFFFF)
val GlassStrong = Color(0x14FFFFFF)
val GlassEdge = Color(0x16FFFFFF)
val GlassEdgeStrong = Color(0x24FFFFFF)
val GlassHairline = Color(0x0FFFFFFF)
val GlassGreen = Color(0xFF22C55E)
val GlassGreenSoft = Color(0x2922C55E)
val GlassGreenDeep = Color(0xFF16A34A)
val GlassGreenDark = Color(0xFF06281A) // For FAB/CTA icons on green bg
val GlassRed = Color(0xFFEF4444)
val GlassRedSoft = Color(0x24EF4444)
val GlassAmber = Color(0xFFF59E0B)
val GlassAmberSoft = Color(0x24F59E0B)
val GlassBlue = Color(0xFF60A5FA)
val GlassBlueSoft = Color(0x2460A5FA)
val GlassPurple = Color(0xFFA78BFA)
val GlassText = Color(0xFFF3F5F4)
val GlassText2 = Color(0xA6F3F5F4)
val GlassText3 = Color(0x6BF3F5F4)

val DarkGlassText = Color(0xFFF3F5F4)
val DarkGlassText2 = Color(0xA6F3F5F4)
val DarkGlassText3 = Color(0x6BF3F5F4)

val LightGlassText = Color(0xFF06100E)
val LightGlassText2 = Color(0xFF4B5563)
val LightGlassText3 = Color(0xFF9CA3AF)

@Immutable
data class GlassColors(
    val bg0: Color,
    val bg1: Color,
    val bgAccent: Color,
    val glass: Color,
    val glassHover: Color,
    val glassStrong: Color,
    val glassEdge: Color,
    val glassEdgeStrong: Color,
    val glassHairline: Color,
    val text: Color,
    val text2: Color,
    val text3: Color
)

val DarkGlassColors = GlassColors(
    bg0 = GlassBg0,
    bg1 = GlassBg1,
    bgAccent = GlassBgAccent,
    glass = GlassColor,
    glassHover = GlassHover,
    glassStrong = GlassStrong,
    glassEdge = GlassEdge,
    glassEdgeStrong = GlassEdgeStrong,
    glassHairline = GlassHairline,
    text = DarkGlassText,
    text2 = DarkGlassText2,
    text3 = DarkGlassText3
)

val LightGlassColors = GlassColors(
    bg0 = Color(0xFFF0F4F3),
    bg1 = Color(0xFFFFFFFF),
    bgAccent = Color.White,
    glass = Color(0x0B000000), // Matching GlassColor logic but for light
    glassHover = Color(0x12000000),
    glassStrong = Color(0x14000000),
    glassEdge = Color(0xFFE5E7EB),
    glassEdgeStrong = Color(0xFFD1D5DB),
    glassHairline = Color(0xFFF3F4F6),
    text = LightGlassText,
    text2 = LightGlassText2,
    text3 = LightGlassText3
)

val PlainDarkGlassColors = GlassColors(
    bg0 = md_theme_dark_background,
    bg1 = Color(0xFF2A2E2A), // Slightly lighter than bg0
    bgAccent = md_theme_dark_surface,
    glass = Color(0xFF2E332E), // Solid color fallback
    glassHover = Color(0xFF383D38),
    glassStrong = Color(0xFF424742),
    glassEdge = md_theme_dark_outlineVariant,
    glassEdgeStrong = md_theme_dark_outline,
    glassHairline = Color(0xFF2D322D),
    text = md_theme_dark_onBackground,
    text2 = md_theme_dark_onSurface.copy(alpha = 0.7f),
    text3 = md_theme_dark_onSurface.copy(alpha = 0.5f)
)

val PlainLightGlassColors = GlassColors(
    bg0 = md_theme_light_background,
    bg1 = Color(0xFFF1F4EE), // Slightly darker/different than bg0
    bgAccent = md_theme_light_surface,
    glass = Color(0xFFEFF2EC), // Solid color fallback
    glassHover = Color(0xFFE5E9E1),
    glassStrong = Color(0xFFDBE1D6),
    glassEdge = md_theme_light_outlineVariant,
    glassEdgeStrong = md_theme_light_outline,
    glassHairline = Color(0xFFF5F7F2),
    text = md_theme_light_onBackground,
    text2 = md_theme_light_onSurface.copy(alpha = 0.7f),
    text3 = md_theme_light_onSurface.copy(alpha = 0.5f)
)

val LocalGlassColors = staticCompositionLocalOf { DarkGlassColors }
