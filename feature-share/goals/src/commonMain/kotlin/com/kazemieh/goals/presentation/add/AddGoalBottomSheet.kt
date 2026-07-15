package com.kazemieh.goals.presentation.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    viewModel: AddGoalViewModel = koinViewModel(),
    goal: Goal? = null,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(goal) {
        viewModel.onIntent(AddGoalIntent.InitialData(goal))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is AddGoalEffect.GoalSaved) {
                onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddGoalContent(
            state = state,
            onIntent = viewModel::onIntent,
            onClose = onDismiss
        )
    }
}

@Composable
fun AddGoalContent(
    state: AddGoalState,
    onIntent: (AddGoalIntent) -> Unit,
    onClose: () -> Unit
) {
    val space = LocalSpacing.current

    val openStartDatePicker = remember { mutableStateOf(false) }
    val openEndDatePicker = remember { mutableStateOf(false) }
    var showTargetCalculator by remember { mutableStateOf(false) }
    var showSavedCalculator by remember { mutableStateOf(false) }
    var showMonthlyCalculator by remember { mutableStateOf(false) }

    AddFrame(
        title = if (state.id == 0L) stringResource(Res.string.label_new_goal) else stringResource(Res.string.edit),
        sub = stringResource(Res.string.title_savings_goals),
        primaryLabel = stringResource(Res.string.btn_save_goal),
        onPrimaryClick = { onIntent(AddGoalIntent.SaveGoal) },
        onClose = onClose,
        showHero = false
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                TitledField(
                    label = stringResource(Res.string.label_title),
                    value = state.name,
                    onValueChange = { onIntent(AddGoalIntent.UpdateName(it)) },
                    placeholder = stringResource(Res.string.hint_enter_goal_name)
                )
            }

            item {
                LargeAmountCard(
                    amount = state.targetAmount,
                    onAmountChange = { onIntent(AddGoalIntent.UpdateTargetAmount(it)) },
                    onCalcClick = { showTargetCalculator = true },
                    label = stringResource(Res.string.label_target_amount),
                    autoFocus = state.id == 0L
                )
            }

            item {
                LargeAmountCard(
                    amount = state.savedAmount,
                    onAmountChange = { onIntent(AddGoalIntent.UpdateSavedAmount(it)) },
                    onCalcClick = { showSavedCalculator = true },
                    label = stringResource(Res.string.label_saved_amount)
                )
            }

            item {
                RecurrenceSelector(
                    selected = state.recurrence,
                    onSelect = { onIntent(AddGoalIntent.UpdateRecurrence(it)) }
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(space.medium)) {
                    Field(
                        label = stringResource(Res.string.start),
                        modifier = Modifier.weight(1f),
                        onClick = { openStartDatePicker.value = true }
                    ) {
                        PickerValue(
                            label = DateUtils.formatDate(state.startDate),
                            icon = Icons.Default.CalendarMonth
                        )
                    }

                    Field(
                        label = stringResource(Res.string.label_end_date),
                        modifier = Modifier.weight(1f),
                        onClick = { openEndDatePicker.value = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PickerValue(
                                label = state.endDate?.let { DateUtils.formatDate(it) } ?: stringResource(Res.string.label_no_end_date),
                                icon = Icons.Default.DateRange
                            )
                            if (state.endDate != null) {
                                FintrackLabelSmallText(
                                    text = stringResource(Res.string.action_clear),
                                    color = GlassGreen,
                                    modifier = Modifier.clickable {
                                        onIntent(AddGoalIntent.UpdateEndDate(null))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                LargeAmountCard(
                    amount = state.monthlyTarget,
                    onAmountChange = { onIntent(AddGoalIntent.UpdateMonthlyTarget(it)) },
                    onCalcClick = { showMonthlyCalculator = true },
                    label = stringResource(Res.string.label_monthly_target)
                )
            }
        }
    }

    if (showTargetCalculator) {
        CalculatorBottomSheet(
            initialAmount = state.targetAmount,
            onConfirm = {
                onIntent(AddGoalIntent.UpdateTargetAmount(it))
                showTargetCalculator = false
            },
            onDismiss = { showTargetCalculator = false }
        )
    }

    if (showSavedCalculator) {
        CalculatorBottomSheet(
            initialAmount = state.savedAmount,
            onConfirm = {
                onIntent(AddGoalIntent.UpdateSavedAmount(it))
                showSavedCalculator = false
            },
            onDismiss = { showSavedCalculator = false }
        )
    }

    if (showMonthlyCalculator) {
        CalculatorBottomSheet(
            initialAmount = state.monthlyTarget,
            onConfirm = {
                onIntent(AddGoalIntent.UpdateMonthlyTarget(it))
                showMonthlyCalculator = false
            },
            onDismiss = { showMonthlyCalculator = false }
        )
    }

    JalaliDatePickerBottomSheet(
        openSheet = openStartDatePicker,
        initialDate = JalaliCalendar.fromTimestamp(state.startDate),
        onConfirm = { onIntent(AddGoalIntent.UpdateStartDate(it.toTimestamp())) }
    )

    JalaliDatePickerBottomSheet(
        openSheet = openEndDatePicker,
        initialDate = JalaliCalendar.fromTimestamp(state.endDate ?: state.startDate),
        disableBeforeDate = JalaliCalendar.fromTimestamp(state.startDate),
        onConfirm = { onIntent(AddGoalIntent.UpdateEndDate(it.toTimestamp())) }
    )
}

@Composable
private fun TitledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val glassColors = LocalGlassColors.current
    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FintrackLabelSmallText(text = label, color = glassColors.text3)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = glassColors.text),
                cursorBrush = Brush.verticalGradient(listOf(GlassGreen, GlassGreen)),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            FintrackBodyMediumText(text = placeholder, color = glassColors.text3)
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun RecurrenceSelector(
    selected: RecurrenceType,
    onSelect: (RecurrenceType) -> Unit
) {
    val types = listOf(
        RecurrenceType.DAILY to Res.string.frequency_daily,
        RecurrenceType.WEEKLY to Res.string.frequency_weekly,
        RecurrenceType.MONTHLY to Res.string.frequency_monthly,
        RecurrenceType.YEARLY to Res.string.frequency_yearly
    )
    Column {
        FintrackLabelMediumText(
            text = stringResource(Res.string.installment_frequency),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        GlassCard(padding = 10.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                types.forEach { (type, labelRes) ->
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
