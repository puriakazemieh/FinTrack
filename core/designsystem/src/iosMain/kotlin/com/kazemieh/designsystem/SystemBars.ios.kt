package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun SystemBarsAppearance(
    darkTheme: Boolean,
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    // Status bar styling on iOS is handled by the hosting UIViewController.
}
