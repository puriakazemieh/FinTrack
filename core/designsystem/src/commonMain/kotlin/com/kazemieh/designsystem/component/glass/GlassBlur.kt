package com.kazemieh.designsystem.component.glass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a blur effect if supported by the platform (Android 31+).
 * Note: Compose Multiplatform's [Modifier.blur] works on JVM/Skia based platforms too.
 */
@Composable
fun Modifier.glassBlur(r: Dp = 20.dp): Modifier {
    // For KMP, we can generally use .blur() directly if Skia handles it.
    // The specific Android API 31+ check is mostly relevant for Android-specific views,
    // but in Compose it's safer to keep as a utility.
    return this.blur(r)
}
