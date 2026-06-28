package com.kazemieh.fixed_expense.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.FixedExpense
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.Source
import com.kazemieh.domain.usecase.FixedExpenseUseCaseGroup
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock

class AddFixedExpenseViewModel(
    private val fixedExpenseUseCases: FixedExpenseUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(AddFixedExpenseState())
    val state: StateFlow<AddFixedExpenseState> = _state.asStateFlow()

    private val _effect = Channel<AddFixedExpenseEffect>()
    val effect: Flow<AddFixedExpenseEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: AddFixedExpenseIntent) {
        when (intent) {
            is AddFixedExpenseIntent.SetAmount -> _state.update { it.copy(amount = intent.amount) }
            is AddFixedExpenseIntent.SetCategory -> _state.update { it.copy(category = intent.category) }
            is AddFixedExpenseIntent.SetSource -> _state.update { it.copy(source = intent.source) }
            is AddFixedExpenseIntent.SetRecurrence -> _state.update { it.copy(recurrence = intent.recurrence) }
            is AddFixedExpenseIntent.SetStartDate -> _state.update { it.copy(startDate = intent.startDate) }
            is AddFixedExpenseIntent.SetAutoPost -> _state.update { it.copy(isAutoPostEnabled = intent.enabled) }
            is AddFixedExpenseIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            AddFixedExpenseIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val currentState = _state.value
        val amountValue = currentState.amount.toLongOrNull()
        if (amountValue == null || currentState.category == null || currentState.source == null) return

        viewModelScope.launch {
            val expense = FixedExpense(
                amount = amountValue,
                categoryId = currentState.category.id ?: 0L,
                sourceId = currentState.source.id ?: 0L,
                recurrence = currentState.recurrence,
                startDate = currentState.startDate,
                nextDueDate = currentState.startDate, // Initial next due date is start date
                isAutoPostEnabled = currentState.isAutoPostEnabled,
                description = currentState.description
            )
            val reminderTitle = getString(Res.string.notif_installment_label)
            val reminderMessage = getString(Res.string.msg_notif_fixed_expense_due, currentState.category.name)
            fixedExpenseUseCases.addFixedExpenseUseCase(expense, reminderTitle, reminderMessage)
            _effect.send(AddFixedExpenseEffect.Saved)
        }
    }
}

data class AddFixedExpenseState(
    val amount: String = "",
    val category: Category? = null,
    val source: Source? = null,
    val recurrence: RecurrenceType = RecurrenceType.MONTHLY,
    val startDate: Long = Clock.System.now().toEpochMilliseconds(),
    val isAutoPostEnabled: Boolean = false,
    val description: String = ""
)

sealed interface AddFixedExpenseIntent {
    data class SetAmount(val amount: String) : AddFixedExpenseIntent
    data class SetCategory(val category: Category?) : AddFixedExpenseIntent
    data class SetSource(val source: Source?) : AddFixedExpenseIntent
    data class SetRecurrence(val recurrence: RecurrenceType) : AddFixedExpenseIntent
    data class SetStartDate(val startDate: Long) : AddFixedExpenseIntent
    data class SetAutoPost(val enabled: Boolean) : AddFixedExpenseIntent
    data class SetDescription(val description: String) : AddFixedExpenseIntent
    data object Submit : AddFixedExpenseIntent
}

sealed interface AddFixedExpenseEffect {
    data object Saved : AddFixedExpenseEffect
}
