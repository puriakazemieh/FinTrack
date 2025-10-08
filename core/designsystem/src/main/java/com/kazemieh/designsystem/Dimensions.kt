package com.kazemieh.designsystem

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val borderPadding: Dp = 16.dp,
    val verticalPadding: Dp = 10.dp,
    val buttonHeight: Dp = 60.dp,
    val topPadding: Dp = 4.dp,
    val bottomPadding: Dp = 4.dp,
    val menuItemHeight: Dp = 160.dp,
    val categoryHeight: Dp = 48.dp,


    val smallPadding: Dp = 8.dp,
    val mediumPadding: Dp = 12.dp,
    val sidePadding: Dp = 16.dp,
    val largePadding: Dp = 24.dp,

    val strokeWidth: Dp = 2.dp,

    val xSmallIconSize: Dp = 16.dp,
    val smallIconSize: Dp = 24.dp,

    val defaultMinWidth: Dp = 96.dp,

    val headerPadding: Dp = 32.dp
)

@Composable
@SuppressLint("ConfigurationScreenWidthHeight")
fun rememberDimensions(): Dimensions {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    return when {
        screenHeight < 300.dp -> Dimensions(
            borderPadding = 8.dp,
            verticalPadding = 5.dp,
            //topPadding = 20.dp,
            menuItemHeight = 100.dp
        )

        screenHeight in (300.dp..500.dp) -> Dimensions(
            borderPadding = 8.dp,
            verticalPadding = 5.dp,
            menuItemHeight = 100.dp
        )

        screenHeight in (500.dp..600.dp) -> Dimensions(
            verticalPadding = 5.dp,
            // topPadding = 30.dp,
            menuItemHeight = 140.dp,
        )

        else -> Dimensions()
    }
}

@Composable
@SuppressLint("ConfigurationScreenWidthHeight")
fun isNormalScreen(): Boolean {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    return screenHeight > 500.dp
}