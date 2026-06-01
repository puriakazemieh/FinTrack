package com.kazemieh.financialsource.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.bottomsheet.FormBottomSheetScaffold
import com.kazemieh.designsystem.component.form.NameDescriptionFields
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.designsystem.model.Bank
import com.kazemieh.designsystem.picker.ColorIconPickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackSourceIcons
import com.kazemieh.designsystem.component.CardItem
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.card_number_label
import fintrack.core.designsystem.generated.resources.description_label
import fintrack.core.designsystem.generated.resources.initial_balance_label
import fintrack.core.designsystem.generated.resources.label_account_number
import fintrack.core.designsystem.generated.resources.label_cvv2
import fintrack.core.designsystem.generated.resources.label_exp_month
import fintrack.core.designsystem.generated.resources.label_exp_year
import fintrack.core.designsystem.generated.resources.label_shaba_number
import fintrack.core.designsystem.generated.resources.source_name_label
import fintrack.core.designsystem.generated.resources.submit_source
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceBottomSheet(
    viewModel: AddSourceViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    selectedSource: Source? = null,
    onDismiss: () -> Unit,
    setSource: (Source) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedSource?.id) {
        if (selectedSource != null) viewModel.onIntent(AddSourceIntent.StartEdit(selectedSource))
        else viewModel.onIntent(AddSourceIntent.StartAdd)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddSourceEffect.ShowMessage -> coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = effect.message.resolveString(),
                        duration = SnackbarDuration.Short
                    )
                }

                is AddSourceEffect.SavedSource -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setSource(effect.source)
                            onDismiss()
                        }
                    }
                }
                AddSourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    FormBottomSheetScaffold(
        sheetState = sheetState,
        onDismissRequest = { viewModel.onIntent(AddSourceIntent.OnDismiss) },
        primaryButtonText = stringResource(Res.string.submit_source),
        onPrimaryClick = { viewModel.onIntent(AddSourceIntent.Save) }
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.mediumLarge)
        ) {

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TypeSource.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = state.draft.type == option,
                            onClick = { viewModel.onIntent(AddSourceIntent.UpdateType(option)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                        ) {
                            FintrackBodyMediumText(text = option.value.asString())
                        }
                    }
                }
            }

            AnimatedVisibility(visible = state.draft.type == TypeSource.CREDIT) {
                val bank = remember(state.draft.cardNumber) {
                    Bank.fromCardNumber(state.draft.cardNumber.orEmpty())
                }
                CardItem(
                    name = state.draft.name,
                    cardNumber = state.draft.cardNumber.orEmpty(),
                    bank = bank,
                    iconId = state.draft.iconId
                )
            }

            NameDescriptionFields(
                name = state.draft.name,
                onNameChange = { viewModel.onIntent(AddSourceIntent.UpdateName(it)) },
                nameLabel = stringResource(Res.string.source_name_label),

                description = state.draft.description.orEmpty(),
                onDescriptionChange = { viewModel.onIntent(AddSourceIntent.UpdateDescription(it)) },
                descriptionLabel = stringResource(Res.string.description_label),

                // ✅ آیکون فعال
                isIconShow = true,
                initialColorId = state.draft.colorId,
                initialIconId = state.draft.iconId,
                onIconClick = { viewModel.onIntent(AddSourceIntent.OpenPicker) },

                between = {
                    FintrackOutlinedTextField(
                        isPrice = true,
                        value = if (state.draft.balance == 0) "" else state.draft.balance.toString(),
                        onValueChange = { input ->
                            val newValue = input.toIntOrNull()
                            when {
                                newValue != null -> viewModel.onIntent(
                                    AddSourceIntent.UpdateBalance(
                                        newValue
                                    )
                                )

                                input.isEmpty() -> viewModel.onIntent(
                                    AddSourceIntent.UpdateBalance(
                                        0
                                    )
                                )
                            }
                        },
                        label = { FintrackBodyMediumText(text = stringResource(Res.string.initial_balance_label)) },
                        textStyle = MaterialTheme.typography.titleLarge,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    AnimatedVisibility(visible = state.draft.type == TypeSource.CREDIT) {
                        Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.mediumLarge)) {
                            FintrackOutlinedTextField(
                                value = state.draft.cardNumber.orEmpty(),
                                isPersianNumber = true,
                                onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateCardNumber(it)) },
                                label = { FintrackBodyMediumText(text = stringResource(Res.string.card_number_label)) },
                                textStyle = MaterialTheme.typography.titleLarge,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium)
                            ) {
                                FintrackOutlinedTextField(
                                    value = state.draft.cvv2.orEmpty(),
                                    isPersianNumber = true,
                                    onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateCvv2(it)) },
                                    label = { FintrackBodyMediumText(text = stringResource(Res.string.label_cvv2)) },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.titleLarge,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                FintrackOutlinedTextField(
                                    value = state.draft.expirationMonth.orEmpty(),
                                    isPersianNumber = true,
                                    onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateExpirationMonth(it)) },
                                    label = { FintrackBodyMediumText(text = stringResource(Res.string.label_exp_month)) },
                                    modifier = Modifier.weight(0.5f),
                                    textStyle = MaterialTheme.typography.titleLarge,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                FintrackOutlinedTextField(
                                    value = state.draft.expirationYear.orEmpty(),
                                    isPersianNumber = true,
                                    onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateExpirationYear(it)) },
                                    label = { FintrackBodyMediumText(text = stringResource(Res.string.label_exp_year)) },
                                    modifier = Modifier.weight(0.5f),
                                    textStyle = MaterialTheme.typography.titleLarge,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }

                            FintrackOutlinedTextField(
                                value = state.draft.shabaNumber.orEmpty(),
                                isPersianNumber = true,
                                onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateShabaNumber(it)) },
                                label = { FintrackBodyMediumText(text = stringResource(Res.string.label_shaba_number)) },
                                textStyle = MaterialTheme.typography.titleLarge,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            FintrackOutlinedTextField(
                                value = state.draft.accountNumber.orEmpty(),
                                isPersianNumber = true,
                                onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateAccountNumber(it)) },
                                label = { FintrackBodyMediumText(text = stringResource(Res.string.label_account_number)) },
                                textStyle = MaterialTheme.typography.titleLarge,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium)
                            ) {
                                FintrackOutlinedTextField(
                                    value = state.draft.branchCode.orEmpty(),
                                    isPersianNumber = true,
                                    onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateBranchCode(it)) },
                                    label = { FintrackBodyMediumText(text = "کد شعبه") },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.titleLarge,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                FintrackOutlinedTextField(
                                    value = state.draft.branchName.orEmpty(),
                                    onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateBranchName(it)) },
                                    label = { FintrackBodyMediumText(text = "نام شعبه") },
                                    textStyle = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            )
        }

        if (state.isPickerOpen) {
            ColorIconPickerBottomSheet(
                initialColorId = state.draft.colorId,
                initialIconId = state.draft.iconId,
                icons = if (state.draft.type == TypeSource.CREDIT) FinTrackSourceIcons.icons else FinTrackIcons.icons,
                onDismiss = { viewModel.onIntent(AddSourceIntent.ClosePicker) },
                onSave = { color, icon ->
                    viewModel.onIntent(AddSourceIntent.SetColorIcon(color.id, icon.id))
                }
            )
        }
    }
}

