package com.kazemieh.financialsource.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.AddFinancialSource
import com.kazemieh.model.FinancialSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddFinancialSourceViewModel(
    private val addFinancialSourceUseCase: AddFinancialSource
) : ViewModel() {

    private val _state = MutableStateFlow(AddFinancialSourceState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddFinancialSourceEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddFinancialSourceIntent) {
        when (intent) {
            AddFinancialSourceIntent.AddFinancialSource -> addFinancialSource()
            is AddFinancialSourceIntent.SelectedType -> _state.update {
                it.copy(selectedTypeFinancialSource = intent.selectedTypeFinancialSource)
            }

            is AddFinancialSourceIntent.SetBalance -> _state.update { it.copy(balance = intent.balance) }
            is AddFinancialSourceIntent.SetCardNumber -> _state.update { it.copy(cardNumber = intent.cardNumber) }
            is AddFinancialSourceIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddFinancialSourceIntent.SetSourceName -> _state.update { it.copy(sourceName = intent.sourceName) }
            AddFinancialSourceIntent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddFinancialSourceState() }
                    _effect.send(AddFinancialSourceEffect.OnDismiss)
                }
            }
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
                val sourceId = addFinancialSourceUseCase(financialSource)
                if (sourceId >= 0) {
                    _effect.send(
                        AddFinancialSourceEffect.AddedFinancialSource(
                            sourceId.toInt(),
                            _state.value.sourceName ?: "انتخاب کنید"
                        )
                    )
                    _state.update { AddFinancialSourceState() }
                }

            } else {
                _effect.send(AddFinancialSourceEffect.ShowMessage(R.string.check_name_financial_source))
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
)

sealed interface AddFinancialSourceIntent {
    data class SetBalance(val balance: Int = 0) : AddFinancialSourceIntent
    data class SelectedType(val selectedTypeFinancialSource: SelectedTypeFinancialSource = SelectedTypeFinancialSource.CREDIT) :
        AddFinancialSourceIntent

    data object AddFinancialSource : AddFinancialSourceIntent
    data class SetSourceName(val sourceName: String? = null) : AddFinancialSourceIntent
    data class SetCardNumber(val cardNumber: String? = null) : AddFinancialSourceIntent
    data class SetDescription(val description: String? = null) : AddFinancialSourceIntent
    data object OnDismiss : AddFinancialSourceIntent
}

enum class SelectedTypeFinancialSource(val count: Int, val value: Int) {
    CREDIT(1, R.string.credit_),
    CASH(2, R.string.cash_)
}


sealed interface AddFinancialSourceEffect {
    data class ShowMessage(val message: Int) : AddFinancialSourceEffect
    data class AddedFinancialSource(val id: Int, val name: String) : AddFinancialSourceEffect
    data object OnDismiss : AddFinancialSourceEffect
}
