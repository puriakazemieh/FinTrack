package com.kazemieh.financialsource.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.bottomsheet.FormBottomSheetScaffold
import com.kazemieh.designsystem.component.form.NameDescriptionFields
import com.kazemieh.designsystem.picker.ColorIconPickerBottomSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
    val context = LocalContext.current
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
                        message = context.getString(effect.message),
                        duration = SnackbarDuration.Short
                    )
                }

                is AddSourceEffect.SavedSource -> setSource(effect.source)
                AddSourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    FormBottomSheetScaffold(
        sheetState = sheetState,
        onDismissRequest = { viewModel.onIntent(AddSourceIntent.OnDismiss) },
        primaryButtonText = stringResource(R.string.submit_source),
        onPrimaryClick = { viewModel.onIntent(AddSourceIntent.Save) }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.mediumLarge)) {

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TypeSource.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = state.draft.type == option,
                            onClick = { viewModel.onIntent(AddSourceIntent.UpdateType(option)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                        ) {
                            FintrackBodyMediumText(text = stringResource(option.value))
                        }
                    }
                }
            }

            NameDescriptionFields(
                name = state.draft.name,
                onNameChange = { viewModel.onIntent(AddSourceIntent.UpdateName(it)) },
                nameLabel = stringResource(R.string.source_name_label),

                description = state.draft.description.orEmpty(),
                onDescriptionChange = { viewModel.onIntent(AddSourceIntent.UpdateDescription(it)) },
                descriptionLabel = stringResource(R.string.description_label),

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
                                newValue != null -> viewModel.onIntent(AddSourceIntent.UpdateBalance(newValue))
                                input.isEmpty() -> viewModel.onIntent(AddSourceIntent.UpdateBalance(0))
                            }
                        },
                        label = { FintrackBodyMediumText(text = stringResource(R.string.initial_balance_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    AnimatedVisibility(visible = state.draft.type == TypeSource.CREDIT) {
                        FintrackOutlinedTextField(
                            value = state.draft.cardNumber.orEmpty(),
                            onValueChange = { viewModel.onIntent(AddSourceIntent.UpdateCardNumber(it)) },
                            label = { FintrackBodyMediumText(text = stringResource(R.string.card_number_label)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            )
        }

        if (state.isPickerOpen) {
            ColorIconPickerBottomSheet(
                initialColorId = state.draft.colorId,
                initialIconId = state.draft.iconId,
                onDismiss = { viewModel.onIntent(AddSourceIntent.ClosePicker) },
                onSave = { color, icon ->
                    viewModel.onIntent(AddSourceIntent.SetColorIcon(color.id, icon.id))
                }
            )
        }
    }
}

