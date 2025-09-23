package com.kazemieh.fintrack

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class DashboardViewModel() : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state


    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.SetSource -> {
                _state.update {
                    it.copy(
                        setSource = intent.setSource,
                        sourceList = false,
                        showAddSource = false
                    )
                }
            }

            is DashboardIntent.SourceList -> {
                _state.update { it.copy(sourceList = intent.sourceList) }
            }

            is DashboardIntent.ShowAddSource -> {
                _state.update {
                    it.copy(
                        showAddSource = intent.showAddSource,
                        fromSourceList = intent.fromSourceList,
                        setSource = if (!intent.showAddSource && intent.fromSourceList) null else it.setSource
                    )
                }
            }

            is DashboardIntent.ShowAddTransaction -> {
                _state.update {
                    it.copy(
                        showAddTransaction = intent.showAddTransaction,
                        setSource = if (!intent.showAddTransaction) null else it.setSource
                    )
                }
            }

        }
    }


}

data class DashboardState(
    val setSource: Pair<Int, String>? = null,
    val sourceList: Boolean = false,
    val showAddSource: Boolean = false,
    val showAddTransaction: Boolean = false,
    val fromSourceList: Boolean = false,
)


sealed interface DashboardIntent {
    data class ShowAddTransaction(val showAddTransaction: Boolean = false) : DashboardIntent
    data class SourceList(val sourceList: Boolean = false) : DashboardIntent
    data class ShowAddSource(
        val showAddSource: Boolean = false,
        val fromSourceList: Boolean = false
    ) : DashboardIntent

    data class SetSource(val setSource: Pair<Int, String>? = null) : DashboardIntent
}
