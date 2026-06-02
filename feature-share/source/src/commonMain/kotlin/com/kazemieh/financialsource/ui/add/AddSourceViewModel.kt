package com.kazemieh.financialsource.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.model.Source
import com.kazemieh.common.SnackbarController
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.designsystem.model.Bank
import com.kazemieh.designsystem.picker.FinTrackSourceIcons
import fintrack.core.designsystem.generated.resources.*
import com.kazemieh.domain.usecase.SourceUseCases
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.Resource


class AddSourceViewModel(
    private val sourceUseCases: SourceUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(AddSourceState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddSourceEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var editJob: Job? = null

    fun onIntent(intent: AddSourceIntent) {
        when (intent) {
            AddSourceIntent.StartAdd -> startAdd()
            is AddSourceIntent.StartEdit -> startEdit(intent.source)

            is AddSourceIntent.UpdateName -> updateDraft { it.copy(name = intent.value) }
            is AddSourceIntent.UpdateDescription -> updateDraft { it.copy(description = intent.value) }
            is AddSourceIntent.UpdateBalance -> updateDraft { it.copy(balance = intent.value) }
            is AddSourceIntent.UpdateCardNumber -> updateDraft {
                val limitedValue = intent.value?.take(16)
                val bank = Bank.fromCardNumber(limitedValue.orEmpty())
                if (bank != Bank.Unknown && it.isIconManuallySelected.not()) {
                    val bankIconId = FinTrackSourceIcons.findIdByResource(bank.logo)
                    it.copy(cardNumber = limitedValue, iconId = bankIconId)
                } else {
                    it.copy(cardNumber = limitedValue)
                }
            }

            is AddSourceIntent.UpdateAccountNumber -> updateDraft { it.copy(accountNumber = intent.value) }
            is AddSourceIntent.UpdateBranchCode -> updateDraft { it.copy(branchCode = intent.value) }
            is AddSourceIntent.UpdateBranchName -> updateDraft { it.copy(branchName = intent.value) }
            is AddSourceIntent.UpdateCvv2 -> updateDraft { it.copy(cvv2 = intent.value?.take(4)) }
            is AddSourceIntent.UpdateExpirationMonth -> updateDraft { it.copy(expirationMonth = intent.value?.take(2)) }
            is AddSourceIntent.UpdateExpirationYear -> updateDraft { it.copy(expirationYear = intent.value?.take(2)) }
            is AddSourceIntent.UpdateShabaNumber -> updateDraft { it.copy(shabaNumber = intent.value) }

            is AddSourceIntent.UpdateType -> updateDraft {
                if (intent.value == TypeSource.CASH) {
                    val newIconId = if ((it.iconId ?: 0) >= 1000) 1 else it.iconId
                    it.copy(
                        type = intent.value,
                        cardNumber = null,
                        iconId = newIconId,
                        isIconManuallySelected = false
                    )
                } else {
                    val newIconId = if ((it.iconId ?: 0) < 1000) 1000 else it.iconId
                    it.copy(
                        type = intent.value,
                        iconId = newIconId,
                        isIconManuallySelected = false
                    )
                }
            }

            // ✅ Picker
            AddSourceIntent.OpenPicker -> _state.update { it.copy(isPickerOpen = true) }
            AddSourceIntent.ClosePicker -> _state.update { it.copy(isPickerOpen = false) }
            is AddSourceIntent.SetColorIcon -> _state.update {
                it.copy(
                    draft = it.draft.copy(
                        colorId = intent.colorId,
                        iconId = intent.iconId,
                        isIconManuallySelected = true
                    ),
                    isPickerOpen = false
                )
            }

            AddSourceIntent.Save -> save()
            AddSourceIntent.OnDismiss -> dismiss()
        }
    }

    private fun updateDraft(transform: (SourceDraft) -> SourceDraft) {
        _state.update { it.copy(draft = transform(it.draft)) }
    }

    private fun startAdd() {
        editJob?.cancel()
        _state.update {
            AddSourceState(
                mode = AddSourceMode.Add,
                draft = SourceDraft(type = TypeSource.CREDIT),
                isPickerOpen = false
            )
        }
    }

    private fun startEdit(source: Source) {
        editJob?.cancel()

        val id = source.id ?: 0L
        _state.update {
            AddSourceState(
                mode = AddSourceMode.Edit(id),
                draft = source.toDraft(),
                isPickerOpen = false
            )
        }

        // اگر لازم داری از DB نسخه کامل رو بخونی:
        editJob = viewModelScope.launch {
            sourceUseCases.observeSourceUseCase(id).collect { db ->
                db?.let {
                    _state.update { st ->
                        st.copy(
                            mode = AddSourceMode.Edit(it.id ?: id),
                            draft = it.toDraft()
                        )
                    }
                }
            }
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            editJob?.cancel()
            _state.update { AddSourceState() }
            _effect.send(AddSourceEffect.OnDismiss)
        }
    }

    private fun save() = with(_state.value) {
        viewModelScope.launch {
            if (draft.name.isBlank()) {
                SnackbarController.showMessage(UiText.StringResourceText(Res.string.check_name_financial_source).resolveString())
                return@launch
            }

            val source = draft.toSource(id = mode.sourceIdOrNull)

            val sourceId = when (mode) {
                AddSourceMode.Add -> sourceUseCases.addSource(source)
                is AddSourceMode.Edit -> sourceUseCases.updateSourceUseCase(source).toLong()
            }

            if (sourceId >= 0) {
                _effect.send(AddSourceEffect.SavedSource(source.copy(id = sourceId)))
                _state.update { AddSourceState() }
            }
        }
    }
}

/** --- State / Mode / Draft --- */

data class AddSourceState(
    val mode: AddSourceMode = AddSourceMode.Add,
    val draft: SourceDraft = SourceDraft(),
    val isPickerOpen: Boolean = false
)

sealed interface AddSourceMode {
    data object Add : AddSourceMode
    data class Edit(val sourceId: Long) : AddSourceMode
}

private val AddSourceMode.sourceIdOrNull: Long?
    get() = (this as? AddSourceMode.Edit)?.sourceId

data class SourceDraft(
    val name: String = "",
    val description: String? = null,
    val balance: Int = 0,
    val type: TypeSource = TypeSource.CREDIT,
    val cardNumber: String? = null,
    val colorId: Int? = null,
    val iconId: Int? = null,
    val shabaNumber: String? = null,
    val accountNumber: String? = null,
    val cvv2: String? = null,
    val expirationMonth: String? = null,
    val expirationYear: String? = null,
    val branchCode: String? = null,
    val branchName: String? = null,
    val isIconManuallySelected: Boolean = false
)

private fun Source.toDraft(): SourceDraft = SourceDraft(
    name = this.name.orEmpty(),
    description = this.description,
    balance = this.balance,
    type = if (this.type == 1) TypeSource.CREDIT else TypeSource.CASH,
    cardNumber = this.cardNumber,
    colorId = this.colorId,
    iconId = this.iconId,
    shabaNumber = this.shabaNumber,
    accountNumber = this.accountNumber,
    cvv2 = this.cvv2,
    expirationMonth = this.expirationMonth,
    expirationYear = this.expirationYear,
    branchCode = this.branchCode,
    branchName = this.branchName
)

private fun SourceDraft.toSource(id: Long?): Source = Source(
    id = id,
    name = name,
    balance = balance,
    cardNumber = cardNumber,
    description = description,
    type = type.count,
    colorId = colorId ?: 1,
    iconId = iconId ?: 1,
    shabaNumber = shabaNumber,
    accountNumber = accountNumber,
    cvv2 = cvv2,
    expirationMonth = expirationMonth,
    expirationYear = expirationYear,
    branchCode = branchCode,
    branchName = branchName
)

/** --- Intent / Effect --- */

sealed interface AddSourceIntent {
    data object StartAdd : AddSourceIntent
    data class StartEdit(val source: Source) : AddSourceIntent

    data class UpdateName(val value: String) : AddSourceIntent
    data class UpdateDescription(val value: String?) : AddSourceIntent
    data class UpdateBalance(val value: Int) : AddSourceIntent
    data class UpdateType(val value: TypeSource) : AddSourceIntent
    data class UpdateCardNumber(val value: String?) : AddSourceIntent
    data class UpdateShabaNumber(val value: String?) : AddSourceIntent
    data class UpdateAccountNumber(val value: String?) : AddSourceIntent
    data class UpdateCvv2(val value: String?) : AddSourceIntent
    data class UpdateExpirationMonth(val value: String?) : AddSourceIntent
    data class UpdateExpirationYear(val value: String?) : AddSourceIntent
    data class UpdateBranchCode(val value: String?) : AddSourceIntent
    data class UpdateBranchName(val value: String?) : AddSourceIntent

    // ✅ Picker
    data object OpenPicker : AddSourceIntent
    data object ClosePicker : AddSourceIntent
    data class SetColorIcon(val colorId: Int?, val iconId: Int?) : AddSourceIntent

    data object Save : AddSourceIntent
    data object OnDismiss : AddSourceIntent
}

enum class TypeSource(val count: Int, val value: UiText) {
    CREDIT(1, UiText.StringResourceText(Res.string.credit_)),
    CASH(2,UiText.StringResourceText( Res.string.cash_))
}


sealed interface AddSourceEffect {
    data class SavedSource(val source: Source) : AddSourceEffect
    data object OnDismiss : AddSourceEffect
}

