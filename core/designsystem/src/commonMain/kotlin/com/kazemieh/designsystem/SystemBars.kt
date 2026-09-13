package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Keeps the platform status/navigation bars and icons in sync with the current theme.
 * Applying their background color explicitly prevents the previous screen from showing
 * through the bars during navigation transitions.
 * No-op on non-Android targets.
 */
@Composable
expect fun SystemBarsAppearance(
    darkTheme: Boolean,
    statusBarColor: Color,
    navigationBarColor: Color,
)
