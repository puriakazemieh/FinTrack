package com.kazemieh.composeApp.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data object Dashboard: Screen()

    @Serializable
    data object Report : Screen()

    @Serializable
    data object Setting : Screen()

    @Serializable
    data object BottomBarGraph : Screen()


}