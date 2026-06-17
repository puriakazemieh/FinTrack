package com.kazemieh.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.PreferenceUseCases
import com.kazemieh.preferences.FinTrackPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update


class DashboardViewModel(
    private val preferenceUseCases: PreferenceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        observeUserName()
    }

    private fun observeUserName() {
        combine(
            preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_USER_NAME, ""),
            preferenceUseCases.getStringFlow(FinTrackPreferences.PREF_USER_FAMILY, "")
        ) { name, family ->
            val fullName = listOf(name, family).filter { it.isNotBlank() }.joinToString(" ")
            val displayName = if (fullName.isBlank()) "کاربر" else fullName
            val displayInitial = if (fullName.isBlank()) "پ" else fullName.first().toString()

            _state.update {
                it.copy(
                    userName = displayName,
                    userInitial = displayInitial
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.ShowTransactionBottomSheet -> _state.update {
                it.copy(
                    showAddTransaction = !_state.value.showAddTransaction,
                    transactionWithRelations = intent.transactionWithRelations,
                    initialTransactionType = intent.type
                )
            }

            is DashboardIntent.DeleteTransactionBottomSheet -> _state.update {
                it.copy(
                    showDeleteTransaction = !_state.value.showDeleteTransaction,
                    transactionWithRelations = intent.transactionWithRelations
                )
            }

             is DashboardIntent.ShowAddSource -> _state.update {
                it.copy(
                    showAddSource = !it.showAddSource,
                    selectedSource = intent.source
                )
            }

            is DashboardIntent.AnimationEnabled -> _state.update {
                it.copy(
                    enableAnimationChart = !_state.value.enableAnimationChart,
                    showAddTransaction = false,
                    transactionWithRelations = null
                )
            }

            DashboardIntent.ToggleBalanceVisibility -> _state.update {
                it.copy(isBalanceVisible = !it.isBalanceVisible)
            }
        }
    }
}

data class DashboardState(
    val showAddTransaction: Boolean = false,
    val showDeleteTransaction: Boolean = false,
    val showAddSource: Boolean = false,
    val selectedSource: Source? = null,
    val enableAnimationChart: Boolean = false,
    val transactionWithRelations: TransactionWithRelations? = null,
    val initialTransactionType: TransactionType? = null,
    val isBalanceVisible: Boolean = true,
    val growthPercentage: String = "+2.5%", // Placeholder
    val userName: String = "کاربر",
    val userInitial: String = "پ"
)


sealed interface DashboardIntent {
    data class ShowTransactionBottomSheet(
        val transactionWithRelations: TransactionWithRelations? = null,
        val type: TransactionType? = null
    ) : DashboardIntent

    data class DeleteTransactionBottomSheet(val transactionWithRelations: TransactionWithRelations? = null) :
        DashboardIntent

    data object AnimationEnabled : DashboardIntent
    data class ShowAddSource(val source: Source? = null) : DashboardIntent
    data object ToggleBalanceVisibility : DashboardIntent
}
