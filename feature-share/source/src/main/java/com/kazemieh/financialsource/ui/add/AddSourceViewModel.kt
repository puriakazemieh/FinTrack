package com.kazemieh.financialsource.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.AddFinancialSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddFinancialSourceViewModel(
    private val addFinancialSourceUseCase: AddFinancialSource
) : ViewModel() {

    private val _state = MutableStateFlow(AddSourceState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddFinancialSourceEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddSourceIntent) {
        when (intent) {
            AddSourceIntent.AddSource -> addSource()
            is AddSourceIntent.SelectedType -> _state.update {
                it.copy(selectedTypeFinancialSource = intent.selectedTypeFinancialSource)
            }

            is AddSourceIntent.SetBalance -> _state.update { it.copy(balance = intent.balance) }
            is AddSourceIntent.SetCardNumber -> _state.update { it.copy(cardNumber = intent.cardNumber) }
            is AddSourceIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddSourceIntent.SetSourceName -> _state.update { it.copy(sourceName = intent.sourceName) }
            AddSourceIntent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { AddSourceState() }
                    _effect.send(AddFinancialSourceEffect.OnDismiss)
                }
            }
        }
    }

    private fun addSource() = with(_state.value) {
        viewModelScope.launch {
            if (sourceName?.isNotBlank() == true) {
                val source = Source(
                    name = sourceName,
                    balance = balance,
                    cardNumber = cardNumber,
                    description = description,
                    type = selectedTypeFinancialSource.count
                )
                val sourceId = addFinancialSourceUseCase(source)
                if (sourceId >= 0) {
                    _effect.send(
                        AddFinancialSourceEffect.AddedFinancialSource(source.copy(id = sourceId))
                    )
                    _state.update { AddSourceState() }
                }

            } else {
                _effect.send(AddFinancialSourceEffect.ShowMessage(R.string.check_name_financial_source))
            }
        }
    }
}


data class AddSourceState(
    val balance: Int = 0,
    val selectedTypeFinancialSource: SelectedTypeFinancialSource = SelectedTypeFinancialSource.CREDIT,
    val sourceName: String? = null,
    val cardNumber: String? = null,
    val description: String? = null,
)

sealed interface AddSourceIntent {
    data class SetBalance(val balance: Int = 0) : AddSourceIntent
    data class SelectedType(val selectedTypeFinancialSource: SelectedTypeFinancialSource = SelectedTypeFinancialSource.CREDIT) :
        AddSourceIntent

    data object AddSource : AddSourceIntent
    data class SetSourceName(val sourceName: String? = null) : AddSourceIntent
    data class SetCardNumber(val cardNumber: String? = null) : AddSourceIntent
    data class SetDescription(val description: String? = null) : AddSourceIntent
    data object OnDismiss : AddSourceIntent
}

enum class SelectedTypeFinancialSource(val count: Int, val value: Int) {
    CREDIT(1, R.string.credit_),
    CASH(2, R.string.cash_)
}


sealed interface AddFinancialSourceEffect {
    data class ShowMessage(val message: Int) : AddFinancialSourceEffect
    data class AddedFinancialSource(val source: Source) : AddFinancialSourceEffect
    data object OnDismiss : AddFinancialSourceEffect
}
