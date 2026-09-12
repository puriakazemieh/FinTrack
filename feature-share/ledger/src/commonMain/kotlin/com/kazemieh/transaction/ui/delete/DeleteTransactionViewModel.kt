package com.kazemieh.transaction.ui.delete


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.component.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.*
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class DeleteTransactionViewModel(
    private val analytics: com.kazemieh.common.analytics.AnalyticsService,
    private val transactionUseCaseGroup: TransactionUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteTransactionState())
    val state: StateFlow<DeleteTransactionState> = _state.asStateFlow()

    private val _effect = Channel<DeleteTransactionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DeleteTransactionIntent) {
        when (intent) {
            is DeleteTransactionIntent.SetData -> _state.update {
                it.copy(transactionWithRelations = intent.transactionWithRelations, isLoading = false)
            }

            DeleteTransactionIntent.Submit -> deleteTransaction()

            DeleteTransactionIntent.OnDismiss -> viewModelScope.launch {
                _state.update { DeleteTransactionState() }
                _effect.send(DeleteTransactionEffect.OnDismiss)
            }
        }
    }

    private fun deleteTransaction() {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            val tx = _state.value.transactionWithRelations?.transaction
            if (tx == null) {
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.transaction_failed))
                _effect.send(DeleteTransactionEffect.OnDismiss)
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            val ok = runCatching {
                analytics.track(com.kazemieh.common.analytics.ProductEvent.TransactionDeleted)
                transactionUseCaseGroup.deleteTransactionUseCase(tx)
            }.isSuccess

            if (ok) {
                _effect.send(DeleteTransactionEffect.DeletedTransaction)
                _state.update { DeleteTransactionState() }
            } else {
                _state.update { it.copy(isLoading = false) }
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.transaction_delete_failed))
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
    val transactionWithRelations: TransactionWithRelations? = null,
    val isLoading: Boolean = false
)

sealed interface DeleteTransactionEffect {
    object DeletedTransaction : DeleteTransactionEffect
    data object OnDismiss : DeleteTransactionEffect
}
