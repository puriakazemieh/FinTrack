package com.kazemieh.financialsource.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.R
import com.kazemieh.domain.usecase.AddFinancialSource
import com.kazemieh.model.FinancialSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddFinancialSourceViewModel(
    private val addFinancialSourceUseCase: AddFinancialSource
) : ViewModel() {

    private val _state = MutableStateFlow(AddFinancialSourceState())
    val state = _state.asStateFlow()

    fun onIntent(intent: AddFinancialSourceIntent) {
        when (intent) {
            AddFinancialSourceIntent.AddFinancialSource -> addFinancialSource()
            is AddFinancialSourceIntent.SelectedType -> _state.update {
                it.copy(
                    selectedTypeFinancialSource = intent.selectedTypeFinancialSource
                )
            }

            is AddFinancialSourceIntent.SetBalance -> _state.update { it.copy(balance = intent.balance) }
            is AddFinancialSourceIntent.SetCardNumber -> _state.update { it.copy(cardNumber = intent.cardNumber) }
            is AddFinancialSourceIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddFinancialSourceIntent.SetSourceName -> _state.update { it.copy(sourceName = intent.sourceName) }
        }
    }

    fun addFinancialSource() = with(_state.value) {
        viewModelScope.launch {
            if (sourceName?.isNotBlank() == true) {
                val financialSource = FinancialSource(
                    name = sourceName,
                    balance = balance,
                    cardNumber = cardNumber,
                    description = description,
                    type = selectedTypeFinancialSource.count
                )
                val addedFinancialSourceId = addFinancialSourceUseCase(financialSource)
                if (addedFinancialSourceId != null) {
                    if (addedFinancialSourceId >= 0)
                        _state.update {
                            it.copy(addedFinancialSource = true)
                        }
                }
            }
        }
    }
}


data class AddFinancialSourceState(
    val balance: Int = 0,
    val selectedTypeFinancialSource: SelectedTypeFinancialSource = SelectedTypeFinancialSource.CREDIT,
    val sourceName: String? = null,
    val cardNumber: String? = null,
    val description: String? = null,
    val addedFinancialSource: Boolean = false,
    )

sealed interface AddFinancialSourceIntent {
    data class SetBalance(val balance: Int = 0) : AddFinancialSourceIntent
    data class SelectedType(val selectedTypeFinancialSource: SelectedTypeFinancialSource = SelectedTypeFinancialSource.CREDIT) :
        AddFinancialSourceIntent

    data object AddFinancialSource : AddFinancialSourceIntent
    data class SetSourceName(val sourceName: String? = null) : AddFinancialSourceIntent
    data class SetCardNumber(val cardNumber: String? = null) : AddFinancialSourceIntent
    data class SetDescription(val description: String? = null) : AddFinancialSourceIntent
}

enum class SelectedTypeFinancialSource(val count: Int, val value: Int) {
    CREDIT(1, R.string.credit_),
    CASH(2, R.string.cash_)
}