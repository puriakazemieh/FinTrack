package com.kazemieh.designsystem

import androidx.compose.ui.graphics.Color

/**
 * User-selectable accent palettes. The app historically shipped a single green
 * accent hard-wired through [GlassGreen]; this enum lets the primary/accent color
 * be swapped app-wide from settings. Each palette carries the four accent roles the
 * theme needs — [accent] (main), [accentSoft] (tinted fill), [accentDeep] (darker,
 * used as primary on light themes) and [onAccent] (content on top of the accent).
 *
 * The default [EMERALD] maps to the existing green tokens so behaviour is unchanged
 * until the user picks another palette.
 */
enum class AccentPalette(
    val accent: Color,
    val accentSoft: Color,
    val accentDeep: Color,
    val onAccent: Color
) {
    EMERALD(GlassGreen, GlassGreenSoft, GlassGreenDeep, GlassGreenDark),
    BLUE(GlassBlue, GlassBlueSoft, Color(0xFF2563EB), Color(0xFF06121F)),
    PURPLE(GlassPurple, Color(0x24A78BFA), Color(0xFF7C3AED), Color(0xFF120C1F)),
    AMBER(GlassAmber, GlassAmberSoft, Color(0xFFD97706), Color(0xFF1F1405)),
    ROSE(Color(0xFFFB7185), Color(0x24FB7185), Color(0xFFE11D48), Color(0xFF1F0A0E)),
    GOLD(GlassGold, GlassGoldSoft, Color(0xFFC79A1E), Color(0xFF1F1B05));

    companion object {
        val Default = EMERALD

        fun fromName(name: String?): AccentPalette =
            entries.firstOrNull { it.name == name } ?: Default
    }
}
