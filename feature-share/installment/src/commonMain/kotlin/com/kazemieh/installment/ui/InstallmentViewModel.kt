package com.kazemieh.installment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.InstallmentWithRelations
import com.kazemieh.domain.usecase.InstallmentUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

import org.jetbrains.compose.resources.getString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.installment_deleted
import fintrack.core.designsystem.generated.resources.payment
import fintrack.core.designsystem.generated.resources.transaction_failed
import kotlin.time.Clock

sealed interface InstallmentIntent {
    data object Init : InstallmentIntent
    data class UpdateSearchQuery(val query: String) : InstallmentIntent
    data class MarkAsPaid(
        val installmentId: Long,
        val transactionDescription: String,
        val reminderTitle: String,
        val reminderMessage: String
    ) : InstallmentIntent
    data class Delete(val installmentId: Long) : InstallmentIntent
}

sealed interface InstallmentEffect {
    data class ShowMessage(val message: String) : InstallmentEffect
}

data class InstallmentState(
    val upcoming: List<InstallmentWithRelations> = emptyList(),
    val overdue: List<InstallmentWithRelations> = emptyList(),
    val completed: List<InstallmentWithRelations> = emptyList(),
    val filteredUpcoming: List<InstallmentWithRelations> = emptyList(),
    val filteredOverdue: List<InstallmentWithRelations> = emptyList(),
    val filteredCompleted: List<InstallmentWithRelations> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

class InstallmentViewModel(
    private val useCases: InstallmentUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(InstallmentState())
    val state: StateFlow<InstallmentState> = _state.asStateFlow()

    private val _effects = Channel<InstallmentEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<InstallmentEffect> = _effects.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    fun onIntent(intent: InstallmentIntent) {
        when (intent) {
            InstallmentIntent.Init -> observeInstallments()
            is InstallmentIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is InstallmentIntent.MarkAsPaid -> markAsPaid(intent)
            is InstallmentIntent.Delete -> deleteInstallment(intent.installmentId)
        }
    }

    private fun observeInstallments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            useCases.observeInstallmentsUseCase()
                .combine(_searchQuery) { installments, query ->
                    val now = Clock.System.now().toEpochMilliseconds()
                    val upcoming = mutableListOf<InstallmentWithRelations>()
                    val overdue = mutableListOf<InstallmentWithRelations>()
                    val completed = mutableListOf<InstallmentWithRelations>()

                    installments.forEach { item ->
                        if (item.installment.isCompleted) {
                            completed.add(item)
                        } else if (item.installment.nextDueDate < now) {
                            overdue.add(item)
                        } else {
                            upcoming.add(item)
                        }
                    }

                    fun List<InstallmentWithRelations>.filterByQuery(q: String) = filter {
                        it.installment.title.contains(q, ignoreCase = true) ||
                                it.installment.description?.contains(q, ignoreCase = true) == true
                    }

                    InstallmentData(
                        upcoming = upcoming,
                        overdue = overdue,
                        completed = completed,
                        filteredUpcoming = upcoming.filterByQuery(query),
                        filteredOverdue = overdue.filterByQuery(query),
                        filteredCompleted = completed.filterByQuery(query)
                    )
                }
                .collectLatest { data ->
                    _state.update {
                        it.copy(
                            upcoming = data.upcoming,
                            overdue = data.overdue,
                            completed = data.completed,
                            filteredUpcoming = data.filteredUpcoming,
                            filteredOverdue = data.filteredOverdue,
                            filteredCompleted = data.filteredCompleted,
                            isLoading = false,
                            searchQuery = _searchQuery.value
                        )
                    }
                }
        }
    }

    private fun markAsPaid(intent: InstallmentIntent.MarkAsPaid) {
        viewModelScope.launch {
            try {
                useCases.markInstallmentAsPaidUseCase(
                    installmentId = intent.installmentId,
                    transactionDescription = intent.transactionDescription,
                    reminderTitle = intent.reminderTitle,
                    reminderMessage = intent.reminderMessage
                )
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.payment)))
            } catch (e: Exception) {
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.transaction_failed)))
            }
        }
    }

    private fun deleteInstallment(id: Long) {
        viewModelScope.launch {
            try {
                useCases.deleteInstallmentUseCase(id)
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.installment_deleted)))
            } catch (e: Exception) {
                _effects.send(InstallmentEffect.ShowMessage(getString(Res.string.transaction_failed)))
            }
        }
    }
}

private data class InstallmentData(
    val upcoming: List<InstallmentWithRelations>,
    val overdue: List<InstallmentWithRelations>,
    val completed: List<InstallmentWithRelations>,
    val filteredUpcoming: List<InstallmentWithRelations>,
    val filteredOverdue: List<InstallmentWithRelations>,
    val filteredCompleted: List<InstallmentWithRelations>
)
