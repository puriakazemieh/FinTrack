package com.kazemieh.check.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Check
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.domain.usecase.CheckUseCaseGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CheckListViewModel(
    private val checkUseCases: CheckUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(CheckListState())
    val state: StateFlow<CheckListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        observeChecks()
    }

    fun onIntent(intent: CheckListIntent) {
        when (intent) {
            is CheckListIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
            is CheckListIntent.UpdateStatus -> updateStatus(intent.checkId, intent.newStatus)
            is CheckListIntent.DeleteCheck -> deleteCheck(intent.checkId)
        }
    }

    private fun observeChecks() {
        viewModelScope.launch {
            checkUseCases.observeAllChecksUseCase()
                .combine(_searchQuery) { checks, query ->
                    val filtered = checks.filter {
                        it.description?.contains(query, ignoreCase = true) == true ||
                                it.personName?.contains(query, ignoreCase = true) == true
                    }
                    checks to filtered
                }
                .collect { (all, filtered) ->
                    _state.update {
                        it.copy(
                            checks = all,
                            filteredChecks = filtered,
                            searchQuery = _searchQuery.value
                        )
                    }
                }
        }
    }

    private fun updateStatus(checkId: Long, newStatus: CheckStatus) {
        viewModelScope.launch {
            val check = _state.value.checks.find { it.id == checkId }
            check?.let {
                checkUseCases.updateCheckUseCase(it.copy(status = newStatus))
            }
        }
    }

    private fun deleteCheck(checkId: Long) {
        viewModelScope.launch {
            checkUseCases.deleteCheckUseCase(checkId)
        }
    }
}

data class CheckListState(
    val checks: List<Check> = emptyList(),
    val filteredChecks: List<Check> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

sealed interface CheckListIntent {
    data class UpdateSearchQuery(val query: String) : CheckListIntent
    data class UpdateStatus(val checkId: Long, val newStatus: CheckStatus) : CheckListIntent
    data class DeleteCheck(val checkId: Long) : CheckListIntent
}
