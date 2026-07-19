package com.kazemieh.composeApp.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data class Dashboard(
        val showAddTransaction: Boolean = false,
        // -1 means "no draft"; set when opened from a bank-SMS notification deep link.
        val smsDraftId: Long = -1L
    ) : Screen()

    @Serializable
    data class Transactions(
        val resetFilters: Boolean = false,
        val categoryId: Long? = null,
        val sourceId: Long? = null,
        val tagId: Long? = null,
        val personId: Long? = null,
        val transactionType: com.kazemieh.common.model.TransactionType? = null,
        val query: String? = null
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
    data object ManageTools : Screen()

    @Serializable
    data object Search : Screen()

    @Serializable
    data object Budget : Screen()

    @Serializable
    data object Goal : Screen()

    @Serializable
    data object Installment : Screen()

    @Serializable
    data object Categories : Screen()

    @Serializable
    data object Sources : Screen()

    @Serializable
    data object Tags : Screen()

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
    data object AIAdvisor : Screen()

    @Serializable
    data object Achievements : Screen()

    @Serializable
    data class AddAsset(val assetId: Long? = null) : Screen()

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
    data object FxRates : Screen()

    @Serializable
    data object CurrencyConverter : Screen()

    @Serializable
    data object News : Screen()

    @Serializable
    data class ArticleReader(val articleId: String) : Screen()

    @Serializable
    data object FAQ : Screen()

    @Serializable
    data object Support : Screen()

    @Serializable
    data object Events : Screen()

    @Serializable
    data object FinancialCalendar : Screen()

    @Serializable
    data object BottomBarGraph : Screen()

}
