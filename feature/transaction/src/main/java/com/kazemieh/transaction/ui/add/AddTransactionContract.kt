package com.kazemieh.transaction.ui.add

import com.kazemieh.model.Category
import com.kazemieh.model.FinancialSource
import com.kazemieh.model.Tag


sealed interface AddTransactionEvent {
    data class SetAmount(val amount: String) : AddTransactionEvent
    data class SetCategory(val category: Category) : AddTransactionEvent
    data class SetFinancialSource(val addedFinancialSource: Pair<Int?, String?>) :
        AddTransactionEvent

    data class SetDate(val date: String) : AddTransactionEvent
    data class SetDescription(val description: String) : AddTransactionEvent
    data class ToggleTag(val tag: Tag) : AddTransactionEvent
    data class SetIsIncome(val isIncome: Boolean) : AddTransactionEvent

    object LoadInitialData : AddTransactionEvent
    object Submit : AddTransactionEvent
    data object OnDismiss : AddTransactionEvent
    data object OnFinancialSourceClicked : AddTransactionEvent
}


data class AddTransactionState(
    val amount: String = "",
    val description: String = "",
    val selectedDate: String = "",
    val isIncome: Boolean = false,

    val categories: List<Category> = emptyList(),
    val financialSources: List<FinancialSource> = emptyList(),
    val tags: List<Tag> = emptyList(),

    val selectedCategory: Category? = null,
    val selectedFinancialSource: Pair<Int?, String?>? = null,
    val selectedTags: Set<Tag> = emptySet(),

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddTransactionEffect {
    object Success : AddTransactionEffect
    data class Error(val message: String) : AddTransactionEffect
    data object OnDismiss : AddTransactionEffect
    data object OnFinancialSourceClicked : AddTransactionEffect
}

