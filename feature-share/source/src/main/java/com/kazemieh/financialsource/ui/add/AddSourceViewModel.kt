package com.kazemieh.financialsource.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.R
import com.kazemieh.domain.usecase.AddSourceUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddFinancialSourceViewModel(
    private val sourceUseCases: AddSourceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AddSourceState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddFinancialSourceEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: AddSourceIntent) {
        when (intent) {
            AddSourceIntent.AddSource -> addSource()
            is AddSourceIntent.SelectedType -> _state.update { it.copy(typeSource = intent.typeSource) }
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

            is AddSourceIntent.ShowEditData -> getDefaultData(intent.selectedSource)
        }
    }


    private fun getDefaultData(selectedSource: Source?) {
        if (selectedSource == null) return
        viewModelScope.launch {
            sourceUseCases.getSource(selectedSource.id ?: 0).collect {
                it?.let {source ->
                    _state.update {
                        it.copy(
                            balance = source.balance,
                            typeSource = if (source.type == 1) TypeSource.CREDIT else TypeSource.CASH,
                            sourceName = source.name,
                            cardNumber = source.cardNumber,
                            description = source.description,
                            source = source
                        )
                    }
                }
            }
        }
    }

    private fun addSource() = with(_state.value) {
        viewModelScope.launch {
            if (sourceName?.isNotBlank() == true) {
                val source = Source(
                    id = source?.id,
                    name = sourceName,
                    balance = balance,
                    cardNumber = cardNumber,
                    description = description,
                    type = typeSource.count
                )
                val sourceId = if (_state.value.source != null) {
                    sourceUseCases.editSource(source).toLong()
                } else {
                    sourceUseCases.addSource(source)
                }
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
    val typeSource: TypeSource = TypeSource.CREDIT,
    val sourceName: String? = null,
    val cardNumber: String? = null,
    val description: String? = null,
    val source: Source? = null,
)

sealed interface AddSourceIntent {
    data class SetBalance(val balance: Int = 0) : AddSourceIntent
    data class ShowEditData(val selectedSource: Source? = null) : AddSourceIntent
    data class SelectedType(val typeSource: TypeSource = TypeSource.CREDIT) : AddSourceIntent

    data object AddSource : AddSourceIntent
    data class SetSourceName(val sourceName: String? = null) : AddSourceIntent
    data class SetCardNumber(val cardNumber: String? = null) : AddSourceIntent
    data class SetDescription(val description: String? = null) : AddSourceIntent
    data object OnDismiss : AddSourceIntent
}

enum class TypeSource(val count: Int, val value: Int) {
    CREDIT(1, R.string.credit_),
    CASH(2, R.string.cash_)
}


sealed interface AddFinancialSourceEffect {
    data class ShowMessage(val message: Int) : AddFinancialSourceEffect
    data class AddedFinancialSource(val source: Source) : AddFinancialSourceEffect
    data object OnDismiss : AddFinancialSourceEffect
}
