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
    data object ThemeSettings : Screen()

    @Serializable
    data object CurrencySettings : Screen()

    @Serializable
    data object EditProfile : Screen()

    @Serializable
    data object NotificationSettings : Screen()

    @Serializable
    data object Search : Screen()

    @Serializable
    data object Budget : Screen()

    @Serializable
    data object Installment : Screen()

    @Serializable
    data object Person : Screen()

    @Serializable
    data class PersonDetail(val personId: Long) : Screen()

    @Serializable
    data object Debt : Screen()

    @Serializable
    data object Check : Screen()

    @Serializable
    data object FixedExpense : Screen()

    @Serializable
    data object Assets : Screen()

    @Serializable
    data object AddAsset : Screen()

    @Serializable
    data object Shopping : Screen()

    @Serializable
    data object Notes : Screen()

    @Serializable
    data class NoteEdit(val noteId: Long = 0) : Screen()

    @Serializable
    data object Onboarding : Screen()

    @Serializable
    data object BackupRestore : Screen()

    @Serializable
    data object BottomBarGraph : Screen()

}
