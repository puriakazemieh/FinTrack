package com.kazemieh.financialsource.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.GlassEdge
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenSoft
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.CardItem
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackHeadlineLargeText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glassTextFieldColors
import com.kazemieh.designsystem.component.FourDigitGroupingTransformation
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.NumberCommaTransformation
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.ColorSwatches
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.IconGrid
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.designsystem.model.Bank
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.designsystem.picker.FinTrackSourceIcons
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.btn_save_source
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.hint_source_name_placeholder
import fintrack.core.designsystem.generated.resources.initial_balance_label
import fintrack.core.designsystem.generated.resources.label_account_number
import fintrack.core.designsystem.generated.resources.label_account_shaba
import fintrack.core.designsystem.generated.resources.label_branch
import fintrack.core.designsystem.generated.resources.label_branch_code
import fintrack.core.designsystem.generated.resources.label_branch_name
import fintrack.core.designsystem.generated.resources.label_card_16_digits
import fintrack.core.designsystem.generated.resources.label_card_info
import fintrack.core.designsystem.generated.resources.label_color
import fintrack.core.designsystem.generated.resources.label_cvv2
import fintrack.core.designsystem.generated.resources.label_exp_month
import fintrack.core.designsystem.generated.resources.label_exp_year
import fintrack.core.designsystem.generated.resources.label_icon
import fintrack.core.designsystem.generated.resources.label_optional
import fintrack.core.designsystem.generated.resources.label_shaba_no_ir
import fintrack.core.designsystem.generated.resources.label_type
import fintrack.core.designsystem.generated.resources.label_zero
import fintrack.core.designsystem.generated.resources.source_name_label
import fintrack.core.designsystem.generated.resources.title_edit_source
import fintrack.core.designsystem.generated.resources.title_new_source
import fintrack.core.designsystem.generated.resources.title_source_management
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceBottomSheet(
    viewModel: AddSourceViewModel = koinViewModel(),
    selectedSource: Source? = null,
    onDismiss: () -> Unit,
    onNavigateToTransactions: ((Source) -> Unit)? = null,
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
                is AddSourceEffect.SavedSource -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setSource(effect.source)
                        }
                    }
                }

                AddSourceEffect.OnDismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(AddSourceIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddSourceContent(
            state = state,
            onIntent = viewModel::onIntent,
            onNavigateToTransactions = onNavigateToTransactions
        )
    }
}

@Composable
fun AddSourceContent(
    state: AddSourceState,
    onIntent: (AddSourceIntent) -> Unit,
    onNavigateToTransactions: ((Source) -> Unit)? = null
) {
    val glassColors = LocalGlassColors.current
    val rainbowColors = FinTrackPickerColors.rainbow()
    val colors = rainbowColors.map { it.color }
    val selectedColorIndex = remember(state.draft.colorId, rainbowColors) {
        rainbowColors.indexOfFirst { it.id == state.draft.colorId }.coerceAtLeast(0)
    }
    val selectedIconIndex = remember(state.draft.iconId, state.draft.type) {
        val icons =
            if (state.draft.type == TypeSource.CREDIT) FinTrackSourceIcons.icons else FinTrackIcons.icons
        icons.indexOfFirst { it.id == state.draft.iconId }.coerceAtLeast(0)
    }

    AddFrame(
        title = if (state.mode == AddSourceMode.Add) stringResource(Res.string.title_new_source) else stringResource(
            Res.string.title_edit_source
        ),
        sub = stringResource(Res.string.title_source_management),
        primaryLabel = stringResource(Res.string.btn_save_source),
        onPrimaryClick = { onIntent(AddSourceIntent.Save) },
        onClose = { onIntent(AddSourceIntent.OnDismiss) },
        onFilterClick = if (state.mode is AddSourceMode.Edit && onNavigateToTransactions != null) {
            {
                (state.mode as? AddSourceMode.Edit)?.sourceId?.let { id ->
                    onNavigateToTransactions(
                        Source(
                            id = id,
                            name = state.draft.name,
                            description = state.draft.description,
                            balance = state.draft.balance,
                            type = state.draft.type.ordinal + 1, // TypeSource mapping might need verification
                            cardNumber = state.draft.cardNumber,
                            colorId = state.draft.colorId ?: 1,
                            iconId = state.draft.iconId ?: 1
                        )
                    )
                    onIntent(AddSourceIntent.OnDismiss)
                }
            }
        } else null,
        hero = {
            if (state.draft.type == TypeSource.CREDIT) {
                val bank = remember(state.draft.cardNumber) {
                    Bank.fromCardNumber(state.draft.cardNumber.orEmpty())
                }
                CardItem(
                    name = state.draft.name,
                    cardNumber = state.draft.cardNumber.orEmpty(),
                    bank = bank,
                    iconId = state.draft.iconId
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FinTrackLeadingIcon(
                        colorId = state.draft.colorId ?: 1,
                        iconId = state.draft.iconId ?: 1,
                        style = LeadingIconStyle.Badge,
                        size = 64.dp,
                        iconSize = 32.dp
                    )
                    if (state.draft.name.isNotEmpty()) {
                        FintrackHeadlineLargeText(
                            text = state.draft.name,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.text
                        )
                    }
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SourceTypeSelector(
                    selectedType = state.draft.type,
                    onTypeSelected = { onIntent(AddSourceIntent.UpdateType(it)) }
                )
            }

            item {
                Field(label = stringResource(Res.string.source_name_label), required = true) {
                    TextField(
                        value = state.draft.name,
                        onValueChange = { onIntent(AddSourceIntent.UpdateName(it)) },
                        placeholder = {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.hint_source_name_placeholder),
                                color = glassColors.text3
                            )
                        },
                        colors = glassTextFieldColors(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = glassColors.text,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Field(label = stringResource(Res.string.initial_balance_label)) {
                    TextField(
                        value = if (state.draft.balance == 0) "" else state.draft.balance.toString(),
                        onValueChange = { input ->
                            val newValue = input.toIntOrNull() ?: 0
                            onIntent(AddSourceIntent.UpdateBalance(newValue))
                        },
                        visualTransformation = NumberCommaTransformation(),
                        placeholder = {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.label_zero),
                                color = glassColors.text3
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = GlassGreen
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = GlassGreen,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        suffix = {
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.currency_toman),
                                color = glassColors.text3
                            )
                        }
                    )
                }
            }

            if (state.draft.type == TypeSource.CREDIT) {
                item {
                    GlassCard(padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.label_card_info) + " (" + stringResource(
                                    Res.string.label_optional
                                ) + ")",
                                color = glassColors.text3
                            )

                            TextField(
                                value = state.draft.cardNumber.orEmpty(),
                                onValueChange = { onIntent(AddSourceIntent.UpdateCardNumber(it)) },
                                placeholder = {
                                    FintrackBodyMediumText(
                                        text = stringResource(Res.string.label_card_16_digits),
                                        color = glassColors.text3
                                    )
                                },
                                visualTransformation = FourDigitGroupingTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = glassTextFieldColors()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextField(
                                    value = state.draft.cvv2.orEmpty(),
                                    onValueChange = { onIntent(AddSourceIntent.UpdateCvv2(it)) },
                                    placeholder = {
                                        FintrackBodyMediumText(
                                            text = stringResource(Res.string.label_cvv2),
                                            color = glassColors.text3
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = glassTextFieldColors()
                                )
                                TextField(
                                    value = state.draft.expirationMonth.orEmpty(),
                                    onValueChange = {
                                        onIntent(
                                            AddSourceIntent.UpdateExpirationMonth(
                                                it
                                            )
                                        )
                                    },
                                    placeholder = {
                                        FintrackBodyMediumText(
                                            text = stringResource(Res.string.label_exp_month),
                                            color = glassColors.text3
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.5f),
                                    colors = glassTextFieldColors()
                                )
                                TextField(
                                    value = state.draft.expirationYear.orEmpty(),
                                    onValueChange = {
                                        onIntent(
                                            AddSourceIntent.UpdateExpirationYear(
                                                it
                                            )
                                        )
                                    },
                                    placeholder = {
                                        FintrackBodyMediumText(
                                            text = stringResource(Res.string.label_exp_year),
                                            color = glassColors.text3
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.5f),
                                    colors = glassTextFieldColors()
                                )
                            }
                        }
                    }
                }

                item {
                    GlassCard(padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.label_account_shaba) + " (" + stringResource(
                                    Res.string.label_optional
                                ) + ")",
                                color = glassColors.text3
                            )
                            TextField(
                                value = state.draft.shabaNumber.orEmpty(),
                                onValueChange = { onIntent(AddSourceIntent.UpdateShabaNumber(it)) },
                                placeholder = {
                                    FintrackBodyMediumText(
                                        text = stringResource(Res.string.label_shaba_no_ir),
                                        color = glassColors.text3
                                    )
                                },
                                visualTransformation = FourDigitGroupingTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = glassTextFieldColors()
                            )
                            TextField(
                                value = state.draft.accountNumber.orEmpty(),
                                onValueChange = { onIntent(AddSourceIntent.UpdateAccountNumber(it)) },
                                placeholder = {
                                    FintrackBodyMediumText(
                                        text = stringResource(Res.string.label_account_number),
                                        color = glassColors.text3
                                    )
                                },
                                visualTransformation = FourDigitGroupingTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = glassTextFieldColors()
                            )
                        }
                    }
                }

                item {
                    GlassCard(padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.label_branch) + " (" + stringResource(
                                    Res.string.label_optional
                                ) + ")",
                                color = glassColors.text3
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextField(
                                    value = state.draft.branchCode.orEmpty(),
                                    onValueChange = { onIntent(AddSourceIntent.UpdateBranchCode(it)) },
                                    placeholder = {
                                        FintrackBodyMediumText(
                                            text = stringResource(Res.string.label_branch_code),
                                            color = glassColors.text3
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = glassTextFieldColors()
                                )
                                TextField(
                                    value = state.draft.branchName.orEmpty(),
                                    onValueChange = { onIntent(AddSourceIntent.UpdateBranchName(it)) },
                                    placeholder = {
                                        FintrackBodyMediumText(
                                            text = stringResource(Res.string.label_branch_name),
                                            color = glassColors.text3
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = glassTextFieldColors()
                                )
                            }
                        }
                    }
                }
            }

            if (state.draft.type != TypeSource.CREDIT) {
                item {
                    GlassCard(padding = 14.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FintrackLabelMediumText(
                                text = stringResource(Res.string.label_color),
                                color = glassColors.text3
                            )
                            ColorSwatches(
                                colors = colors,
                                pickedIndex = selectedColorIndex,
                                onColorPick = {
                                    onIntent(
                                        AddSourceIntent.SetColorIcon(
                                            rainbowColors[it].id,
                                            state.draft.iconId
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FintrackLabelMediumText(
                            text = stringResource(Res.string.label_icon),
                            color = glassColors.text3
                        )
                        IconGrid(
                            icons = if (state.draft.type == TypeSource.CREDIT) FinTrackSourceIcons.icons else FinTrackIcons.icons,
                            pickedIndex = selectedIconIndex,
                            color = rainbowColors[selectedColorIndex].color,
                            onIconPick = {
                                onIntent(
                                    AddSourceIntent.SetColorIcon(
                                        state.draft.colorId,
                                        if (state.draft.type == TypeSource.CREDIT) FinTrackSourceIcons.icons[it].id else FinTrackIcons.icons[it].id
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceTypeSelector(
    selectedType: TypeSource,
    onTypeSelected: (TypeSource) -> Unit
) {
    GlassCard(padding = 14.dp) {
        val glassColors = LocalGlassColors.current
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FintrackLabelSmallText(text = stringResource(Res.string.label_type), color = glassColors.text3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TypeSource.entries.forEach { option ->
                    val active = option == selectedType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) GlassGreenSoft else Color.Transparent)
                            .then(
                                if (active) Modifier.border(
                                    1.dp,
                                    GlassGreen.copy(alpha = 0.33f),
                                    RoundedCornerShape(10.dp)
                                )
                                else Modifier.border(1.dp, glassColors.glassEdge, RoundedCornerShape(10.dp))
                            )
                            .clickable { onTypeSelected(option) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FintrackBodyMediumText(
                            text = option.value.asString(),
                            color = if (active) GlassGreen else glassColors.text3
                        )
                    }
                }
            }
        }
    }
}

