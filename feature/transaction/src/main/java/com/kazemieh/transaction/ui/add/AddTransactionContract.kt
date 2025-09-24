package com.kazemieh.transaction.ui.add

import com.kazemieh.model.Tag


sealed interface AddTransactionEvent {
    data class SetAmount(val amount: String) : AddTransactionEvent
    data class SetCategory(val category: Pair<Int, String>? = null) : AddTransactionEvent
    data class SetSource(val setSource: Pair<Int, String>? = null) :
        AddTransactionEvent

    data class SetDate(val date: String) : AddTransactionEvent
    data class SetDescription(val description: String) : AddTransactionEvent
    data class ToggleTag(val tag: Tag) : AddTransactionEvent
    data class SetIsIncome(val isIncome: Boolean) : AddTransactionEvent

    object Submit : AddTransactionEvent
    data object OnDismiss : AddTransactionEvent


}


data class AddTransactionState(
    val amount: String = "",
    val description: String = "",
    val selectedDate: String = "",
    val isIncome: Boolean = false,

    val tags: List<Tag> = emptyList(),

    val category: Pair<Int, String>? = null,
    val source: Pair<Int, String>? = null,
    val selectedTags: Set<Tag> = emptySet(),

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddTransactionEffect {
    object Success : AddTransactionEffect
    data class Error(val message: String) : AddTransactionEffect
    data object OnDismiss : AddTransactionEffect
}

