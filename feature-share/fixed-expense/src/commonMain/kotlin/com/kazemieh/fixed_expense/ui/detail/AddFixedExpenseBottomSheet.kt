package com.kazemieh.fixed_expense.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackButton
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.LargeAmountCard
import com.kazemieh.designsystem.component.glass.Switch
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.custom_date
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.frequency_daily
import fintrack.core.designsystem.generated.resources.frequency_monthly
import fintrack.core.designsystem.generated.resources.frequency_weekly
import fintrack.core.designsystem.generated.resources.frequency_yearly
import fintrack.core.designsystem.generated.resources.recurrence_none
import fintrack.core.designsystem.generated.resources.hint_transaction_description
import fintrack.core.designsystem.generated.resources.ic_1
import fintrack.core.designsystem.generated.resources.installment_frequency
import fintrack.core.designsystem.generated.resources.label_auto_post_enabled
import fintrack.core.designsystem.generated.resources.label_char_count_limit
import fintrack.core.designsystem.generated.resources.label_end_date
import fintrack.core.designsystem.generated.resources.label_most_used
import fintrack.core.designsystem.generated.resources.label_note
import fintrack.core.designsystem.generated.resources.save_
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.select_source
import fintrack.core.designsystem.generated.resources.source
import fintrack.core.designsystem.generated.resources.start
import fintrack.core.designsystem.generated.resources.title_add_fixed_expense
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFixedExpenseBottomSheet(
    onDismiss: () -> Unit,
    expenseId: Long? = null,
    viewModel: AddFixedExpenseViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            viewModel.onIntent(AddFixedExpenseIntent.LoadExpense(expenseId))
        } else {
            // Start every new "add" with an empty form (no leftover data from a previous save).
            viewModel.onIntent(AddFixedExpenseIntent.Reset)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddFixedExpenseEffect.Saved -> onDismiss()
            }
        }
    }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddFrame(
            title = if (state.expenseId == null) stringResource(Res.string.title_add_fixed_expense)
            else stringResource(Res.string.edit),
            primaryLabel = stringResource(Res.string.save_),
            onPrimaryClick = { viewModel.onIntent(AddFixedExpenseIntent.Submit) },
            onClose = onDismiss,
            showHero = false
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item(key = "amount") {
                    LargeAmountCard(
                        amount = state.amount,
                        onAmountChange = { viewModel.onIntent(AddFixedExpenseIntent.SetAmount(it)) },
                        onCalcClick = { showCalculator = true },
                        autoFocus = state.expenseId == null
                    )
                }

                // Category (optional) + most used
                item {
                    Field(
                        label = stringResource(Res.string.category),
                        required = false,
                        onClick = { showCategoryPicker = true }
                    ) {
                        PickerValue(
                            label = state.category?.name ?: stringResource(Res.string.select_category),
                            icon = FinTrackIcons.findIcon(state.category?.iconId).resource
                        )
                    }
                    MostUsedCategoryChips(
                        items = state.mostUsedCategories,
                        onItemClick = { viewModel.onIntent(AddFixedExpenseIntent.SetCategory(it)) }
                    )
                }

                // Source (optional) + most used
                item {
                    Field(
                        label = stringResource(Res.string.source),
                        required = false,
                        onClick = { showSourcePicker = true }
                    ) {
                        PickerValue(
                            label = state.source?.name ?: stringResource(Res.string.select_source),
                            icon = FinTrackIcons.findIcon(state.source?.iconId).resource
                        )
                    }
                    MostUsedSourceChips(
                        items = state.mostUsedSources,
                        onItemClick = { viewModel.onIntent(AddFixedExpenseIntent.SetSource(it)) }
                    )
                }

                // Recurrence
                item {
                    RecurrenceSelector(
                        selected = state.recurrence,
                        onSelect = { type ->
                            viewModel.onIntent(AddFixedExpenseIntent.SetRecurrence(type))
                            if (type == RecurrenceType.CUSTOM) showRangePicker = true
                        }
                    )
                }

                // Start date
                item {
                    Field(
                        label = stringResource(Res.string.start),
                        onClick = { showStartDatePicker = true }
                    ) {
                        PickerValue(
                            label = state.startDate.toPersianDate(),
                            icon = Icons.Default.CalendarMonth
                        )
                    }
                }

                // Custom range end date (only for CUSTOM)
                if (state.recurrence == RecurrenceType.CUSTOM) {
                    item {
                        Field(
                            label = stringResource(Res.string.label_end_date),
                            onClick = { showRangePicker = true }
                        ) {
                            PickerValue(
                                label = state.endDate?.toPersianDate()
                                    ?: stringResource(Res.string.custom_date),
                                icon = Icons.Default.DateRange
                            )
                        }
                    }
                }

                // Auto post
                item {
                    GlassCard(padding = 14.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.label_auto_post_enabled),
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                on = state.isAutoPostEnabled,
                                onToggle = { viewModel.onIntent(AddFixedExpenseIntent.SetAutoPost(it)) }
                            )
                        }
                    }
                }

                // Note (same style as add-transaction)
                item {
                    NoteCard(
                        value = state.description,
                        onValueChange = { viewModel.onIntent(AddFixedExpenseIntent.SetDescription(it)) }
                    )
                }
            }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerBottomSheet(
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
            onSourceClick = {
                viewModel.onIntent(AddFixedExpenseIntent.SetSource(it))
                showSourcePicker = false
            },
            onDismiss = { showSourcePicker = false }
        )
    }

    if (showStartDatePicker) {
        val open = remember { mutableStateOf(true) }
        LaunchedEffect(open.value) { if (!open.value) showStartDatePicker = false }
        JalaliDatePickerBottomSheet(
            openSheet = open,
            initialDate = JalaliCalendar.fromTimestamp(state.startDate),
            onConfirm = {
                viewModel.onIntent(AddFixedExpenseIntent.SetStartDate(it.toTimestamp()))
                showStartDatePicker = false
            }
        )
    }

    if (showRangePicker) {
        CustomRangeBottomSheet(
            startDate = state.startDate,
            endDate = state.endDate,
            onConfirm = { start, end ->
                viewModel.onIntent(AddFixedExpenseIntent.SetStartDate(start))
                viewModel.onIntent(AddFixedExpenseIntent.SetEndDate(end))
                showRangePicker = false
            },
            onDismiss = { showRangePicker = false }
        )
    }

    if (showCalculator) {
        CalculatorBottomSheet(
            initialAmount = state.amount,
            onConfirm = {
                viewModel.onIntent(AddFixedExpenseIntent.SetAmount(it))
                showCalculator = false
            },
            onDismiss = { showCalculator = false }
        )
    }
}

@Composable
private fun Long.toPersianDate(): String =
    Instant.fromEpochMilliseconds(this)
        .toPersianDateTime(TimeZone.currentSystemDefault())
        .toDateString()

@Composable
private fun RecurrenceSelector(
    selected: RecurrenceType,
    onSelect: (RecurrenceType) -> Unit
) {
    // CUSTOM opens the range picker; ONCE is a one-off (non-recurring) expense.
    val types = listOf(
        RecurrenceType.DAILY to Res.string.frequency_daily,
        RecurrenceType.WEEKLY to Res.string.frequency_weekly,
        RecurrenceType.MONTHLY to Res.string.frequency_monthly,
        RecurrenceType.YEARLY to Res.string.frequency_yearly,
        RecurrenceType.CUSTOM to Res.string.custom_date,
        RecurrenceType.ONCE to Res.string.recurrence_none
    )
    Column {
        FintrackLabelMediumText(
            text = stringResource(Res.string.installment_frequency),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        GlassCard(padding = 10.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.take(3).forEach { (type, labelRes) ->
                        Chip(
                            active = selected == type,
                            onClick = { onSelect(type) },
                            modifier = Modifier.weight(1f)
                        ) {
                            FintrackLabelSmallText(text = stringResource(labelRes))
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.drop(3).forEach { (type, labelRes) ->
                        Chip(
                            active = selected == type,
                            onClick = { onSelect(type) },
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

@Composable
private fun NoteCard(
    value: String,
    onValueChange: (String) -> Unit
) {
    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FintrackLabelSmallText(text = stringResource(Res.string.label_note))
                FintrackLabelSmallText(
                    text = stringResource(
                        Res.string.label_char_count_limit,
                        value.length.toLong().toPersianDigits(),
                        250.toLong().toPersianDigits()
                    )
                )
            }
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= 250) onValueChange(it) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = LocalGlassColors.current.text),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                cursorBrush = Brush.verticalGradient(listOf(GlassGreen, GlassGreen)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            FintrackBodyMediumText(
                                text = stringResource(Res.string.hint_transaction_description)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedCategoryChips(
    items: List<Category>,
    onItemClick: (Category) -> Unit
) {
    if (items.isEmpty()) return
    MostUsedRow {
        items.take(3).forEach { category ->
            Chip(color = GlassGreen, onClick = { onItemClick(category) }) {
                FintrackLabelSmallText(text = category.name, color = GlassGreen, fontSize = 10.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedSourceChips(
    items: List<Source>,
    onItemClick: (Source) -> Unit
) {
    if (items.isEmpty()) return
    MostUsedRow {
        items.take(3).forEach { source ->
            Chip(color = GlassBlue, onClick = { onItemClick(source) }) {
                FintrackLabelSmallText(text = source.name, color = GlassBlue, fontSize = 10.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun PickerValue(
    label: String,
    icon: Any? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackBodyMediumText(text = label, fontWeight = FontWeight.SemiBold)
        when (icon) {
            is org.jetbrains.compose.resources.DrawableResource -> {
                Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(16.dp))
            }

            is ImageVector -> {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            }

            else -> {
                Icon(painter = painterResource(Res.drawable.ic_1), contentDescription = null, modifier = Modifier.size(13.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangeBottomSheet(
    startDate: Long,
    endDate: Long?,
    onConfirm: (start: Long, end: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var localStart by remember { mutableStateOf(startDate) }
    var localEnd by remember { mutableStateOf(endDate ?: startDate) }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FintrackTitleMediumText(text = stringResource(Res.string.custom_date))

                Field(label = stringResource(Res.string.start), onClick = { pickStart = true }) {
                    PickerValue(label = localStart.toPersianDate(), icon = Icons.Default.CalendarMonth)
                }
                Field(label = stringResource(Res.string.label_end_date), onClick = { pickEnd = true }) {
                    PickerValue(label = localEnd.toPersianDate(), icon = Icons.Default.DateRange)
                }

                FintrackButton(
                    text = stringResource(Res.string.save_),
                    onClick = { onConfirm(localStart, localEnd) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (pickStart) {
        val open = remember { mutableStateOf(true) }
        LaunchedEffect(open.value) { if (!open.value) pickStart = false }
        JalaliDatePickerBottomSheet(
            openSheet = open,
            initialDate = JalaliCalendar.fromTimestamp(localStart),
            onConfirm = { localStart = it.toTimestamp(); pickStart = false }
        )
    }

    if (pickEnd) {
        val open = remember { mutableStateOf(true) }
        LaunchedEffect(open.value) { if (!open.value) pickEnd = false }
        JalaliDatePickerBottomSheet(
            openSheet = open,
            initialDate = JalaliCalendar.fromTimestamp(localEnd),
            disableBeforeDate = JalaliCalendar.fromTimestamp(localStart),
            onConfirm = { localEnd = it.toTimestamp(); pickEnd = false }
        )
    }
}
