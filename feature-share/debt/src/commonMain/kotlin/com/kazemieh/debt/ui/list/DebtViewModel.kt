package com.kazemieh.debt.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DebtViewModel(
    private val debtUseCases: DebtUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(DebtState())
    val state: StateFlow<DebtState> = _state.asStateFlow()

    fun onIntent(intent: DebtIntent) {
        when (intent) {
            is DebtIntent.ObserveAllDebts -> observeAllDebts()
            is DebtIntent.ObserveDebtsByPerson -> observeDebtsByPerson(intent.personId)
            is DebtIntent.SettleDebt -> settleDebt(intent.debtId, intent.description)
            is DebtIntent.DeleteDebt -> deleteDebt(intent.debtId)
        }
    }

    private fun observeAllDebts() {
        viewModelScope.launch {
            debtUseCases.observeDebtsUseCase().collect { debts ->
                _state.update { it.copy(debts = debts) }
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
    val isLoading: Boolean = false
)

sealed interface DebtIntent {
    data object ObserveAllDebts : DebtIntent
    data class ObserveDebtsByPerson(val personId: Long) : DebtIntent
    data class SettleDebt(val debtId: Long, val description: String) : DebtIntent
    data class DeleteDebt(val debtId: Long) : DebtIntent
}
