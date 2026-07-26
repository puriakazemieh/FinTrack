package com.kazemieh.fixed_expense.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.persiandatetime.extensions.toDateString
import com.kazemieh.common.persiandatetime.extensions.toPersianDateTime
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenDark
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import androidx.compose.material3.TextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.kazemieh.designsystem.component.glassTextFieldColors
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.LargeAmountCard
import com.kazemieh.designsystem.component.glass.RemovableChip
import com.kazemieh.designsystem.component.glass.AddChip
import com.kazemieh.designsystem.component.glass.SectionContainer
import com.kazemieh.designsystem.component.glass.Switch
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.person.ui.list.PersonPickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.action_clear
import fintrack.core.designsystem.generated.resources.action_register_transaction
import fintrack.core.designsystem.generated.resources.btn_add_person
import fintrack.core.designsystem.generated.resources.btn_add_tag
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.error_category_required_for_auto_post
import fintrack.core.designsystem.generated.resources.frequency_daily
import fintrack.core.designsystem.generated.resources.frequency_monthly
import fintrack.core.designsystem.generated.resources.frequency_weekly
import fintrack.core.designsystem.generated.resources.frequency_yearly
import fintrack.core.designsystem.generated.resources.guide_fixed_expense_auto_post
import fintrack.core.designsystem.generated.resources.hint_transaction_description
import fintrack.core.designsystem.generated.resources.ic_1
import fintrack.core.designsystem.generated.resources.installment_frequency
import fintrack.core.designsystem.generated.resources.label_auto_post_enabled
import fintrack.core.designsystem.generated.resources.label_char_count_limit
import fintrack.core.designsystem.generated.resources.label_end_date
import fintrack.core.designsystem.generated.resources.label_most_used
import fintrack.core.designsystem.generated.resources.label_no_end_date
import fintrack.core.designsystem.generated.resources.label_note
import fintrack.core.designsystem.generated.resources.label_related_persons
import fintrack.core.designsystem.generated.resources.label_tag_prefix
import fintrack.core.designsystem.generated.resources.label_title
import fintrack.core.designsystem.generated.resources.persons
import fintrack.core.designsystem.generated.resources.save_
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.select_source
import fintrack.core.designsystem.generated.resources.source
import fintrack.core.designsystem.generated.resources.start
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.title_add_fixed_expense
import fintrack.core.designsystem.generated.resources.title_person_management
import fintrack.core.designsystem.generated.resources.title_tag_management
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.getString
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            viewModel.onIntent(AddFixedExpenseIntent.LoadExpense(expenseId))
        } else {
            viewModel.onIntent(AddFixedExpenseIntent.Reset)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AddFixedExpenseEffect.Saved -> onDismiss()
                is AddFixedExpenseEffect.Error -> {
                    snackbarHostState.showSnackbar(getString(effect.messageRes))
                }
            }
        }
    }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showPersonPicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box {
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
                    item(key = "title") {
                        Field(
                            label = stringResource(Res.string.label_title),
                            required = true
                        ) {
                            TextField(
                                value = state.title,
                                onValueChange = { viewModel.onIntent(AddFixedExpenseIntent.SetTitle(it)) },
                                placeholder = {
                                    FintrackBodyMediumText(
                                        text = stringResource(Res.string.label_title),
                                        color = LocalGlassColors.current.text3
                                    )
                                },
                                colors = glassTextFieldColors(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = LocalGlassColors.current.text,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

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

                    // Tags
                    item {
                        val colors = FinTrackPickerColors.rainbow()
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
                                    onRemove = {
                                        viewModel.onIntent(AddFixedExpenseIntent.SetTags(state.tags - tag))
                                    }
                                )
                            }
                        }
                        MostUsedTagChips(
                            items = state.mostUsedTags,
                            selectedItems = state.tags,
                            onItemClick = { tag ->
                                val newSet = state.tags.toMutableSet()
                                if (newSet.contains(tag)) newSet.remove(tag) else newSet.add(tag)
                                viewModel.onIntent(AddFixedExpenseIntent.SetTags(newSet))
                            }
                        )
                    }

                    // Persons
                    item {
                        SectionContainer(
                            title = stringResource(Res.string.label_related_persons),
                            sub = stringResource(Res.string.title_person_management),
                            onAddClick = { showPersonPicker = true },
                            addLabel = stringResource(Res.string.btn_add_person)
                        ) {
                            state.persons.forEach { person ->
                                RemovableChip(
                                    label = person.name,
                                    color = GlassGreen,
                                    onRemove = {
                                        viewModel.onIntent(AddFixedExpenseIntent.SetPersons(state.persons - person))
                                    },
                                    icon = {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(GlassGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            FintrackLabelSmallText(
                                                text = person.name.take(1),
                                                fontWeight = FontWeight.Bold,
                                                color = GlassGreenDark
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        MostUsedPersonChips(
                            items = state.mostUsedPersons,
                            selectedItems = state.persons,
                            onItemClick = { person ->
                                val newSet = state.persons.toMutableSet()
                                if (newSet.contains(person)) newSet.remove(person) else newSet.add(person)
                                viewModel.onIntent(AddFixedExpenseIntent.SetPersons(newSet))
                            }
                        )
                    }

                    // Recurrence
                    item {
                        RecurrenceSelector(
                            selected = state.recurrence,
                            onSelect = { type ->
                                viewModel.onIntent(AddFixedExpenseIntent.SetRecurrence(type))
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

                    // End date (optional bound on the recurrence)
                    item {
                        Field(
                            label = stringResource(Res.string.label_end_date),
                            onClick = { showEndDatePicker = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PickerValue(
                                    label = state.endDate?.toPersianDate()
                                        ?: stringResource(Res.string.label_no_end_date),
                                    icon = Icons.Default.DateRange
                                )
                                if (state.endDate != null) {
                                    FintrackLabelSmallText(
                                        text = stringResource(Res.string.action_clear),
                                        color = GlassGreen,
                                        modifier = Modifier.clickable {
                                            viewModel.onIntent(AddFixedExpenseIntent.SetEndDate(null))
                                        }
                                    )
                                }
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
                                Column(modifier = Modifier.weight(1f)) {
                                    FintrackBodyMediumText(
                                        text = stringResource(Res.string.label_auto_post_enabled),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (state.isAutoPostEnabled && state.category == null) {
                                        FintrackLabelSmallText(
                                            text = stringResource(Res.string.error_category_required_for_auto_post),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Switch(
                                    on = state.isAutoPostEnabled,
                                    onToggle = { viewModel.onIntent(AddFixedExpenseIntent.SetAutoPost(it)) }
                                )
                            }
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.guide_fixed_expense_auto_post),
                                color = LocalGlassColors.current.text3,
                                modifier = Modifier.padding(top = 8.dp)
                            )
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

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
            )
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

    if (showTagPicker) {
        TagPickerBottomSheet(
            selectedTags = state.tags,
            onSubmitClick = {
                viewModel.onIntent(AddFixedExpenseIntent.SetTags(it ?: emptySet()))
                showTagPicker = false
            },
            onDismiss = { showTagPicker = false }
        )
    }

    if (showPersonPicker) {
        PersonPickerBottomSheet(
            selectedPersons = state.persons,
            onSubmitClick = {
                viewModel.onIntent(AddFixedExpenseIntent.SetPersons(it ?: emptySet()))
                showPersonPicker = false
            },
            onDismiss = { showPersonPicker = false }
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

    if (showEndDatePicker) {
        val open = remember { mutableStateOf(true) }
        LaunchedEffect(open.value) { if (!open.value) showEndDatePicker = false }
        JalaliDatePickerBottomSheet(
            openSheet = open,
            initialDate = JalaliCalendar.fromTimestamp(state.endDate ?: state.startDate),
            disableBeforeDate = JalaliCalendar.fromTimestamp(state.startDate),
            onConfirm = {
                viewModel.onIntent(AddFixedExpenseIntent.SetEndDate(it.toTimestamp()))
                showEndDatePicker = false
            }
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
private fun MostUsedTagChips(
    items: List<Tag>,
    selectedItems: Set<Tag>,
    onItemClick: (Tag) -> Unit
) {
    if (items.isEmpty()) return
    MostUsedRow {
        items.take(6).forEach { tag ->
            val active = selectedItems.any { it.id == tag.id }
            Chip(
                color = GlassGreen,
                active = active,
                onClick = { onItemClick(tag) }
            ) {
                FintrackLabelSmallText(
                    text = "#${tag.name}" + if (active) " ✓" else "",
                    color = if (active) LocalGlassColors.current.text3 else GlassGreen,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedPersonChips(
    items: List<Person>,
    selectedItems: Set<Person>,
    onItemClick: (Person) -> Unit
) {
    if (items.isEmpty()) return
    MostUsedRow {
        items.take(6).forEach { person ->
            val active = selectedItems.any { it.id == person.id }
            Chip(
                color = GlassBlue,
                active = active,
                onClick = { onItemClick(person) }
            ) {
                FintrackLabelSmallText(
                    text = person.name + if (active) " ✓" else "",
                    color = if (active) LocalGlassColors.current.text3 else GlassBlue,
                    fontSize = 10.sp
                )
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
