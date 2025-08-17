package com.kazemieh.financialsource.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.domain.usecase.AddCategory
import com.kazemieh.domain.usecase.AddFinancialSource
import kotlinx.coroutines.launch

class AddFinancialSourceViewModel(
    private val addFinancialSourceUseCase: AddFinancialSource
) : ViewModel() {

    fun addFinancialSource(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                addFinancialSourceUseCase(name)
            }
        }
    }
}