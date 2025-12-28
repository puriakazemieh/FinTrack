package com.kazemieh.transaction.ui.delete


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.domain.usecase.TransactionUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeleteTransactionViewModel(
    private val transactionUseCases: TransactionUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteTransactionState())
    val state: StateFlow<DeleteTransactionState> = _state.asStateFlow()

    private val _effect = Channel<DeleteTransactionEffect>()
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DeleteTransactionIntent) {
        when (intent) {

            is DeleteTransactionIntent.SetData -> _state.update { it.copy(transactionWithRelations = intent.transactionWithRelations) }

            DeleteTransactionIntent.Submit -> deleteTransaction()

            DeleteTransactionIntent.OnDismiss -> {
                viewModelScope.launch {
                    _state.update { DeleteTransactionState() }
                    _effect.send(DeleteTransactionEffect.OnDismiss)
                }
            }

        }
    }

    private fun deleteTransaction() {
        viewModelScope.launch {
            _state.value.transactionWithRelations?.transaction?.let {
                transactionUseCases.deleteTransaction(it)
                _effect.send(DeleteTransactionEffect.DeletedTransaction)
            }
        }
    }

}

sealed interface DeleteTransactionIntent {
    data object Submit : DeleteTransactionIntent
    data object OnDismiss : DeleteTransactionIntent
    data class SetData(val transactionWithRelations: TransactionWithRelations? = null) :
        DeleteTransactionIntent

}


data class DeleteTransactionState(
    val transactionWithRelations: TransactionWithRelations? = null
)

sealed interface DeleteTransactionEffect {
    object DeletedTransaction : DeleteTransactionEffect
    data class ShowMessage(val message: Int) : DeleteTransactionEffect
    data object OnDismiss : DeleteTransactionEffect
}
