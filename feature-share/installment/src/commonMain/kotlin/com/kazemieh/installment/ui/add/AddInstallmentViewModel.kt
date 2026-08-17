package com.kazemieh.installment.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Installment
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.InstallmentFrequency
import com.kazemieh.common.persiandatetime.extensions.toEpochMilliseconds
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.domain.usecase.InstallmentUseCaseGroup
import com.kazemieh.domain.usecase.TransactionUseCaseGroup
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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
    data class SetTags(val tags: Set<Tag>) : AddInstallmentIntent
    data class SetPersons(val persons: Set<Person>) : AddInstallmentIntent
    data class SetStartDate(val timeStamp: Long) : AddInstallmentIntent
    data class SetReminderEnabled(val enabled: Boolean) : AddInstallmentIntent
    data class SetPostAsTransaction(val enabled: Boolean) : AddInstallmentIntent
    data class SetReminderTime(val hour: Int, val minute: Int) : AddInstallmentIntent
    data class SetFrequency(val frequency: InstallmentFrequency) : AddInstallmentIntent
    data class LoadInstallment(val installmentId: Long) : AddInstallmentIntent
    data class SetLoanAmount(val amount: String) : AddInstallmentIntent
    data class SetLoanInstallmentAmount(val amount: String) : AddInstallmentIntent
    data class SetLoanCount(val count: String) : AddInstallmentIntent
    data class SetDescription(val description: String) : AddInstallmentIntent
    data object ApplyLoanCalculator : AddInstallmentIntent
    data object Reset : AddInstallmentIntent
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
    val installmentId: Long? = null,
    val title: String = "",
    val totalAmount: String = "",
    val installmentAmount: String = "",
    val totalInstallments: String = "",
    val paidInstallments: String = "0",
    val category: Category? = null,
    val source: Source? = null,
    val tags: Set<Tag> = emptySet(),
    val persons: Set<Person> = emptySet(),
    val startDate: Long = Clock.System.now().toEpochMilliseconds(),
    val nextDueDate: Long = Clock.System.now().toEpochMilliseconds(),
    val isCompleted: Boolean = false,
    val reminderEnabled: Boolean = true,
    val postAsTransaction: Boolean = true,
    val frequency: InstallmentFrequency = InstallmentFrequency.MONTHLY,
    val description: String = "",
    val isLoading: Boolean = false,
    val showMismatchWarning: Boolean = false,

    // Loan Calculator
    val loanAmount: String = "",
    val loanInstallmentAmount: String = "",
    val loanCount: String = "",
    val loanTotalPayment: Long = 0,
    val loanTotalInterest: Long = 0,

    // Most Used
    val mostUsedCategories: List<Category> = emptyList(),
    val mostUsedSources: List<Source> = emptyList(),
    val mostUsedTags: List<Tag> = emptyList(),
    val mostUsedPersons: List<Person> = emptyList()
)

class AddInstallmentViewModel(
    private val installmentUseCases: InstallmentUseCaseGroup,
    private val transactionUseCaseGroup: TransactionUseCaseGroup
) : ViewModel() {

    private val _state = MutableStateFlow(AddInstallmentState())
    val state: StateFlow<AddInstallmentState> = _state.asStateFlow()

    private val _effects = Channel<AddInstallmentEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<AddInstallmentEffect> = _effects.receiveAsFlow()

    init {
        observeMostUsed()
    }

    private fun observeMostUsed() {
        transactionUseCaseGroup.observeMostUsedCategoriesUseCase(com.kazemieh.common.model.TransactionType.EXPENSE)
            .onEach { list -> _state.update { it.copy(mostUsedCategories = list) } }
            .launchIn(viewModelScope)

        transactionUseCaseGroup.observeMostUsedSourcesUseCase()
            .onEach { list -> _state.update { it.copy(mostUsedSources = list) } }
            .launchIn(viewModelScope)

        transactionUseCaseGroup.observeMostUsedTagsUseCase()
            .onEach { list -> _state.update { it.copy(mostUsedTags = list) } }
            .launchIn(viewModelScope)

        transactionUseCaseGroup.observeMostUsedPersonsUseCase()
            .onEach { list -> _state.update { it.copy(mostUsedPersons = list) } }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: AddInstallmentIntent) {
        when (intent) {
            is AddInstallmentIntent.SetTitle -> _state.update { it.copy(title = intent.title) }
            is AddInstallmentIntent.SetTotalAmount -> setTotalAmount(intent.amount)
            is AddInstallmentIntent.SetInstallmentAmount -> setInstallmentAmount(intent.amount)
            is AddInstallmentIntent.SetTotalInstallments -> setTotalInstallments(intent.total)
            is AddInstallmentIntent.SetPaidInstallments -> _state.update { it.copy(paidInstallments = intent.paid) }
            is AddInstallmentIntent.SetCategory -> _state.update { it.copy(category = intent.category) }
            is AddInstallmentIntent.SetSource -> _state.update { it.copy(source = intent.source) }
            is AddInstallmentIntent.SetTags -> _state.update { it.copy(tags = intent.tags) }
            is AddInstallmentIntent.SetPersons -> _state.update { it.copy(persons = intent.persons) }
            is AddInstallmentIntent.SetStartDate -> _state.update { it.copy(startDate = intent.timeStamp, nextDueDate = intent.timeStamp) }
            is AddInstallmentIntent.SetReminderEnabled -> _state.update { it.copy(reminderEnabled = intent.enabled) }
            is AddInstallmentIntent.SetPostAsTransaction -> _state.update { it.copy(postAsTransaction = intent.enabled) }
            is AddInstallmentIntent.SetReminderTime -> setReminderTime(intent.hour, intent.minute)
            is AddInstallmentIntent.SetFrequency -> _state.update { it.copy(frequency = intent.frequency) }
            is AddInstallmentIntent.LoadInstallment -> loadInstallment(intent.installmentId)
            is AddInstallmentIntent.SetLoanAmount -> setLoanAmount(intent.amount)
            is AddInstallmentIntent.SetLoanInstallmentAmount -> setLoanInstallmentAmount(intent.amount)
            is AddInstallmentIntent.SetLoanCount -> setLoanCount(intent.count)
            is AddInstallmentIntent.SetDescription -> _state.update { it.copy(description = intent.description) }
            is AddInstallmentIntent.ApplyLoanCalculator -> applyLoan()
            is AddInstallmentIntent.Reset -> reset()
            is AddInstallmentIntent.Submit -> submit(intent)
        }
    }

    // Fix for loan setters
    private fun setLoanAmount(amount: String) {
        _state.update { it.copy(loanAmount = amount) }
        calculateLoan()
    }
    private fun setLoanInstallmentAmount(amount: String) {
        _state.update { it.copy(loanInstallmentAmount = amount) }
        calculateLoan(calculateInstallment = false)
    }
    private fun setLoanCount(count: String) {
        _state.update { it.copy(loanCount = count) }
        calculateLoan()
    }

    private fun setTotalAmount(amount: String) {
        val installmentAmount = _state.value.installmentAmount.toLongOrNull() ?: 0
        val totalAmount = amount.toLongOrNull() ?: 0

        var totalInstallments = _state.value.totalInstallments
        var showWarning = false

        if (installmentAmount > 0 && totalAmount > 0) {
            totalInstallments = (totalAmount / installmentAmount).toString()
            showWarning = totalAmount % installmentAmount != 0L
        }

        _state.update {
            it.copy(
                totalAmount = amount,
                totalInstallments = totalInstallments,
                showMismatchWarning = showWarning
            )
        }
    }

    private fun setInstallmentAmount(amount: String) {
        val installmentAmount = amount.toLongOrNull() ?: 0
        val totalAmount = _state.value.totalAmount.toLongOrNull() ?: 0

        var totalInstallments = _state.value.totalInstallments
        var showWarning = false

        if (installmentAmount > 0 && totalAmount > 0) {
            totalInstallments = (totalAmount / installmentAmount).toString()
            showWarning = totalAmount % installmentAmount != 0L
        }

        _state.update {
            it.copy(
                installmentAmount = amount,
                totalInstallments = totalInstallments,
                showMismatchWarning = showWarning
            )
        }
    }

    private fun setTotalInstallments(total: String) {
        val installmentAmount = _state.value.installmentAmount.toLongOrNull() ?: 0
        val count = total.toLongOrNull() ?: 0
        val totalAmount = if (count > 0) installmentAmount * count else 0
        
        _state.update {
            it.copy(
                totalInstallments = total,
                totalAmount = if (totalAmount > 0) totalAmount.toString() else it.totalAmount,
                showMismatchWarning = false
            )
        }
    }

    private fun calculateLoan(calculateInstallment: Boolean = true) {
        val s = _state.value
        val amount = s.loanAmount.toLongOrNull() ?: 0
        val count = s.loanCount.toLongOrNull() ?: 1
        
        var installment = s.loanInstallmentAmount.toLongOrNull() ?: 0
        
        if (calculateInstallment && amount > 0 && count > 0) {
            installment = amount / count
        }

        val totalPayment = installment * count
        val totalInterest = if (totalPayment > amount && amount > 0) totalPayment - amount else 0

        _state.update {
            it.copy(
                loanInstallmentAmount = installment.toString(),
                loanTotalPayment = totalPayment,
                loanTotalInterest = totalInterest
            )
        }
    }

    private fun applyLoan() {
        val s = _state.value
        _state.update {
            it.copy(
                totalAmount = s.loanTotalPayment.toString(),
                installmentAmount = s.loanInstallmentAmount,
                totalInstallments = s.loanCount
            )
        }
    }

    private fun setReminderTime(hour: Int, minute: Int) {
        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(_state.value.startDate)
        val pdt = instant.toPersianDateTime(tz)
        val newTs = pdt.copy(hour = hour, minute = minute, second = 0).toEpochMilliseconds(tz)
        _state.update { it.copy(startDate = newTs, nextDueDate = newTs) }
    }

    private fun reset() {
        _state.value = AddInstallmentState(
            mostUsedCategories = _state.value.mostUsedCategories,
            mostUsedSources = _state.value.mostUsedSources,
            mostUsedTags = _state.value.mostUsedTags,
            mostUsedPersons = _state.value.mostUsedPersons,
            startDate = Clock.System.now().toEpochMilliseconds(),
            nextDueDate = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun loadInstallment(installmentId: Long) {
        viewModelScope.launch {
            val installmentWithRelations = installmentUseCases.getInstallmentUseCase(installmentId) ?: return@launch
            val installment = installmentWithRelations.installment
            _state.update {
                it.copy(
                    installmentId = installment.id,
                    title = installment.title,
                    totalAmount = installment.totalAmount.toString(),
                    installmentAmount = installment.installmentAmount.toString(),
                    totalInstallments = installment.totalInstallments.toString(),
                    paidInstallments = installment.paidInstallments.toString(),
                    category = installmentWithRelations.category,
                    source = installmentWithRelations.source,
                    tags = installmentWithRelations.tags.toSet(),
                    persons = installmentWithRelations.persons.toSet(),
                    startDate = installment.startDate,
                    nextDueDate = installment.nextDueDate,
                    isCompleted = installment.isCompleted,
                    reminderEnabled = installment.reminderEnabled,
                    postAsTransaction = installment.postAsTransaction,
                    frequency = installment.frequency,
                    description = installment.description ?: ""
                )
            }
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
                    id = s.installmentId ?: 0,
                    title = s.title,
                    totalAmount = s.totalAmount.toLongOrNull() ?: 0,
                    installmentAmount = s.installmentAmount.toLongOrNull() ?: 0,
                    totalInstallments = s.totalInstallments.toIntOrNull() ?: 1,
                    paidInstallments = s.paidInstallments.toIntOrNull() ?: 0,
                    categoryId = s.category.id!!,
                    sourceId = s.source.id!!,
                    startDate = s.startDate,
                    nextDueDate = if (s.installmentId != null) s.nextDueDate else s.startDate,
                    isCompleted = s.isCompleted,
                    reminderEnabled = s.reminderEnabled,
                    postAsTransaction = s.postAsTransaction,
                    frequency = s.frequency,
                    description = s.description
                )
                val tagIds = s.tags.mapNotNull { it.id }
                val personIds = s.persons.mapNotNull { it.id }

                if (s.installmentId != null) {
                    installmentUseCases.updateInstallmentUseCase(
                        installment = installment,
                        tagIds = tagIds,
                        personIds = personIds,
                        reminderTitle = intent.reminderTitle,
                        reminderMessage = intent.reminderMessage
                    )
                } else {
                    installmentUseCases.addInstallmentUseCase(
                        installment = installment,
                        tagIds = tagIds,
                        personIds = personIds,
                        reminderTitle = intent.reminderTitle,
                        reminderMessage = intent.reminderMessage
                    )
                }
                _effects.send(AddInstallmentEffect.Success)
            } catch (e: Exception) {
                _effects.send(AddInstallmentEffect.Error(getString(Res.string.transaction_failed)))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
