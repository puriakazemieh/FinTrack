package com.kazemieh.installment.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.InstallmentFrequency
import com.kazemieh.common.model.Source
import com.kazemieh.domain.usecase.InstallmentUseCaseGroup
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.fill_all_field
import fintrack.core.designsystem.generated.resources.transaction_failed
import kotlin.time.Clock

sealed interface AddInstallmentIntent {
    data class SetTitle(val title: String) : AddInstallmentIntent
    data class SetTotalAmount(val amount: String) : AddInstallmentIntent
    data class SetInstallmentAmount(val amount: String) : AddInstallmentIntent
    data class SetTotalInstallments(val total: String) : AddInstallmentIntent
    data class SetPaidInstallments(val paid: String) : AddInstallmentIntent
    data class SetCategory(val category: Category) : AddInstallmentIntent
    data class SetSource(val source: Source) : AddInstallmentIntent
    data class SetStartDate(val timeStamp: Long) : AddInstallmentIntent
    data class SetFrequency(val frequency: InstallmentFrequency) : AddInstallmentIntent
    data class Submit(
        val reminderTitle: String,
        val reminderMessage: String
    ) : AddInstallmentIntent
}

sealed interface AddInstallmentEffect {
    data object Success : AddInstallmentEffect
    data class Error(val message: String) : AddInstallmentEffect
}

data class AddInstallmentState(
    val title: String = "",
    val totalAmount: String = "",
    val installmentAmount: String = "",
    val totalInstallments: String = "",
    val paidInstallments: String = "0",
    val category: Category? = null,
    val source: Source? = null,
    val startDate: Long = Clock.System.now().toEpochMilliseconds(),
    val frequency: InstallmentFrequency = InstallmentFrequency.MONTHLY,
    val isLoading: Boolean = false
)

class AddInstallmentViewModel(
    private val installmentUseCases: InstallmentUseCaseGroup,
    private val transactionUseCaseGroup: TransactionUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(AddInstallmentState())
    val state: StateFlow<AddInstallmentState> = _state.asStateFlow()

    private val _effects = Channel<AddInstallmentEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<AddInstallmentEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: AddInstallmentIntent) {
        when (intent) {
            is AddInstallmentIntent.SetTitle -> _state.update { it.copy(title = intent.title) }
            is AddInstallmentIntent.SetTotalAmount -> _state.update { it.copy(totalAmount = intent.amount) }
            is AddInstallmentIntent.SetInstallmentAmount -> _state.update { it.copy(installmentAmount = intent.amount) }
            is AddInstallmentIntent.SetTotalInstallments -> _state.update { it.copy(totalInstallments = intent.total) }
            is AddInstallmentIntent.SetPaidInstallments -> _state.update { it.copy(paidInstallments = intent.paid) }
            is AddInstallmentIntent.SetCategory -> _state.update { it.copy(category = intent.category) }
            is AddInstallmentIntent.SetSource -> _state.update { it.copy(source = intent.source) }
            is AddInstallmentIntent.SetStartDate -> _state.update { it.copy(startDate = intent.timeStamp) }
            is AddInstallmentIntent.SetFrequency -> _state.update { it.copy(frequency = intent.frequency) }
            is AddInstallmentIntent.Submit -> submit(intent)
        }
    }

    private fun submit(intent: AddInstallmentIntent.Submit) {
        val s = _state.value
        if (s.title.isBlank() || s.installmentAmount.isBlank() || s.category == null || s.source == null) {
            viewModelScope.launch { _effects.send(AddInstallmentEffect.Error(getString(Res.string.fill_all_field))) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val installment = Installment(
                    id = 0,
                    title = s.title,
                    totalAmount = s.totalAmount.toLongOrNull() ?: 0,
                    installmentAmount = s.installmentAmount.toLongOrNull() ?: 0,
                    totalInstallments = s.totalInstallments.toIntOrNull() ?: 1,
                    paidInstallments = s.paidInstallments.toIntOrNull() ?: 0,
                    categoryId = s.category.id!!,
                    sourceId = s.source.id!!,
                    startDate = s.startDate,
                    nextDueDate = s.startDate, // Initial next due date is start date
                    frequency = s.frequency
                )
                installmentUseCases.addInstallmentUseCase(
                    installment = installment,
                    reminderTitle = intent.reminderTitle,
                    reminderMessage = intent.reminderMessage
                )
                _effects.send(AddInstallmentEffect.Success)
            } catch (e: Exception) {
                _effects.send(AddInstallmentEffect.Error(getString(Res.string.transaction_failed)))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
