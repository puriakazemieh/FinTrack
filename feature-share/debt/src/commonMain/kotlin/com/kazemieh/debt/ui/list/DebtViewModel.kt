package com.kazemieh.debt.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DebtViewModel(
    private val debtUseCases: DebtUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(DebtState())
    val state: StateFlow<DebtState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    fun onIntent(intent: DebtIntent) {
        when (intent) {
            is DebtIntent.ObserveAllDebts -> observeAllDebts()
            is DebtIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is DebtIntent.ObserveDebtsByPerson -> observeDebtsByPerson(intent.personId)
            is DebtIntent.SettleDebt -> settleDebt(intent.debtId, intent.description)
            is DebtIntent.DeleteDebt -> deleteDebt(intent.debtId)
        }
    }

    private fun observeAllDebts() {
        viewModelScope.launch {
            debtUseCases.observeDebtsUseCase()
                .combine(_searchQuery) { debts, query ->
                    val filtered = debts.filter {
                        it.person.name.contains(query, ignoreCase = true) ||
                                it.debt.description?.contains(query, ignoreCase = true) == true
                    }
                    debts to filtered
                }
                .collect { (all, filtered) ->
                    _state.update {
                        it.copy(
                            debts = all,
                            filteredDebts = filtered,
                            searchQuery = _searchQuery.value
                        )
                    }
                }
        }
    }

    private fun observeDebtsByPerson(personId: Long) {
        viewModelScope.launch {
            debtUseCases.observeDebtsByPersonUseCase(personId).collect { debts ->
                _state.update { it.copy(debts = debts) }
            }
        }
    }

    private fun settleDebt(debtId: Long, description: String) {
        viewModelScope.launch {
            debtUseCases.settleDebtUseCase(debtId, description)
        }
    }

    private fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            debtUseCases.deleteDebtUseCase(debtId)
        }
    }
}

data class DebtState(
    val debts: List<DebtWithRelations> = emptyList(),
    val filteredDebts: List<DebtWithRelations> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

sealed interface DebtIntent {
    data object ObserveAllDebts : DebtIntent
    data class UpdateSearchQuery(val query: String) : DebtIntent
    data class ObserveDebtsByPerson(val personId: Long) : DebtIntent
    data class SettleDebt(val debtId: Long, val description: String) : DebtIntent
    data class DeleteDebt(val debtId: Long) : DebtIntent
}
