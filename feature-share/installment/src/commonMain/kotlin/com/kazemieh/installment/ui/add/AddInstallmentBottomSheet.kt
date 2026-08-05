package com.kazemieh.installment.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.InstallmentFrequency
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenDark
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.component.picker.FintrackTimePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.installment.ui.add.component.LoanCalculatorCard
import com.kazemieh.installment.ui.add.component.PickerValue
import com.kazemieh.installment.ui.add.component.TitledInput
import com.kazemieh.jalali.JalaliCalendar
import com.kazemieh.person.ui.list.PersonPickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddInstallmentBottomSheet(
    viewModel: AddInstallmentViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    installmentId: Long? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glassColors = LocalGlassColors.current

    LaunchedEffect(installmentId) {
        if (installmentId != null) {
            viewModel.onIntent(AddInstallmentIntent.LoadInstallment(installmentId))
        } else {
            viewModel.onIntent(AddInstallmentIntent.Reset)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AddInstallmentEffect.Success -> {
                    onSuccess()
                    onDismiss()
                }
                is AddInstallmentEffect.Error -> {
                }
            }
        }
    }

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
        var showTimePicker by remember { mutableStateOf(false) }
        var showTagPicker by remember { mutableStateOf(false) }
        var showPersonPicker by remember { mutableStateOf(false) }
        var showCalculator by remember { mutableStateOf(false) }

        AddFrame(
            title = if (state.installmentId == null) stringResource(Res.string.add_installment) else stringResource(Res.string.edit),
            sub = stringResource(Res.string.installment_title),
            primaryLabel = stringResource(Res.string.save_),
            onPrimaryClick = {
                val reminderTitle = state.title
                val reminderMessage = state.installmentAmount.toLongOrNull()?.toSignedPersianPrice() ?: "0"
                viewModel.onIntent(AddInstallmentIntent.Submit(reminderTitle, reminderMessage))
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
                    TitledInput(
                        label = stringResource(Res.string.installment_title) + " " + stringResource(Res.string.required_star),
                        value = state.title,
                        onValueChange = { viewModel.onIntent(AddInstallmentIntent.SetTitle(it)) },
                        placeholder = stringResource(Res.string.installment_title),
                        singleLine = true
                    )
                }

                item {
                    LoanCalculatorCard(
                        amount = state.loanAmount,
                        installment = state.loanInstallmentAmount,
                        count = state.loanCount,
                        totalPayment = state.loanTotalPayment,
                        totalInterest = state.loanTotalInterest,
                        onAmountChange = { viewModel.onIntent(AddInstallmentIntent.SetLoanAmount(it)) },
                        onInstallmentChange = { viewModel.onIntent(AddInstallmentIntent.SetLoanInstallmentAmount(it)) },
                        onCountChange = { viewModel.onIntent(AddInstallmentIntent.SetLoanCount(it)) },
                        onApply = { viewModel.onIntent(AddInstallmentIntent.ApplyLoanCalculator) }
                    )
                }

                item {
                    LargeAmountCard(
                        amount = state.installmentAmount,
                        onAmountChange = { viewModel.onIntent(AddInstallmentIntent.SetInstallmentAmount(it)) },
                        label = stringResource(Res.string.installment_amount),
                        autoFocus = state.installmentId == null,
                        onCalcClick = { showCalculator = true }
                    )
                }

                item {
                    Field(label = stringResource(Res.string.total_installments)) {
                        Column {
                            TextField(
                                value = state.totalInstallments,
                                onValueChange = { viewModel.onIntent(AddInstallmentIntent.SetTotalInstallments(it.filter { c -> c.isDigit() })) },
                                colors = glassTextFieldColors(),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = glassColors.text, fontWeight = FontWeight.SemiBold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PersianNumberTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { FintrackBodyMediumText(text = "۰".toPersianDigits(), color = glassColors.text3) }
                            )
                            if (state.showMismatchWarning) {
                                FintrackLabelSmallText(
                                    text = stringResource(Res.string.msg_installment_mismatch),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Field(label = stringResource(Res.string.total_installment_amount)) {
                        TextField(
                            value = state.totalAmount,
                            onValueChange = { viewModel.onIntent(AddInstallmentIntent.SetTotalAmount(it.filter { c -> c.isDigit() })) },
                            colors = glassTextFieldColors(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = glassColors.text, fontWeight = FontWeight.SemiBold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = NumberCommaTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { FintrackBodyMediumText(text = "۰".toPersianDigits(), color = glassColors.text3) }
                        )
                    }
                }

                item {
                    Field(label = stringResource(Res.string.category), required = true, onClick = { showCategoryPicker = true }) {
                        val color = colors.firstOrNull { it.id == state.category?.colorId }?.color ?: GlassGreen
                        PickerValue(label = state.category?.name ?: stringResource(Res.string.select_category), color = color, icon = FinTrackIcons.findIcon(state.category?.iconId).resource)
                    }
                    MostUsedCategoryChips(items = state.mostUsedCategories, onItemClick = { viewModel.onIntent(AddInstallmentIntent.SetCategory(it)) })
                }

                item {
                    Field(label = stringResource(Res.string.source), required = true, onClick = { showSourcePicker = true }) {
                        PickerValue(label = state.source?.name ?: stringResource(Res.string.select_source), color = GlassBlue, icon = FinTrackIcons.findIcon(state.source?.iconId).resource)
                    }
                    MostUsedSourceChips(items = state.mostUsedSources, onItemClick = { viewModel.onIntent(AddInstallmentIntent.SetSource(it)) })
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
                                onRemove = { viewModel.onIntent(AddInstallmentIntent.SetTags(state.tags - tag)) }
                            )
                        }
                    }
                    MostUsedTagChips(items = state.mostUsedTags, selectedItems = state.tags, onItemClick = { tag ->
                        val newSet = state.tags.toMutableSet()
                        if (newSet.contains(tag)) newSet.remove(tag) else newSet.add(tag)
                        viewModel.onIntent(AddInstallmentIntent.SetTags(newSet))
                    })
                }

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
                                onRemove = { viewModel.onIntent(AddInstallmentIntent.SetPersons(state.persons - person)) },
                                icon = {
                                    Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(GlassGreen), contentAlignment = Alignment.Center) {
                                        FintrackLabelSmallText(text = person.name.take(1), fontWeight = FontWeight.Bold, color = GlassGreenDark)
                                    }
                                }
                            )
                        }
                    }
                    MostUsedPersonChips(items = state.mostUsedPersons, selectedItems = state.persons, onItemClick = { person ->
                        val newSet = state.persons.toMutableSet()
                        if (newSet.contains(person)) newSet.remove(person) else newSet.add(person)
                        viewModel.onIntent(AddInstallmentIntent.SetPersons(newSet))
                    })
                }

                item {
                    RecurrenceSelector(selected = state.frequency, onSelect = { viewModel.onIntent(AddInstallmentIntent.SetFrequency(it)) })
                }

                item {
                    Field(label = stringResource(Res.string.date), required = true, onClick = { showDatePicker = true }) {
                        val jalali = JalaliCalendar.fromTimestamp(state.startDate)
                        val dateStr = "${jalali.day.toPersianDigits()} / ${jalali.monthString} / ${jalali.year.toPersianDigits()}"
                        PickerValue(label = dateStr, color = glassColors.text, icon = Icons.Default.CalendarMonth)
                    }
                }

                item {
                    Field(label = stringResource(Res.string.label_time), onClick = { showTimePicker = true }) {
                        PickerValue(label = DateUtils.formatTime(state.startDate).toPersianDigits(), color = glassColors.text, icon = Icons.Default.Schedule)
                    }
                }

                item {
                    GlassCard(padding = 14.dp) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            FintrackBodyLargeText(text = stringResource(Res.string.label_post_as_transaction), fontWeight = FontWeight.SemiBold)
                            Switch(on = state.postAsTransaction, onToggle = { viewModel.onIntent(AddInstallmentIntent.SetPostAsTransaction(it)) })
                        }
                    }
                }

                item {
                    GlassCard(padding = 14.dp) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Notifications, null, tint = GlassGreen, modifier = Modifier.size(20.dp))
                                FintrackBodyLargeText(text = stringResource(Res.string.reminder), fontWeight = FontWeight.SemiBold)
                            }
                            Switch(on = state.reminderEnabled, onToggle = { viewModel.onIntent(AddInstallmentIntent.SetReminderEnabled(it)) })
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
                                onValueChange = { if (it.length <= 250) viewModel.onIntent(AddInstallmentIntent.SetDescription(it)) },
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

        if (showCategoryPicker) {
            CategoryPickerBottomSheet(transactionType = TransactionType.EXPENSE, onCategoryClick = { viewModel.onIntent(AddInstallmentIntent.SetCategory(it)); showCategoryPicker = false }, onDismiss = { showCategoryPicker = false })
        }
        if (showSourcePicker) {
            SourcePickerBottomSheet(onSourceClick = { viewModel.onIntent(AddInstallmentIntent.SetSource(it)); showSourcePicker = false }, onDismiss = { showSourcePicker = false })
        }
        if (showTagPicker) {
            TagPickerBottomSheet(selectedTags = state.tags, onSubmitClick = { viewModel.onIntent(AddInstallmentIntent.SetTags(it ?: emptySet())); showTagPicker = false }, onDismiss = { showTagPicker = false })
        }
        if (showPersonPicker) {
            PersonPickerBottomSheet(selectedPersons = state.persons, onSubmitClick = { viewModel.onIntent(AddInstallmentIntent.SetPersons(it ?: emptySet())); showPersonPicker = false }, onDismiss = { showPersonPicker = false })
        }
        if (showDatePicker) {
            val openSheet = remember { mutableStateOf(true) }
            LaunchedEffect(openSheet.value) { if (!openSheet.value) showDatePicker = false }
            JalaliDatePickerBottomSheet(openSheet = openSheet, initialDate = JalaliCalendar.fromTimestamp(state.startDate), onConfirm = { viewModel.onIntent(AddInstallmentIntent.SetStartDate(it.toTimestamp())); showDatePicker = false })
        }
        if (showTimePicker) {
            val openSheet = remember { mutableStateOf(true) }
            LaunchedEffect(openSheet.value) { if (!openSheet.value) showTimePicker = false }
            FintrackTimePickerBottomSheet(
                openSheet = openSheet,
                initialTime = DateUtils.timeOfDay(state.startDate),
                onConfirm = { time ->
                    val parts = time.split(":")
                    viewModel.onIntent(AddInstallmentIntent.SetReminderTime(parts[0].toInt(), parts[1].toInt()))
                    showTimePicker = false
                }
            )
        }
        if (showCalculator) {
            CalculatorBottomSheet(
                initialAmount = state.installmentAmount,
                onConfirm = {
                    viewModel.onIntent(AddInstallmentIntent.SetInstallmentAmount(it))
                    showCalculator = false
                },
                onDismiss = { showCalculator = false }
            )
        }
    }
}

@Composable
private fun RecurrenceSelector(selected: InstallmentFrequency, onSelect: (InstallmentFrequency) -> Unit) {
    val types = listOf(
        InstallmentFrequency.DAILY to Res.string.frequency_daily,
        InstallmentFrequency.WEEKLY to Res.string.frequency_weekly,
        InstallmentFrequency.MONTHLY to Res.string.frequency_monthly,
        InstallmentFrequency.YEARLY to Res.string.frequency_yearly
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelMediumText(text = stringResource(Res.string.installment_frequency), color = MaterialTheme.colorScheme.onSurfaceVariant)
        GlassCard(padding = 10.dp) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                types.forEach { (type, labelRes) ->
                    Chip(active = selected == type, onClick = { onSelect(type) }, modifier = Modifier.weight(1f)) {
                        FintrackLabelSmallText(text = stringResource(labelRes))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedCategoryChips(items: List<com.kazemieh.common.model.Category>, onItemClick: (com.kazemieh.common.model.Category) -> Unit) {
    if (items.isEmpty()) return
    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.take(3).forEach { category ->
                Chip(color = GlassGreen, onClick = { onItemClick(category) }) {
                    FintrackLabelSmallText(text = category.name, color = GlassGreen, fontSize = 10.sp)
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
                    FintrackLabelSmallText(text = "#${tag.name}", color = if (active) androidx.compose.ui.graphics.Color.White else color, fontSize = 10.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedPersonChips(items: List<com.kazemieh.common.model.Person>, selectedItems: Set<com.kazemieh.common.model.Person>, onItemClick: (com.kazemieh.common.model.Person) -> Unit) {
    if (items.isEmpty()) return
    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelSmallText(text = stringResource(Res.string.label_most_used), fontSize = 9.sp)
        FlowRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.take(3).forEach { person ->
                val active = selectedItems.contains(person)
                Chip(color = GlassGreen, active = active, onClick = { onItemClick(person) }) {
                    FintrackLabelSmallText(text = person.name, color = if (active) androidx.compose.ui.graphics.Color.White else GlassGreen, fontSize = 10.sp)
                }
            }
        }
    }
}
