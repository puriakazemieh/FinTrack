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
        val transactionType: com.kazemieh.common.model.TransactionType? = null
    ) : Screen()

    @Serializable
    data object Tools : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object ThemeAndCurrency : Screen()

    @Serializable
    data object ProfileEdit : Screen()

    @Serializable
    data object NotificationSettings : Screen()

    @Serializable
    data object Search : Screen()

    @Serializable
    data object Budget : Screen()

    @Serializable
    data object Installment : Screen()

    @Serializable
    data object Onboarding : Screen()

    @Serializable
    data object BottomBarGraph : Screen()

}
