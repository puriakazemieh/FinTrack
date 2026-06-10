package com.kazemieh.person.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.DebtWithRelations
import com.kazemieh.common.model.Person
import com.kazemieh.domain.usecase.DebtUseCaseGroup
import com.kazemieh.domain.usecase.ObservePersonsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PersonDetailViewModel(
    private val personId: Long,
    private val observePersonsUseCase: ObservePersonsUseCase,
    private val debtUseCases: DebtUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(PersonDetailState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            observePersonsUseCase().collect { persons ->
                val person = persons.find { it.id == personId }
                _state.update { it.copy(person = person) }
            }
        }

        viewModelScope.launch {
            debtUseCases.observeDebtsByPersonUseCase(personId).collect { debts ->
                val totalCredits = debts.filter { it.debt.type == DebtType.OWED_TO_ME && !it.debt.isSettled }
                    .sumOf { it.debt.amount }
                val totalDebts = debts.filter { it.debt.type == DebtType.OWED_BY_ME && !it.debt.isSettled }
                    .sumOf { it.debt.amount }

                _state.update {
                    it.copy(
                        debts = debts,
                        totalCredits = totalCredits,
                        totalDebts = totalDebts,
                        balance = totalCredits - totalDebts
                    )
                }
            }
        }
    }

    fun onIntent(intent: PersonDetailIntent) {
        when (intent) {
            is PersonDetailIntent.SettleDebt -> settleDebt(intent.debtId, intent.description)
            is PersonDetailIntent.DeleteDebt -> deleteDebt(intent.debtId)
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

data class PersonDetailState(
    val person: Person? = null,
    val debts: List<DebtWithRelations> = emptyList(),
    val totalCredits: Long = 0,
    val totalDebts: Long = 0,
    val balance: Long = 0,
    val isLoading: Boolean = false
)

sealed interface PersonDetailIntent {
    data class SettleDebt(val debtId: Long, val description: String) : PersonDetailIntent
    data class DeleteDebt(val debtId: Long) : PersonDetailIntent
}
