package com.kazemieh.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.TransactionUseCases
import com.kazemieh.model.FinancialSource
import com.kazemieh.model.TransactionWithRelations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class DashboardState(
    val totalBalance: Int = 0,
    val totalIncome: Int = 0,
    val totalExpense: Int = 0,

    val financialSources: List<FinancialSource> = emptyList(),

    val recentTransactions: List<TransactionWithRelations> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface DashboardEvent {
    object LoadData : DashboardEvent
    object AddSource : DashboardEvent
    object AddTransaction : DashboardEvent
}

class DashboardViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.LoadData -> {
                loadData()
            }

            is DashboardEvent.AddSource -> {
            }

            is DashboardEvent.AddTransaction -> {
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionUseCases.getAllTransactions(),
                transactionUseCases.getAllFinancialSource()
            ) { transactions, sources ->
                val totalIncome = transactions
                    .filter { it.transaction.amount > 0 }
                    .sumOf { it.transaction.amount }

                val totalExpense = transactions
                    .filter { it.transaction.amount < 0 }
                    .sumOf { it.transaction.amount }

                val totalBalance = totalIncome + totalExpense

                DashboardState(
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    totalBalance = totalBalance,
                    recentTransactions = transactions,
                    financialSources = sources,
                    isLoading = false
                )
            }
                .onStart { emit(DashboardState(isLoading = true)) }
                .collect { state -> _state.value = state }
        }
    }
}
