package com.kazemieh.designsystem.picker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.kazemieh.designsystem.*


private data class ThemePalette(
    val light: List<PickableColor>,
    val dark: List<PickableColor>
) {
    fun forTheme(isDark: Boolean) = if (isDark) dark else light

    @Composable
    fun current(): List<PickableColor> = forTheme(isSystemInDarkTheme())
}

object FinTrackPickerColors {

    // --- Helpers ---
    private fun hsv(h: Float, s: Float, v: Float, a: Float = 1f): Color =
        Color.hsv(h, s, v, a)

    private fun rainbowList(count: Int, s: Float, v: Float): List<PickableColor> =
        List(count) { i ->
            val h = (i * 360f) / count
            PickableColor(i + 1, hsv(h, s, v))
        }

    private fun materialLikeList(): List<PickableColor> = buildList(50) {
        val hues = floatArrayOf(0f, 36f, 72f, 108f, 144f, 180f, 216f, 252f, 288f, 324f)
        val tones = floatArrayOf(0.92f, 0.82f, 0.68f, 0.54f, 0.40f) // 5 tone
        var id = 1
        for (h in hues) for (v in tones) {
            add(PickableColor(id++, hsv(h, s = 0.55f, v = v)))
        }
    }

    private fun materialLikeListDark(): List<PickableColor> = buildList(50) {
        val hues = floatArrayOf(0f, 36f, 72f, 108f, 144f, 180f, 216f, 252f, 288f, 324f)
        val tones = floatArrayOf(0.95f, 0.86f, 0.76f, 0.66f, 0.56f)
        var id = 1
        for (h in hues) for (v in tones) {
            add(PickableColor(id++, hsv(h, s = 0.60f, v = v)))
        }
    }

    // 10 seed × 5 tone = 50
    private fun appBased50(
        seeds: List<Color>,
        background: Color,
        onBackground: Color,
        isDark: Boolean
    ): List<PickableColor> {
        require(seeds.size == 10)

        // 5 tone per seed
        val mixA = if (isDark) onBackground else background
        val mixB = if (isDark) background else onBackground

        val aFractions = floatArrayOf(0.35f, 0.20f, 0.00f, -1f, -1f)
        val bFractions = floatArrayOf(-1f, -1f, 0.00f, 0.15f, 0.30f)

        var id = 1
        return buildList(50) {
            for (seed in seeds) {
                // 0,1: نزدیک‌تر به mixA (روشن‌تر تو دارک، روشن‌تر تو لایت)
                add(PickableColor(id++, lerp(seed, mixA, 0.35f)))
                add(PickableColor(id++, lerp(seed, mixA, 0.20f)))
                // 2: خود seed
                add(PickableColor(id++, seed))
                // 3,4: نزدیک‌تر به mixB (تیره‌تر تو لایت، تیره‌تر تو دارک)
                add(PickableColor(id++, lerp(seed, mixB, 0.15f)))
                add(PickableColor(id++, lerp(seed, mixB, 0.30f)))
            }
        }
    }

    // --- 4 Palettes (Light + Dark) ---
    private val rainbowPalette = ThemePalette(
        light = rainbowList(50, s = 0.85f, v = 0.95f),
        dark  = rainbowList(50, s = 0.90f, v = 0.90f)
    )

    private val material3Palette = ThemePalette(
        light = materialLikeList(),
        dark  = materialLikeListDark()
    )

    private val pastelPalette = ThemePalette(
        light = rainbowList(50, s = 0.25f, v = 0.98f),
        dark  = rainbowList(50, s = 0.32f, v = 0.88f)
    )

    private val appThemePalette = ThemePalette(
        light = appBased50(
            seeds = listOf(
                md_theme_light_primary,
                md_theme_light_primaryContainer,
                md_theme_light_secondary,
                md_theme_light_secondaryContainer,
                md_theme_light_tertiary,
                md_theme_light_tertiaryContainer,
                md_theme_light_error,
                md_theme_light_errorContainer,
                md_theme_light_outline,
                md_theme_light_outlineVariant
            ),
            background = md_theme_light_background,
            onBackground = md_theme_light_onBackground,
            isDark = false
        ),
        dark = appBased50(
            seeds = listOf(
                md_theme_dark_primary,
                md_theme_dark_primaryContainer,
                md_theme_dark_secondary,
                md_theme_dark_secondaryContainer,
                md_theme_dark_tertiary,
                md_theme_dark_tertiaryContainer,
                md_theme_dark_error,
                md_theme_dark_errorContainer,
                md_theme_dark_outline,
                md_theme_dark_outlineVariant
            ),
            background = md_theme_dark_background,
            onBackground = md_theme_dark_onBackground,
            isDark = true
        )
    )

    // --- Public API (auto theme) ---
    @Composable fun rainbow()   = rainbowPalette.current()
    @Composable fun material3() = material3Palette.current()
    @Composable fun pastel()    = pastelPalette.current()
    @Composable fun appTheme()  = appThemePalette.current()

    // اگر خودت boolean تم داری:
    fun rainbow(isDark: Boolean)   = rainbowPalette.forTheme(isDark)
    fun material3(isDark: Boolean) = material3Palette.forTheme(isDark)
    fun pastel(isDark: Boolean)    = pastelPalette.forTheme(isDark)
    fun appTheme(isDark: Boolean)  = appThemePalette.forTheme(isDark)
}