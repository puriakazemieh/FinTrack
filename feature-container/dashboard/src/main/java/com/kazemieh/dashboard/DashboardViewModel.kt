package com.kazemieh.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


class DashboardViewModel() : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.ShowAddTransaction -> _state.update {
                it.copy(showAddTransaction = !_state.value.showAddTransaction)
            }

            is DashboardIntent.ShowAddSource -> _state.update {
                it.copy(showAddSource = !_state.value.showAddSource)
            }

            is DashboardIntent.AnimationEnabled -> _state.update {
                it.copy(
                    enableAnimationChart = !_state.value.enableAnimationChart,
                    showAddTransaction = false
                )
            }

        }
    }


}

data class DashboardState(
    val showAddTransaction: Boolean = false,
    val showAddSource: Boolean = false,
    val enableAnimationChart: Boolean = false,
)


sealed interface DashboardIntent {
    data object ShowAddTransaction : DashboardIntent
    data object AnimationEnabled : DashboardIntent
    data object ShowAddSource : DashboardIntent
}
