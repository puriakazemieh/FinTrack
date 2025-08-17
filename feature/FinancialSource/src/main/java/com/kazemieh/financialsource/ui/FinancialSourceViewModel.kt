package com.kazemieh.financialsource.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.GetAllFinancialSource
import com.kazemieh.model.FinancialSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryState(
    val financialSource: List<FinancialSource> = emptyList(),
    val isLoading: Boolean = false
)

class FinancialSourceViewModel(
    private val getAllFinancialSource: GetAllFinancialSource
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state = _state.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllFinancialSource().collect { financialSource ->
                _state.update { it.copy(financialSource = financialSource, isLoading = false) }
            }
        }
    }
}
