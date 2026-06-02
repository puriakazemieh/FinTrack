package com.kazemieh.composeApp.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data object Dashboard: Screen()

    @Serializable
    data object Transactions : Screen()

    @Serializable
    data object Tools : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object BottomBarGraph : Screen()

}
