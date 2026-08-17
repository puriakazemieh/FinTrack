package com.kazemieh.check.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.LargeAmountCard
import com.kazemieh.designsystem.component.glass.RemovableChip
import com.kazemieh.designsystem.component.glass.SectionContainer
import com.kazemieh.designsystem.component.glass.Switch
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.jalali.JalaliCalendar
import com.kazemieh.person.ui.list.PersonPickerSingleBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCheckBottomSheet(
    onDismiss: () -> Unit,
    checkId: Long? = null,
    viewModel: AddCheckViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = FinTrackPickerColors.rainbow()

    LaunchedEffect(checkId) {
        if (checkId == null) viewModel.onIntent(AddCheckIntent.Reset)
        else viewModel.onIntent(AddCheckIntent.LoadCheck(checkId))
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { if (it is AddCheckEffect.Saved) onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddFrame(
            title = if (state.checkId == null) stringResource(Res.string.title_add_check) else stringResource(Res.string.edit),
            sub = stringResource(Res.string.sub_check_list_management_desc),
            primaryLabel = stringResource(Res.string.save_),
            onPrimaryClick = { viewModel.onIntent(AddCheckIntent.Submit) },
            onClose = onDismiss,
            showHero = false
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    CheckTypeSelector(
                        isIncoming = state.isIncoming,
                        onIncomingChange = { viewModel.onIntent(AddCheckIntent.SetIsIncoming(it)) }
                    )
                }
                item {
                    LargeAmountCard(
                        amount = state.amount,
                        onAmountChange = { viewModel.onIntent(AddCheckIntent.SetAmount(it)) },
                        onCalcClick = { viewModel.onIntent(AddCheckIntent.ToggleSheet(AddCheckSheet.Calculator)) },
                        autoFocus = state.checkId == null
                    )
                }
                item {
                    Field(label = stringResource(Res.string.category), onClick = { viewModel.onIntent(AddCheckIntent.ToggleSheet(AddCheckSheet.CategoryPicker)) }) {
                        CheckPickerValue(
                            label = state.category?.name ?: stringResource(Res.string.select_category),
                            drawable = FinTrackIcons.findIcon(state.category?.iconId).resource
                        )
                    }
                    MostUsedRow(state.mostUsedCategories.map { it.name }) { index ->
                        state.mostUsedCategories.getOrNull(index)?.let { viewModel.onIntent(AddCheckIntent.SetCategory(it)) }
                    }
                }
                item {
                    Field(label = stringResource(Res.string.source), onClick = { viewModel.onIntent(AddCheckIntent.ToggleSheet(AddCheckSheet.SourcePicker)) }) {
                        CheckPickerValue(
                            label = state.source?.name ?: stringResource(Res.string.select_source),
                            drawable = FinTrackIcons.findIcon(state.source?.iconId).resource
                        )
                    }
                    MostUsedRow(state.mostUsedSources.map { it.name }, color = GlassBlue) { index ->
                        state.mostUsedSources.getOrNull(index)?.let { viewModel.onIntent(AddCheckIntent.SetSource(it)) }
                    }
                }
                item {
                    Field(label = stringResource(Res.string.label_counterparty), required = true, onClick = { viewModel.onIntent(AddCheckIntent.ToggleSheet(AddCheckSheet.PersonPicker)) }) {
                        CheckPickerValue(
                            label = state.person?.name ?: stringResource(Res.string.person_choose),
                            icon = Icons.Default.Person
                        )
                    }
                    MostUsedRow(state.mostUsedPersons.map { it.name }) { index ->
                        state.mostUsedPersons.getOrNull(index)?.let { viewModel.onIntent(AddCheckIntent.SetPerson(it)) }
                    }
                }
                item {
                    SectionContainer(
                        title = stringResource(Res.string.tags),
                        sub = stringResource(Res.string.title_tag_management),
                        onAddClick = { viewModel.onIntent(AddCheckIntent.ToggleSheet(AddCheckSheet.TagPicker)) },
                        addLabel = stringResource(Res.string.btn_add_tag)
                    ) {
                        state.tags.forEach { tag ->
                            val color = colors.firstOrNull { it.id == tag.colorId }?.color ?: GlassBlue
                            RemovableChip(
                                label = stringResource(Res.string.label_tag_prefix, tag.name),
                                color = color,
                                onRemove = { viewModel.onIntent(AddCheckIntent.SetTags(state.tags - tag)) }
                            )
                        }
                    }
                    MostUsedRow(state.mostUsedTags.map { "#${it.name}" }, color = GlassBlue) { index ->
                        state.mostUsedTags.getOrNull(index)?.let { tag ->
                            val tags = state.tags.toMutableSet()
                            if (!tags.add(tag)) tags.remove(tag)
                            viewModel.onIntent(AddCheckIntent.SetTags(tags))
                        }
                    }
                }
                item {
                    Field(label = stringResource(Res.string.label_issue_date), required = true, onClick = { viewModel.onIntent(AddCheckIntent.ToggleSheet(AddCheckSheet.IssueDatePicker)) }) {
                        CheckPickerValue(
                            label = state.date.toPersianDate(),
                            icon = Icons.Default.CalendarMonth
                        )
                    }
                }
                item {
                    Field(label = stringResource(Res.string.label_due_date), required = true, onClick = { viewModel.onIntent(AddCheckIntent.ToggleSheet(AddCheckSheet.DueDatePicker)) }) {
                        CheckPickerValue(
                            label = state.dueDate.toPersianDate(),
                            icon = Icons.Default.CalendarMonth
                        )
                    }
                }
                item {
                    GlassCard(padding = 14.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, null, tint = GlassGreen, modifier = Modifier.size(20.dp))
                                FintrackBodyLargeText(stringResource(Res.string.reminder), fontWeight = FontWeight.SemiBold)
                            }
                            Switch(on = state.reminderEnabled, onToggle = { viewModel.onIntent(AddCheckIntent.SetReminderEnabled(it)) })
                        }
                    }
                }
                if (state.reminderEnabled) item {
                    Field(label = stringResource(Res.string.label_time), onClick = { viewModel.onIntent(AddCheckIntent.ToggleSheet(AddCheckSheet.ReminderTimePicker)) }) {
                        CheckPickerValue(
                            label = DateUtils.formatTime(state.dueDate).toPersianDigits(),
                            icon = Icons.Default.Schedule
                        )
                    }
                }
                item {
                    CheckStatusSelector(state.status) { viewModel.onIntent(AddCheckIntent.SetStatus(it)) }
                }
                item {
                    FintrackOutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onIntent(AddCheckIntent.SetDescription(it)) },
                        label = { FintrackBodyMediumText(stringResource(Res.string.description)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    when (state.topSheet) {
        AddCheckSheet.Calculator -> CalculatorBottomSheet(
            initialAmount = state.amount,
            onConfirm = { viewModel.onIntent(AddCheckIntent.SetAmount(it)); viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) },
            onDismiss = { viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) }
        )
        AddCheckSheet.CategoryPicker -> CategoryPickerBottomSheet(
            transactionType = if (state.isIncoming) TransactionType.INCOME else TransactionType.EXPENSE,
            onCategoryClick = { viewModel.onIntent(AddCheckIntent.SetCategory(it)); viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) },
            onDismiss = { viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) }
        )
        AddCheckSheet.SourcePicker -> SourcePickerBottomSheet(
            onSourceClick = { viewModel.onIntent(AddCheckIntent.SetSource(it)); viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) },
            onDismiss = { viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) }
        )
        AddCheckSheet.PersonPicker -> PersonPickerSingleBottomSheet(
            onPersonClick = { viewModel.onIntent(AddCheckIntent.SetPerson(it)); viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) },
            onDismiss = { viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) }
        )
        AddCheckSheet.TagPicker -> TagPickerBottomSheet(
            selectedTags = state.tags,
            onSubmitClick = { viewModel.onIntent(AddCheckIntent.SetTags(it ?: emptySet())); viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) },
            onDismiss = { viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) }
        )
        AddCheckSheet.IssueDatePicker -> CheckDatePicker(state.date) {
            viewModel.onIntent(AddCheckIntent.SetDate(it))
            viewModel.onIntent(AddCheckIntent.ToggleSheet(null))
        }
        AddCheckSheet.DueDatePicker -> CheckDatePicker(state.dueDate) {
            viewModel.onIntent(AddCheckIntent.SetDueDate(it))
            viewModel.onIntent(AddCheckIntent.ToggleSheet(null))
        }
        AddCheckSheet.ReminderTimePicker -> {
            val open = remember { mutableStateOf(true) }
            LaunchedEffect(open.value) { if (!open.value) viewModel.onIntent(AddCheckIntent.ToggleSheet(null)) }
            FintrackTimePickerBottomSheet(
                openSheet = open,
                initialTime = DateUtils.timeOfDay(state.dueDate),
                onConfirm = { time ->
                    time.split(":").takeIf { it.size == 2 }?.let { viewModel.onIntent(AddCheckIntent.SetReminderTime(it[0].toInt(), it[1].toInt())) }
                    viewModel.onIntent(AddCheckIntent.ToggleSheet(null))
                }
            )
        }
        null -> Unit
    }
}

@Composable
private fun CheckDatePicker(initialDate: Long, onConfirm: (Long) -> Unit) {
    val open = remember { mutableStateOf(true) }
    JalaliDatePickerBottomSheet(
        openSheet = open,
        initialDate = JalaliCalendar.fromTimestamp(initialDate),
        onConfirm = { onConfirm(it.toTimestamp()) }
    )
}

@Composable
private fun CheckTypeSelector(isIncoming: Boolean, onIncomingChange: (Boolean) -> Unit) {
    val glassColors = LocalGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(glassColors.glass)
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CheckTypeOption(stringResource(Res.string.label_outgoing_check), !isIncoming, GlassRed, Modifier.weight(1f)) { onIncomingChange(false) }
        CheckTypeOption(stringResource(Res.string.label_incoming_check), isIncoming, GlassGreen, Modifier.weight(1f)) { onIncomingChange(true) }
    }
}

@Composable
private fun CheckTypeOption(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = .14f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        FintrackBodyMediumText(text, color = if (selected) color else LocalGlassColors.current.text2, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CheckPickerValue(label: String, drawable: DrawableResource? = null, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            drawable != null -> Icon(painterResource(drawable), null, modifier = Modifier.size(17.dp), tint = GlassGreen)
            icon != null -> Icon(icon, null, modifier = Modifier.size(17.dp), tint = GlassGreen)
        }
        FintrackBodyMediumText(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MostUsedRow(items: List<String>, color: Color = GlassGreen, onClick: (Int) -> Unit) {
    if (items.isEmpty()) return
    FlowRow(
        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FintrackLabelSmallText(stringResource(Res.string.label_most_used), color = LocalGlassColors.current.text3)
        items.take(3).forEachIndexed { index, label ->
            Chip(color = color, onClick = { onClick(index) }) {
                FintrackLabelSmallText(label, color = color)
            }
        }
    }
}

@Composable
private fun CheckStatusSelector(selected: CheckStatus, onSelect: (CheckStatus) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FintrackBodyMediumText(stringResource(Res.string.label_check_status_title), color = LocalGlassColors.current.text2)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            CheckStatus.entries.forEach { status ->
                val label = when (status) {
                    CheckStatus.PENDING -> Res.string.label_check_status_ongoing
                    CheckStatus.PASSED -> Res.string.label_check_status_passed
                    CheckStatus.REJECTED -> Res.string.label_check_status_returned
                    CheckStatus.CANCELLED -> Res.string.label_check_status_cancelled
                }
                Chip(active = selected == status, onClick = { onSelect(status) }, modifier = Modifier.weight(1f)) {
                    FintrackLabelSmallText(stringResource(label))
                }
            }
        }
    }
}

private fun Long.toPersianDate(): String = Instant.fromEpochMilliseconds(this)
    .toPersianDateTime(TimeZone.currentSystemDefault())
    .toDateString()
