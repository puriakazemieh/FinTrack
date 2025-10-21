package com.kazemieh.fintrack.report


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class ReportViewModel() : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state


    fun onIntent(intent: ReportIntent) {
        when (intent) {
            is ReportIntent.SelectedType -> _state.update { it.copy(transactionType = intent.transactionType) }
        }
    }
}

data class ReportState(
    val transactionType: Int? = null,
)


sealed interface ReportIntent {
    data class SelectedType(val transactionType: Int?) : ReportIntent
}