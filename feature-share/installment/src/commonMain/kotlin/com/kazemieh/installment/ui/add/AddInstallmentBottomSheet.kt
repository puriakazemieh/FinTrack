package com.kazemieh.installment.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.InstallmentFrequency
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.installment.ui.add.component.LargeInstallmentField
import com.kazemieh.installment.ui.add.component.PickerValue
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.add_installment
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.date
import fintrack.core.designsystem.generated.resources.dp_today
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.frequency_daily
import fintrack.core.designsystem.generated.resources.frequency_monthly
import fintrack.core.designsystem.generated.resources.frequency_weekly
import fintrack.core.designsystem.generated.resources.frequency_yearly
import fintrack.core.designsystem.generated.resources.installment_amount
import fintrack.core.designsystem.generated.resources.installment_frequency
import fintrack.core.designsystem.generated.resources.installment_title
import fintrack.core.designsystem.generated.resources.save_
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.select_source
import fintrack.core.designsystem.generated.resources.source
import fintrack.core.designsystem.generated.resources.total_installment_amount
import fintrack.core.designsystem.generated.resources.total_installments
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstallmentBottomSheet(
    viewModel: AddInstallmentViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    installmentId: Long? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(installmentId) {
        if (installmentId != null) viewModel.onIntent(AddInstallmentIntent.LoadInstallment(installmentId))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AddInstallmentEffect.Success -> {
                    onSuccess()
                    onDismiss()
                }

                is AddInstallmentEffect.Error -> {
                    // Show snackbar?
                }
            }
        }
    }

    val glassColors = LocalGlassColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        val colors = FinTrackPickerColors.rainbow()
        var showCategoryPicker by remember { mutableStateOf(false) }
        var showSourcePicker by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }

        AddFrame(
            title = if (state.installmentId == null) stringResource(Res.string.add_installment) else stringResource(Res.string.edit),
            sub = stringResource(Res.string.installment_title),
            primaryLabel = stringResource(Res.string.save_),
            onPrimaryClick = {
                val reminderTitle = state.title
                val reminderMessage =
                    state.installmentAmount.toIntOrNull()?.toSignedPersianPrice() ?: "0"
                viewModel.onIntent(
                    AddInstallmentIntent.Submit(
                        reminderTitle = reminderTitle,
                        reminderMessage = reminderMessage
                    )
                )
            },
            onClose = onDismiss,
            showHero = false
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    LargeInstallmentField(
                        value = state.title,
                        label = stringResource(Res.string.installment_title),
                        onValueChange = { viewModel.onIntent(AddInstallmentIntent.SetTitle(it)) }
                    )
                }

                item {
                    LargeInstallmentField(
                        value = state.installmentAmount,
                        label = stringResource(Res.string.installment_amount),
                        onValueChange = {
                            viewModel.onIntent(
                                AddInstallmentIntent.SetInstallmentAmount(
                                    it
                                )
                            )
                        },
                        keyboardType = KeyboardType.Number
                    )
                }

                item {
                    LargeInstallmentField(
                        value = state.totalAmount,
                        label = stringResource(Res.string.total_installment_amount),
                        onValueChange = { viewModel.onIntent(AddInstallmentIntent.SetTotalAmount(it)) },
                        keyboardType = KeyboardType.Number
                    )
                }

                item {
                    LargeInstallmentField(
                        value = state.totalInstallments,
                        label = stringResource(Res.string.total_installments),
                        onValueChange = {
                            viewModel.onIntent(
                                AddInstallmentIntent.SetTotalInstallments(
                                    it
                                )
                            )
                        },
                        keyboardType = KeyboardType.Number
                    )
                }

                item {
                    Field(
                        label = stringResource(Res.string.category),
                        required = true,
                        onClick = { showCategoryPicker = true }
                    ) {
                        val color = colors.firstOrNull { it.id == state.category?.colorId }?.color
                            ?: GlassGreen
                        PickerValue(
                            label = state.category?.name
                                ?: stringResource(Res.string.select_category),
                            color = color,
                            icon = FinTrackIcons.findIcon(state.category?.iconId).resource
                        )
                    }
                }

                item {
                    Field(
                        label = stringResource(Res.string.source),
                        required = true,
                        onClick = { showSourcePicker = true }
                    ) {
                        PickerValue(
                            label = state.source?.name ?: stringResource(Res.string.select_source),
                            color = GlassBlue,
                            icon = FinTrackIcons.findIcon(state.source?.iconId).resource
                        )
                    }
                }

                item {
                    Field(
                        label = stringResource(Res.string.date),
                        required = true,
                        onClick = { showDatePicker = true }
                    ) {
                        PickerValue(
                            label = stringResource(Res.string.dp_today),
                            color = glassColors.text2,
                            icon = Icons.Default.CalendarToday
                        )
                    }
                }

                item {
                    Field(
                        label = stringResource(Res.string.installment_frequency),
                        required = true
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            InstallmentFrequency.entries.forEach { freq ->
                                val labelRes = when (freq) {
                                    InstallmentFrequency.DAILY -> Res.string.frequency_daily
                                    InstallmentFrequency.WEEKLY -> Res.string.frequency_weekly
                                    InstallmentFrequency.MONTHLY -> Res.string.frequency_monthly
                                    InstallmentFrequency.YEARLY -> Res.string.frequency_yearly
                                }
                                Chip(
                                    active = state.frequency == freq,
                                    onClick = { viewModel.onIntent(AddInstallmentIntent.SetFrequency(freq)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    FintrackLabelSmallText(text = stringResource(labelRes))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCategoryPicker) {
            CategoryPickerBottomSheet(
                transactionType = TransactionType.EXPENSE,
                onCategoryClick = {
                    viewModel.onIntent(AddInstallmentIntent.SetCategory(it))
                    showCategoryPicker = false
                },
                onDismiss = { showCategoryPicker = false }
            )
        }

        if (showSourcePicker) {
            SourcePickerBottomSheet(
                onSourceClick = {
                    viewModel.onIntent(AddInstallmentIntent.SetSource(it))
                    showSourcePicker = false
                },
                onDismiss = { showSourcePicker = false }
            )
        }

        if (showDatePicker) {
            val openSheet = remember { mutableStateOf(true) }
            LaunchedEffect(openSheet.value) {
                if (!openSheet.value) showDatePicker = false
            }
            JalaliDatePickerBottomSheet(
                openSheet = openSheet,
                onConfirm = { jalaliCalendar ->
                    viewModel.onIntent(AddInstallmentIntent.SetStartDate(jalaliCalendar.toTimestamp()))
                }
            )
        }
    }
}
