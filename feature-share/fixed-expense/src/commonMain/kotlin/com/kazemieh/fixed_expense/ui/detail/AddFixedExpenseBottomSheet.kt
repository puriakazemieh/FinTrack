package com.kazemieh.fixed_expense.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import fintrack.core.designsystem.generated.resources.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFixedExpenseBottomSheet(
    onDismiss: () -> Unit,
    viewModel: AddFixedExpenseViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddFixedExpenseEffect.Saved -> onDismiss()
            }
        }
    }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var showRecurrencePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
        ) {
            ScreenHeader(
                title = "افزودن هزینه ثابت",
                onClose = onDismiss
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(space.large),
                verticalArrangement = Arrangement.spacedBy(space.medium)
            ) {
                // Amount
                FintrackOutlinedTextField(
                    value = state.amount,
                    onValueChange = { viewModel.onIntent(AddFixedExpenseIntent.SetAmount(it)) },
                    label = { FintrackBodyMediumText("مبلغ هزینه") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Category Picker
                PickerField(
                    label = "دسته‌بندی",
                    value = state.category?.name ?: "انتخاب دسته‌بندی",
                    icon = Icons.Default.Category,
                    onClick = { showCategoryPicker = true }
                )

                // Source Picker
                PickerField(
                    label = "منبع مالی",
                    value = state.source?.name ?: "انتخاب منبع",
                    icon = Icons.Default.Wallet,
                    onClick = { showSourcePicker = true }
                )

                // Recurrence Picker
                PickerField(
                    label = "دوره تکرار",
                    value = when (state.recurrence) {
                        RecurrenceType.DAILY -> "روزانه"
                        RecurrenceType.WEEKLY -> "هفتگی"
                        RecurrenceType.MONTHLY -> "ماهانه"
                        RecurrenceType.YEARLY -> "سالانه"
                        RecurrenceType.CUSTOM -> "سفارشی"
                        RecurrenceType.ONCE -> "یک‌بار"
                    },
                    icon = Icons.Default.Repeat,
                    onClick = { showRecurrencePicker = true }
                )

                // Start Date Picker
                PickerField(
                    label = "تاریخ شروع",
                    value = Instant.fromEpochMilliseconds(state.startDate).toPersianDateTime(TimeZone.currentSystemDefault()).toDateString(),
                    icon = Icons.Default.CalendarMonth,
                    onClick = { /* TODO: Date Picker */ }
                )

                // Auto Post Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FintrackBodyLargeText("ثبت خودکار در سررسید")
                    Switch(
                        checked = state.isAutoPostEnabled,
                        onCheckedChange = { viewModel.onIntent(AddFixedExpenseIntent.SetAutoPost(it)) }
                    )
                }

                // Description
                FintrackOutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.onIntent(AddFixedExpenseIntent.SetDescription(it)) },
                    label = { FintrackBodyMediumText("توضیحات") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(space.medium))

                Button(
                    onClick = { viewModel.onIntent(AddFixedExpenseIntent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    FintrackBodyLargeText(stringResource(Res.string.save_))
                }
            }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerBottomSheet(
            snackbarHostState = remember { SnackbarHostState() },
            transactionType = TransactionType.EXPENSE,
            onCategoryClick = {
                viewModel.onIntent(AddFixedExpenseIntent.SetCategory(it))
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }

    if (showSourcePicker) {
        SourcePickerBottomSheet(
            snackbarHostState = remember { SnackbarHostState() },
            onSourceClick = {
                viewModel.onIntent(AddFixedExpenseIntent.SetSource(it))
                showSourcePicker = false
            },
            onDismiss = { showSourcePicker = false }
        )
    }

    if (showRecurrencePicker) {
        RecurrencePickerBottomSheet(
            selectedRecurrence = state.recurrence,
            onRecurrenceClick = {
                viewModel.onIntent(AddFixedExpenseIntent.SetRecurrence(it))
                showRecurrencePicker = false
            },
            onDismiss = { showRecurrencePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrencePickerBottomSheet(
    selectedRecurrence: RecurrenceType,
    onRecurrenceClick: (RecurrenceType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FintrackTitleMediumText("انتخاب دوره تکرار", modifier = Modifier.padding(bottom = 16.dp))
            RecurrenceType.entries.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecurrenceClick(type) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = type == selectedRecurrence, onClick = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    FintrackBodyLargeText(
                        when (type) {
                            RecurrenceType.DAILY -> "روزانه"
                            RecurrenceType.WEEKLY -> "هفتگی"
                            RecurrenceType.MONTHLY -> "ماهانه"
                            RecurrenceType.YEARLY -> "سالانه"
                            RecurrenceType.CUSTOM -> "سفارشی"
                            RecurrenceType.ONCE -> "یک‌بار"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val space = LocalSpacing.current
    Column(modifier = Modifier.fillMaxWidth()) {
        FintrackLabelMediumText(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(space.extraSmall))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier.padding(space.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(space.small))
                FintrackBodyLargeText(text = value, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
