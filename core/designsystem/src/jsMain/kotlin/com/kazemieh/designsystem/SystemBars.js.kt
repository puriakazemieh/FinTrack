package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun SystemBarsAppearance(
    darkTheme: Boolean,
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    // No native system bars on web.
}
