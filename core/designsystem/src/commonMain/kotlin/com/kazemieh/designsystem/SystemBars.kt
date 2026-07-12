package com.kazemieh.designsystem

import androidx.compose.runtime.Composable

/**
 * Keeps the platform status/navigation bar icons legible against the current theme:
 * dark icons on light themes, light icons on dark themes. Applied eagerly so the bars
 * recolor in lock-step with theme (and tab) changes instead of lagging a frame behind.
 * No-op on non-Android targets.
 */
@Composable
expect fun SystemBarsAppearance(darkTheme: Boolean)
