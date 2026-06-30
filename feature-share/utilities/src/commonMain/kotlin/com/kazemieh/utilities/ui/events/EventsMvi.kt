package com.kazemieh.utilities.ui.events

import com.kazemieh.common.model.FinancialEvent

data class EventsState(
    val events: List<FinancialEvent> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface EventsIntent {
    data object LoadEvents : EventsIntent
}
