package com.kazemieh.goals.presentation.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Goal
import com.kazemieh.common.model.GoalCategory
import com.kazemieh.common.model.GoalPriority
import com.kazemieh.common.model.GoalType
import com.kazemieh.common.model.GoalTemplate
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.StringResource
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
    val glassColors = LocalGlassColors.current

    val openStartDatePicker = remember { mutableStateOf(false) }
    val openEndDatePicker = remember { mutableStateOf(false) }
    var showTargetCalculator by remember { mutableStateOf(false) }
    var showSavedCalculator by remember { mutableStateOf(false) }
    var showMonthlyCalculator by remember { mutableStateOf(false) }
    var showGoalTypePicker by remember { mutableStateOf(false) }
    
    var helpTextRes by remember { mutableStateOf<StringResource?>(null) }

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
                Field(
                    label = stringResource(Res.string.label_type_goal),
                    onClick = { showGoalTypePicker = true },
                    trailingAction = {
                        IconButton(onClick = { helpTextRes = Res.string.help_goal_type }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = glassColors.text3)
                        }
                    }
                ) {
                    val templateName = state.templates.find { it.id == state.templateId }?.name
                    PickerValue(
                        label = templateName ?: when(state.type) {
                            GoalType.SAVINGS -> stringResource(Res.string.goal_type_savings)
                            GoalType.EMERGENCY_FUND -> stringResource(Res.string.goal_type_emergency)
                            GoalType.DEBT_PAYOFF -> stringResource(Res.string.goal_type_debt)
                            GoalType.RETIREMENT -> stringResource(Res.string.goal_type_retirement)
                            GoalType.BIG_PURCHASE -> stringResource(Res.string.goal_type_purchase)
                            GoalType.INVESTMENT -> stringResource(Res.string.goal_type_investment)
                        },
                        icon = Icons.Default.TrackChanges
                    )
                }
            }

            item {
                TitledField(
                    label = stringResource(Res.string.label_title),
                    value = state.name,
                    onValueChange = { onIntent(AddGoalIntent.UpdateName(it)) },
                    placeholder = stringResource(Res.string.hint_enter_goal_name)
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(space.medium)) {
                    Column(modifier = Modifier.weight(1f)) {
                        CategorySelector(
                            selected = state.category,
                            onSelect = { onIntent(AddGoalIntent.UpdateCategory(it)) },
                            onHelp = { helpTextRes = Res.string.help_goal_type }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        PrioritySelector(
                            selected = state.priority,
                            onSelect = { onIntent(AddGoalIntent.UpdatePriority(it)) },
                            onHelp = { helpTextRes = Res.string.help_priority }
                        )
                    }
                }
            }

            item {
                var showBasketPicker by remember { mutableStateOf(false) }
                Field(
                    label = stringResource(Res.string.label_financial_basket),
                    onClick = { showBasketPicker = true }
                ) {
                    val basketName = state.baskets.find { it.id == state.basketId }?.name
                    PickerValue(
                        label = basketName ?: stringResource(Res.string.label_no_basket),
                        icon = Icons.Default.ShoppingBasket
                    )
                }
                
                if (showBasketPicker) {
                    BasketPickerSheet(
                        state = state,
                        onSelect = {
                            onIntent(AddGoalIntent.UpdateBasketId(it))
                            showBasketPicker = false
                        },
                        onDismiss = { showBasketPicker = false }
                    )
                }
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
                        onClick = { openStartDatePicker.value = true },
                        trailingAction = {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp).clickable { helpTextRes = Res.string.help_start_date }, tint = glassColors.text3)
                        }
                    ) {
                        PickerValue(
                            label = DateUtils.formatDate(state.startDate),
                            icon = Icons.Default.CalendarMonth
                        )
                    }

                    Field(
                        label = stringResource(Res.string.label_end_date),
                        modifier = Modifier.weight(1f),
                        onClick = { openEndDatePicker.value = true },
                        trailingAction = {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp).clickable { helpTextRes = Res.string.help_end_date }, tint = glassColors.text3)
                        }
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
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    FintrackLabelSmallText(text = stringResource(Res.string.label_monthly_trend), color = glassColors.text3)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp).clickable { helpTextRes = Res.string.help_monthly_target },
                        tint = glassColors.text3
                    )
                }
            }

            item {
                FeasibilityIndicator(state)
            }

            item {
                TitledField(
                    label = stringResource(Res.string.description),
                    value = state.description,
                    onValueChange = { onIntent(AddGoalIntent.UpdateDescription(it)) },
                    placeholder = stringResource(Res.string.label_note)
                )
            }
        }
    }

    if (showGoalTypePicker) {
        GoalTypePickerSheet(
            state = state,
            onSelectType = {
                onIntent(AddGoalIntent.UpdateType(it))
                onIntent(AddGoalIntent.UpdateTemplateId(null))
                showGoalTypePicker = false
            },
            onSelectTemplate = {
                onIntent(AddGoalIntent.UpdateTemplateId(it.id))
                onIntent(AddGoalIntent.UpdateType(it.systemType))
                showGoalTypePicker = false
            },
            onAddTemplate = { onIntent(AddGoalIntent.AddTemplate(it)) },
            onDeleteTemplate = { onIntent(AddGoalIntent.DeleteTemplate(it)) },
            onDismiss = { showGoalTypePicker = false }
        )
    }

    if (helpTextRes != null) {
        AlertDialog(
            onDismissRequest = { helpTextRes = null },
            confirmButton = {
                TextButton(onClick = { helpTextRes = null }) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            text = { Text(stringResource(helpTextRes!!)) },
            containerColor = glassColors.bg1,
            titleContentColor = glassColors.text,
            textContentColor = glassColors.text2
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalTypePickerSheet(
    state: AddGoalState,
    onSelectType: (GoalType) -> Unit,
    onSelectTemplate: (GoalTemplate) -> Unit,
    onAddTemplate: (GoalTemplate) -> Unit,
    onDeleteTemplate: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val types = listOf(
        GoalType.SAVINGS to Res.string.goal_type_savings,
        GoalType.EMERGENCY_FUND to Res.string.goal_type_emergency,
        GoalType.DEBT_PAYOFF to Res.string.goal_type_debt,
        GoalType.RETIREMENT to Res.string.goal_type_retirement,
        GoalType.BIG_PURCHASE to Res.string.goal_type_purchase,
        GoalType.INVESTMENT to Res.string.goal_type_investment
    )
    
    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    SheetFrame(
        title = stringResource(Res.string.label_type_goal),
        onDismiss = onDismiss,
        primaryButtonText = stringResource(Res.string.label_add_goal_type),
        onPrimaryClick = { showAddDialog = true }
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FintrackLabelMediumText(text = stringResource(Res.string.label_default), color = LocalGlassColors.current.text3)
                Spacer(Modifier.height(8.dp))
            }
            items(types.size) { index ->
                val (type, labelRes) = types[index]
                ItemSelected(
                    isSelected = state.templateId == null && state.type == type,
                    item = ItemUi(id = index.toLong(), title = UiText.StringResourceText(labelRes), iconId = 1, colorId = 1),
                    onToggle = { onSelectType(type) }
                )
            }
            
            if (state.templates.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    FintrackLabelMediumText(text = stringResource(Res.string.label_manage_goal_types), color = LocalGlassColors.current.text3)
                    Spacer(Modifier.height(8.dp))
                }
                items(state.templates.size) { index ->
                    val template = state.templates[index]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            ItemSelected(
                                isSelected = state.templateId == template.id,
                                item = ItemUi(id = template.id, title = UiText.DynamicString(template.name), iconId = 1, colorId = 1),
                                onToggle = { onSelectTemplate(template) }
                            )
                        }
                        IconButton(onClick = { onDeleteTemplate(template.id) }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotEmpty()) {
                        onAddTemplate(GoalTemplate(name = name))
                        showAddDialog = false
                    }
                }) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            title = { Text(stringResource(Res.string.label_add_goal_type)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.label_title)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasketPickerSheet(
    state: AddGoalState,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    SheetFrame(
        title = stringResource(Res.string.label_financial_basket),
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ItemSelected(
                isSelected = state.basketId == null,
                item = ItemUi(id = -1, title = UiText.StringResourceText(Res.string.label_no_basket), iconId = 1, colorId = 1),
                onToggle = { onSelect(null) }
            )
            
            state.baskets.forEach { basket ->
                ItemSelected(
                    isSelected = state.basketId == basket.id,
                    item = ItemUi(id = basket.id, title = UiText.DynamicString(basket.name), iconId = basket.iconId, colorId = basket.colorId),
                    onToggle = { onSelect(basket.id) }
                )
            }
        }
    }
}

@Composable
private fun FeasibilityIndicator(state: AddGoalState) {
    val target = state.targetAmount.toLongOrNull() ?: 0L
    val saved = state.savedAmount.toLongOrNull() ?: 0L
    val monthly = state.monthlyTarget.toLongOrNull() ?: 0L
    
    if (target == 0L || monthly == 0L) return
    
    val remaining = target - saved
    val monthsNeeded = if (monthly > 0) remaining.toFloat() / monthly else Float.MAX_VALUE
    
    val isRealistic = state.endDate?.let {
        val monthsToDeadline = (it - state.startDate) / (1000L * 60 * 60 * 24 * 30)
        monthsNeeded <= monthsToDeadline
    } ?: true

    val glassColors = LocalGlassColors.current
    GlassCard(
        tone = if (isRealistic) GlassTone.Default else GlassTone.Error,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isRealistic) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isRealistic) GlassGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                FintrackLabelSmallText(text = stringResource(Res.string.label_feasibility), color = glassColors.text3)
                FintrackBodyMediumText(
                    text = if (isRealistic) stringResource(Res.string.msg_feasibility_realistic) else stringResource(Res.string.msg_feasibility_unrealistic),
                    color = if (isRealistic) glassColors.text else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
private fun CategorySelector(
    selected: GoalCategory,
    onSelect: (GoalCategory) -> Unit,
    onHelp: () -> Unit
) {
    val categories = listOf(
        GoalCategory.SHORT_TERM to Res.string.category_short,
        GoalCategory.MID_TERM to Res.string.category_mid,
        GoalCategory.LONG_TERM to Res.string.category_long
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FintrackLabelMediumText(text = stringResource(Res.string.label_category_goal))
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp).clickable { onHelp() },
                tint = LocalGlassColors.current.text3
            )
        }
        GlassCard(padding = 4.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                categories.forEach { (cat, labelRes) ->
                    Chip(
                        active = selected == cat,
                        onClick = { onSelect(cat) },
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
private fun PrioritySelector(
    selected: GoalPriority,
    onSelect: (Priority: GoalPriority) -> Unit,
    onHelp: () -> Unit
) {
    val priorities = listOf(
        GoalPriority.LOW to Res.string.priority_low,
        GoalPriority.MEDIUM to Res.string.priority_medium,
        GoalPriority.HIGH to Res.string.priority_high_goal
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FintrackLabelMediumText(text = stringResource(Res.string.label_priority))
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp).clickable { onHelp() },
                tint = LocalGlassColors.current.text3
            )
        }
        GlassCard(padding = 4.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                priorities.forEach { (prio, labelRes) ->
                    Chip(
                        active = selected == prio,
                        onClick = { onSelect(prio) },
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
