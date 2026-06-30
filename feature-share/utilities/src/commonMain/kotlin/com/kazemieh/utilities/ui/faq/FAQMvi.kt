package com.kazemieh.utilities.ui.faq

import com.kazemieh.common.model.FAQItem

data class FAQState(
    val faqs: List<FAQItem> = emptyList(),
    val expandedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

sealed interface FAQIntent {
    data object LoadFAQs : FAQIntent
    data class ToggleExpand(val id: String) : FAQIntent
}
