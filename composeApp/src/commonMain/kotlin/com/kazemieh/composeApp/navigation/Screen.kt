package com.kazemieh.composeApp.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data object Dashboard : Screen()

    @Serializable
    data class Transactions(
        val resetFilters: Boolean = false,
        val categoryId: Long? = null,
        val sourceId: Long? = null,
        val tagId: Long? = null,
        val personId: Long? = null,
    ) : Screen()

    @Serializable
    data object Tools : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object BottomBarGraph : Screen()

}
