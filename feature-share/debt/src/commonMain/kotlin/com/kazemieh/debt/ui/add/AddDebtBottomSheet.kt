package com.kazemieh.debt.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenDark
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.jalali.DatePickerField
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.jalali.JalaliCalendar
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.person.ui.list.PersonPickerSingleBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.layout.Row

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddDebtBottomSheet(
    onDismiss: () -> Unit,
    debtId: Long? = null,
    personId: Long? = null,
    viewModel: AddDebtViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val glassColors = LocalGlassColors.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(debtId, personId) {
        if (debtId != null) {
            viewModel.onIntent(AddDebtIntent.LoadDebt(debtId))
        } else {
            viewModel.onIntent(AddDebtIntent.Reset)
            if (personId != null) {
                viewModel.onIntent(AddDebtIntent.SetPersonById(personId))
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddDebtEffect.Saved -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismiss()
                        }
                    }
                }
                AddDebtEffect.OnDismiss -> {
                    onDismiss()
                }
                is AddDebtEffect.ShowMessage -> {
                    // Show snackbar or message
                }
            }
        }
    }

    var showPersonPicker by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    val colors = FinTrackPickerColors.rainbow()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddFrame(
            title = if (state.debtId == null) stringResource(Res.string.add_debt) else stringResource(Res.string.edit),
            sub = stringResource(Res.string.title_debts_management),
            primaryLabel = stringResource(Res.string.save_),
            onPrimaryClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                viewModel.onIntent(AddDebtIntent.Submit)
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
                    DebtTypeSelector(
                        selected = state.type,
                        onSelect = { viewModel.onIntent(AddDebtIntent.SetType(it)) }
                    )
                }

                item {
                    LargeAmountCard(
                        amount = state.amount,
                        onAmountChange = { viewModel.onIntent(AddDebtIntent.SetAmount(it)) },
                        label = stringResource(Res.string.debt_amount),
                        autoFocus = state.debtId == null,
                        onCalcClick = { showCalculator = true }
                    )
                }

                item {
                    Field(label = stringResource(Res.string.person_name), required = true, onClick = { showPersonPicker = true }) {
                        PickerValue(
                            label = state.person?.name ?: stringResource(Res.string.person_choose),
                            icon = Icons.Default.Person
                        )
                    }
                    MostUsedPersonChips(items = state.mostUsedPersons, selectedItem = state.person, onItemClick = { viewModel.onIntent(AddDebtIntent.SetPerson(it)) })
                }

                item {
                    Field(label = stringResource(Res.string.category), onClick = { showCategoryPicker = true }) {
                        val color = colors.firstOrNull { it.id == state.category?.colorId }?.color ?: GlassGreen
                        PickerValue(
                            label = state.category?.name ?: stringResource(Res.string.select_category),
                            color = color,
                            icon = FinTrackIcons.findIcon(state.category?.iconId).resource
                        )
                    }
                    MostUsedCategoryChips(items = state.mostUsedCategories, onItemClick = { viewModel.onIntent(AddDebtIntent.SetCategory(it)) })
                }

                item {
                    Field(label = stringResource(Res.string.source), onClick = { showSourcePicker = true }) {
                        PickerValue(
                            label = state.source?.name ?: stringResource(Res.string.select_source),
                            color = GlassBlue,
                            icon = FinTrackIcons.findIcon(state.source?.iconId).resource
                        )
                    }
                    MostUsedSourceChips(items = state.mostUsedSources, onItemClick = { viewModel.onIntent(AddDebtIntent.SetSource(it)) })
                }

                item {
                    SectionContainer(
                        title = stringResource(Res.string.tags),
                        sub = stringResource(Res.string.title_tag_management),
                        onAddClick = { showTagPicker = true },
                        addLabel = stringResource(Res.string.btn_add_tag)
                    ) {
                        state.tags.forEach { tag ->
                            val color = colors.firstOrNull { it.id == tag.colorId }?.color ?: GlassBlue
                            RemovableChip(
                                label = stringResource(Res.string.label_tag_prefix, tag.name),
                                color = color,
                                onRemove = { viewModel.onIntent(AddDebtIntent.SetTags(state.tags - tag)) }
                            )
                        }
                    }
                    MostUsedTagChips(items = state.mostUsedTags, selectedItems = state.tags, onItemClick = { tag ->
                        val newSet = state.tags.toMutableSet()
                        if (newSet.contains(tag)) newSet.remove(tag) else newSet.add(tag)
                        viewModel.onIntent(AddDebtIntent.SetTags(newSet))
                    })
                }

                item {
                    Field(label = stringResource(Res.string.date), required = true, onClick = { showDatePicker = true }) {
                        val dateStr = Instant.fromEpochMilliseconds(state.date)
                            .toPersianDateTime(TimeZone.currentSystemDefault()).toDateString()
                        PickerValue(label = dateStr, color = glassColors.text, icon = Icons.Default.CalendarMonth)
                    }
                }

                item {
                    GlassCard(padding = 14.dp) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Notifications, null, tint = GlassGreen, modifier = Modifier.size(20.dp))
                                FintrackBodyLargeText(text = stringResource(Res.string.reminder), fontWeight = FontWeight.SemiBold)
                            }
                            Switch(on = state.reminderEnabled, onToggle = { viewModel.onIntent(AddDebtIntent.SetReminderEnabled(it)) })
                        }
                    }
                }

                if (state.reminderEnabled) {
                    item {
                        Field(label = stringResource(Res.string.label_time), onClick = { showTimePicker = true }) {
                            val timeStr = com.kazemieh.common.util.DateUtils.formatTime(state.dueDate ?: state.date)
                            PickerValue(label = timeStr.toPersianDigits(), color = glassColors.text, icon = Icons.Default.Schedule)
                        }
                    }
                }

                item {
                    GlassCard(padding = 14.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                FintrackLabelSmallText(text = stringResource(Res.string.label_note))
                                FintrackLabelSmallText(text = stringResource(Res.string.label_char_count_limit, state.description.length.toLong().toPersianDigits(), 250.toLong().toPersianDigits()))
                            }
                            BasicTextField(
                                value = state.description,
                                onValueChange = { if (it.length <= 250) viewModel.onIntent(AddDebtIntent.SetDescription(it)) },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = glassColors.text),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                cursorBrush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(GlassGreen, GlassGreen)),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = @Composable { innerTextField ->
                                    Box {
                                        if (state.description.isEmpty()) FintrackBodyMediumText(text = stringResource(Res.string.hint_transaction_description))
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showPersonPicker) {
            PersonPickerSingleBottomSheet(
                onPersonClick = {
                    viewModel.onIntent(AddDebtIntent.SetPerson(it))
                    showPersonPicker = false
                },
                onDismiss = { showPersonPicker = false }
            )
        }
        if (showCategoryPicker) {
            val transactionType = if (state.type == DebtType.OWED_TO_ME) TransactionType.INCOME else TransactionType.EXPENSE
            CategoryPickerBottomSheet(transactionType = transactionType, onCategoryClick = { viewModel.onIntent(AddDebtIntent.SetCategory(it)); showCategoryPicker = false }, onDismiss = { showCategoryPicker = false })
        }
        if (showSourcePicker) {
            SourcePickerBottomSheet(onSourceClick = { viewModel.onIntent(AddDebtIntent.SetSource(it)); showSourcePicker = false }, onDismiss = { showSourcePicker = false })
        }
        if (showTagPicker) {
            TagPickerBottomSheet(selectedTags = state.tags, onSubmitClick = { viewModel.onIntent(AddDebtIntent.SetTags(it ?: emptySet())); showTagPicker = false }, onDismiss = { showTagPicker = false })
        }
        if (showTimePicker) {
            val openSheet = remember { mutableStateOf(true) }
            LaunchedEffect(openSheet.value) { if (!openSheet.value) showTimePicker = false }
            com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet(
                openSheet = openSheet,
                initialTime = com.kazemieh.common.util.DateUtils.timeOfDay(state.dueDate ?: state.date),
                onConfirm = { time ->
                    val parts = time.split(":")
                    viewModel.onIntent(AddDebtIntent.SetReminderTime(parts[0].toInt(), parts[1].toInt()))
                    showTimePicker = false
                }
            )
        }
        if (showDatePicker) {
            val open = remember { mutableStateOf(true) }
            LaunchedEffect(open.value) { if (!open.value) showDatePicker = false }
            JalaliDatePickerBottomSheet(
                openSheet = open,
                initialDate = JalaliCalendar.fromTimestamp(state.date),
                onConfirm = {
                    viewModel.onIntent(AddDebtIntent.SetDate(it.toTimestamp()))
                    showDatePicker = false
                }
            )
        }
        if (showCalculator) {
            CalculatorBottomSheet(
                initialAmount = state.amount,
                onConfirm = {
                    viewModel.onIntent(AddDebtIntent.SetAmount(it))
                    showCalculator = false
                },
                onDismiss = { showCalculator = false }
            )
        }
    }
}

@Composable
private fun DebtTypeSelector(
    selected: DebtType,
    onSelect: (DebtType) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val types = listOf(
        DebtType.OWED_BY_ME to stringResource(Res.string.debt_owed_by_me),
        DebtType.OWED_TO_ME to stringResource(Res.string.debt_owed_to_me)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(glassColors.glass, RoundedCornerShape(14.dp))
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        types.forEach { (type, label) ->
            val active = type == selected
            val color = when (type) {
                DebtType.OWED_TO_ME -> GlassGreen
                else -> GlassRed
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (active) Modifier
                            .background(color.copy(alpha = 0.14f))
                            .border(1.dp, color.copy(alpha = 0.33f), RoundedCornerShape(10.dp))
                        else Modifier
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                FintrackBodyMediumText(
                    text = label,
                    color = if (active) color else glassColors.text3
                )
            }
        }
    }
}

@Composable
private fun PickerValue(
    label: String,
    color: Color = LocalGlassColors.current.text,
    icon: Any? = null
) {
    val glassColors = LocalGlassColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackBodyMediumText(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        when (icon) {
            is org.jetbrains.compose.resources.DrawableResource -> {
                Icon(
                    painter = org.jetbrains.compose.resources.painterResource(icon),
                    contentDescription = null,
                    tint = glassColors.text3,
                    modifier = Modifier.size(16.dp)
                )
            }

            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = glassColors.text3,
                    modifier = Modifier.size(16.dp)
                )
            }

            else -> {
                Icon(
                    painter = org.jetbrains.compose.resources.painterResource(Res.drawable.ic_1),
                    contentDescription = null,
                    tint = glassColors.text3,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedCategoryChips(items: List<com.kazemieh.common.model.Category>, onItemClick: (com.kazemieh.common.model.Category) -> Unit) {
    if (items.isEmpty()) return
    val colors = FinTrackPickerColors.rainbow()
    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.take(3).forEach { category ->
                val color = colors.firstOrNull { it.id == category.colorId }?.color ?: GlassGreen
                Chip(color = color, onClick = { onItemClick(category) }) {
                    FintrackLabelSmallText(text = category.name, color = color, fontSize = 10.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedSourceChips(items: List<com.kazemieh.common.model.Source>, onItemClick: (com.kazemieh.common.model.Source) -> Unit) {
    if (items.isEmpty()) return
    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.take(3).forEach { source ->
                Chip(color = GlassBlue, onClick = { onItemClick(source) }) {
                    FintrackLabelSmallText(text = source.name, color = GlassBlue, fontSize = 10.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedTagChips(items: List<com.kazemieh.common.model.Tag>, selectedItems: Set<com.kazemieh.common.model.Tag>, onItemClick: (com.kazemieh.common.model.Tag) -> Unit) {
    if (items.isEmpty()) return
    val colors = FinTrackPickerColors.rainbow()
    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.take(3).forEach { tag ->
                val active = selectedItems.contains(tag)
                val color = colors.firstOrNull { it.id == tag.colorId }?.color ?: GlassBlue
                Chip(color = color, active = active, onClick = { onItemClick(tag) }) {
                    FintrackLabelSmallText(text = "#${tag.name}", color = if (active) Color.White else color, fontSize = 10.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedPersonChips(items: List<com.kazemieh.common.model.Person>, selectedItem: com.kazemieh.common.model.Person?, onItemClick: (com.kazemieh.common.model.Person) -> Unit) {
    if (items.isEmpty()) return
    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.take(3).forEach { person ->
                val active = selectedItem?.id == person.id
                Chip(color = GlassGreen, active = active, onClick = { onItemClick(person) }) {
                    FintrackLabelSmallText(text = person.name, color = if (active) Color.White else GlassGreen, fontSize = 10.sp)
                }
            }
        }
    }
}
