package com.kazemieh.transaction.ui.add

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.SmsDraft
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassBlueSoft
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenDark
import com.kazemieh.designsystem.GlassGreenSoft
import com.kazemieh.designsystem.GlassRedSoft
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.PhotoDrop
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.designsystem.picker.PickableColor
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.person.ui.list.PersonPickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.btn_add_person
import fintrack.core.designsystem.generated.resources.btn_add_tag
import fintrack.core.designsystem.generated.resources.btn_edit_transaction
import fintrack.core.designsystem.generated.resources.btn_save_transaction
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.date
import fintrack.core.designsystem.generated.resources.delete
import fintrack.core.designsystem.generated.resources.dp_today
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.hint_transaction_description
import fintrack.core.designsystem.generated.resources.ic_1
import fintrack.core.designsystem.generated.resources.ic_cat_transfer
import fintrack.core.designsystem.generated.resources.label_char_count_limit
import fintrack.core.designsystem.generated.resources.label_most_used
import fintrack.core.designsystem.generated.resources.label_note
import fintrack.core.designsystem.generated.resources.label_related_persons
import fintrack.core.designsystem.generated.resources.label_tag_prefix
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.select_source
import fintrack.core.designsystem.generated.resources.source
import fintrack.core.designsystem.generated.resources.source_from
import fintrack.core.designsystem.generated.resources.source_to
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.title_new_transaction
import fintrack.core.designsystem.generated.resources.title_person_management
import fintrack.core.designsystem.generated.resources.title_tag_management
import fintrack.core.designsystem.generated.resources.title_transaction_management
import fintrack.core.designsystem.generated.resources.transaction
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.layout.Row as ComposeRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    viewModel: AddTransactionViewModel = koinViewModel(),
    transactionWithRelations: TransactionWithRelations? = null,
    initialType: TransactionType? = null,
    smsDraft: SmsDraft? = null,
    onDismiss: () -> Unit,
    transactionAdded: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    LaunchedEffect(transactionWithRelations, initialType, smsDraft) {
        viewModel.onIntent(
            AddTransactionIntent.FetchDefaultData(
                transactionWithRelations,
                smsDraft
            )
        )
        initialType?.let { viewModel.onIntent(AddTransactionIntent.SelectedType(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddTransactionEffect.AddedTransaction -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            transactionAdded()
                        }
                    }
                }

                AddTransactionEffect.OnDismiss -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onDismiss()
                }
            }
        }
    }

    BottomSheetContent(state, viewModel::onIntent, sheetState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    state: AddTransactionState,
    onIntent: (intent: AddTransactionIntent) -> Unit,
    sheetState: SheetState,
) {

    val glassColors = LocalGlassColors.current

    val backgroundBrush = remember(state.transactionType, glassColors) {
        val topColor: Color = when (state.transactionType) {
            TransactionType.EXPENSE -> GlassRedSoft
            TransactionType.INCOME -> GlassGreenSoft
            TransactionType.TRANSFER -> GlassBlueSoft
            else -> glassColors.bg1
        }
        Brush.verticalGradient(listOf(topColor, glassColors.bg1, glassColors.bg0))
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            focusManager.clearFocus()
            keyboardController?.hide()
            onIntent(AddTransactionIntent.OnDismiss)
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddFrame(
            title = if (state.oldTransaction == null) stringResource(Res.string.title_new_transaction) else stringResource(
                Res.string.edit
            ) + " " + stringResource(Res.string.transaction),
            sub = stringResource(Res.string.title_transaction_management),
            primaryLabel = if (state.oldTransaction == null) stringResource(Res.string.btn_save_transaction) else stringResource(
                Res.string.btn_edit_transaction
            ),
            onPrimaryClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onIntent(AddTransactionIntent.Submit)
            },
            secondaryLabel = if (state.oldTransaction != null) stringResource(Res.string.delete) else null,
            onSecondaryClick = {
                onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.DeleteConfirmation))
            },
            onClose = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onIntent(AddTransactionIntent.OnDismiss)
            },
            showHero = false,
            backgroundBrush = backgroundBrush
        ) {
            AddTransactionContent(
                state = state,
                onIntent = onIntent,
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onIntent(AddTransactionIntent.Submit)
                })
            )
        }
    }

    when (state.topSheet) {
        AddTransactionSheet.SourcePicker -> {
            SourcePickerBottomSheet(
                onSourceClick = { onIntent(AddTransactionIntent.SetSource(it)) },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.SourceEndPicker -> {
            SourcePickerBottomSheet(
                onSourceClick = { onIntent(AddTransactionIntent.SetSourceEnd(it)) },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.CategoryPicker -> {
            CategoryPickerBottomSheet(
                transactionType = state.transactionType,
                onCategoryClick = {
                    onIntent(AddTransactionIntent.SetCategory(it))
                    onIntent(AddTransactionIntent.PopSheet)
                },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.TagPicker -> {
            TagPickerBottomSheet(
                selectedTags = state.tags,
                onSubmitClick = { onIntent(AddTransactionIntent.SetTags(it)) },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.PersonPicker -> {
            PersonPickerBottomSheet(
                selectedPersons = state.persons,
                onSubmitClick = { onIntent(AddTransactionIntent.SetPerson(it)) },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.DatePicker -> {
            val openSheet = remember { mutableStateOf(true) }
            LaunchedEffect(openSheet.value) {
                if (!openSheet.value) {
                    onIntent(AddTransactionIntent.PopSheet)
                }
            }
            JalaliDatePickerBottomSheet(
                openSheet = openSheet,
                onConfirm = { jalaliCalendar ->
                    onIntent(
                        AddTransactionIntent.SetDate(
                            date = "${jalaliCalendar.day.toPersianDigits()} / ${jalaliCalendar.monthString} / ${jalaliCalendar.year.toPersianDigits()}",
                            timeStamp = jalaliCalendar.toTimestamp()
                        )
                    )
                }
            )
        }

        AddTransactionSheet.DeleteConfirmation -> {
            DeleteBottomSheet(
                itemName = state.description.ifEmpty { state.category?.name },
                dismissClicked = { onIntent(AddTransactionIntent.PopSheet) },
                confirmClicked = {
                    onIntent(AddTransactionIntent.Delete)
                    onIntent(AddTransactionIntent.PopSheet)
                },
                isLoading = state.isLoading
            )
        }

        AddTransactionSheet.Calculator -> {
            CalculatorBottomSheet(
                initialAmount = state.amount,
                onConfirm = {
                    onIntent(AddTransactionIntent.SetAmount(it))
                    onIntent(AddTransactionIntent.PopSheet)
                },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        null -> Unit
    }
}

@Composable
fun AddTransactionContent(
    state: AddTransactionState,
    onIntent: (AddTransactionIntent) -> Unit,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val colors = FinTrackPickerColors.rainbow()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        state.smsDraft?.let { draft ->
            item {
                SmsReferenceBanner(
                    draft = draft,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        item {
            GlassSegmentedSelector(
                selectedType = state.transactionType,
                onTypeSelected = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onIntent(AddTransactionIntent.SelectedType(it))
                }
            )
        }

        item(key = "amount") {
            LargeAmountCard(
                amount = state.amount,
                onAmountChange = { onIntent(AddTransactionIntent.SetAmount(it)) },
                onCalcClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.Calculator)) },
                autoFocus = state.oldTransaction == null,
                isError = state.isAmountError,
                enabled = !state.isLoading,
                keyboardActions = keyboardActions
            )
        }

        if (state.transactionType == TransactionType.TRANSFER) {
            item {
                Field(
                    label = stringResource(Res.string.source_from),
                    required = true,
                    error = state.isSourceError,
                    onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.SourcePicker)) }
                ) {
                    PickerValue(
                        label = state.source?.name ?: stringResource(Res.string.select_source),
                        icon = FinTrackIcons.findIcon(state.source?.iconId).resource
                    )
                }
                if (state.mostUsedSources.isNotEmpty()) {
                    MostUsedSourceChips(
                        items = state.mostUsedSources,
                        onItemClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onIntent(AddTransactionIntent.SetSource(it))
                        }
                    )
                }
            }
            item {
                Field(
                    label = stringResource(Res.string.source_to),
                    required = true,
                    error = state.isSourceEndError,
                    onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.SourceEndPicker)) }
                ) {
                    PickerValue(
                        label = state.sourceEnd?.name ?: stringResource(Res.string.select_source),
                        icon = FinTrackIcons.findIcon(state.sourceEnd?.iconId).resource
                    )
                }
                if (state.mostUsedSources.isNotEmpty()) {
                    MostUsedSourceChips(
                        items = state.mostUsedSources,
                        onItemClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onIntent(AddTransactionIntent.SetSourceEnd(it))
                        }
                    )
                }
            }
        } else {
            item {
                Field(
                    label = stringResource(Res.string.category),
                    required = true,
                    error = state.isCategoryError,
                    onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.CategoryPicker)) }
                ) {
                    PickerValue(
                        label = state.category?.name ?: stringResource(Res.string.select_category),
                        icon = if (state.transactionType == TransactionType.TRANSFER) Res.drawable.ic_cat_transfer else FinTrackIcons.findIcon(
                            state.category?.iconId
                        ).resource
                    )
                }
                if (state.mostUsedCategories.isNotEmpty()) {
                    MostUsedCategoryChips(
                        items = state.mostUsedCategories,
                        colors = colors,
                        onItemClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onIntent(AddTransactionIntent.SetCategory(it))
                        }
                    )
                }
            }
            item {
                Field(
                    label = stringResource(Res.string.source),
                    required = true,
                    error = state.isSourceError,
                    onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.SourcePicker)) }
                ) {
                    PickerValue(
                        label = state.source?.name ?: stringResource(Res.string.select_source),
                        icon = FinTrackIcons.findIcon(state.source?.iconId).resource
                    )
                }
                if (state.mostUsedSources.isNotEmpty()) {
                    MostUsedSourceChips(
                        items = state.mostUsedSources,
                        onItemClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onIntent(AddTransactionIntent.SetSource(it))
                        }
                    )
                }
            }
        }

        item {
            Field(
                label = stringResource(Res.string.date),
                required = true,
                onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.DatePicker)) }
            ) {
                PickerValue(
                    label = state.date ?: stringResource(Res.string.dp_today),
                    icon = Icons.Default.CalendarToday
                )
            }
        }

        item {
            SectionContainer(
                title = stringResource(Res.string.label_related_persons),
                sub = stringResource(Res.string.title_person_management),
                onAddClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.PersonPicker)) },
                addLabel = stringResource(Res.string.btn_add_person)
            ) {
                state.persons?.forEach { person ->
                    RemovableChip(
                        label = person.name,
                        color = GlassGreen,
                        onRemove = {
                            val newSet = state.persons.filter { it.id != person.id }.toSet()
                            onIntent(AddTransactionIntent.SetPerson(newSet))
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
            if (state.mostUsedPersons.isNotEmpty()) {
                MostUsedPersonChips(
                    items = state.mostUsedPersons,
                    selectedItems = state.persons ?: emptySet(),
                    onItemClick = { person ->
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        val newSet = state.persons?.toMutableSet() ?: mutableSetOf()
                        if (newSet.contains(person)) newSet.remove(person) else newSet.add(person)
                        onIntent(AddTransactionIntent.SetPerson(newSet))
                    }
                )
            }
        }

        item {
            SectionContainer(
                title = stringResource(Res.string.tags),
                sub = stringResource(Res.string.title_tag_management),
                onAddClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.TagPicker)) },
                addLabel = stringResource(Res.string.btn_add_tag)
            ) {
                state.tags?.forEach { tag ->
                    val color = colors.firstOrNull { it.id == tag.colorId }?.color ?: GlassBlue
                    RemovableChip(
                        label = stringResource(Res.string.label_tag_prefix, tag.name),
                        color = color,
                        onRemove = {
                            val newSet = state.tags.filter { it.id != tag.id }.toSet()
                            onIntent(AddTransactionIntent.SetTags(newSet))
                        }
                    )
                }
            }
            if (state.mostUsedTags.isNotEmpty()) {
                MostUsedTagChips(
                    items = state.mostUsedTags,
                    selectedItems = state.tags ?: emptySet(),
                    colors = colors,
                    onItemClick = { tag ->
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        val newSet = state.tags?.toMutableSet() ?: mutableSetOf()
                        if (newSet.contains(tag)) newSet.remove(tag) else newSet.add(tag)
                        onIntent(AddTransactionIntent.SetTags(newSet))
                    }
                )
            }
        }

        item {
            GlassCard(padding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ComposeRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.label_note)
                        )
                        FintrackLabelSmallText(
                            text = stringResource(
                                Res.string.label_char_count_limit,
                                state.description.length.toLong().toPersianDigits(),
                                250.toLong().toPersianDigits()
                            )
                        )
                    }
                    BasicTextField(
                        value = state.description,
                        onValueChange = { desc ->
                            if (desc.length <= 250) onIntent(
                                AddTransactionIntent.SetDescription(desc)
                            )
                        },
                        enabled = !state.isLoading,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = LocalGlassColors.current.text),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        cursorBrush = Brush.verticalGradient(listOf(GlassGreen, GlassGreen)),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = @Composable { innerTextField ->
                            Box {
                                if (state.description.isEmpty()) {
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

        item {
            val photoBitmap = remember(state.photoBytes) {
                state.photoBytes?.decodeToImageBitmap()
            }
            PhotoDrop(
                photoBitmap = photoBitmap,
                onImagePicked = { onIntent(AddTransactionIntent.SetPhoto(it)) },
                onRemove = { onIntent(AddTransactionIntent.SetPhoto(null)) }
            )
        }
    }
}

@Composable
private fun PickerValue(
    label: String,
    icon: Any? = null
) {
    ComposeRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackBodyMediumText(
            text = label,
            fontWeight = FontWeight.SemiBold
        )
        when (icon) {
            is org.jetbrains.compose.resources.DrawableResource -> {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            is ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            else -> {
                Icon(
                    painter = painterResource(Res.drawable.ic_1), // Placeholder arrow? 
                    contentDescription = null,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedCategoryChips(
    items: List<Category>,
    colors: List<PickableColor>,
    onItemClick: (Category) -> Unit
) {
    if (items.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(
            text = stringResource(Res.string.label_most_used),
            fontSize = 9.sp
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.take(3).forEach { category ->
                val color = colors.firstOrNull { it.id == category.colorId }?.color ?: GlassGreen
                Chip(
                    color = color,
                    onClick = { onItemClick(category) }
                ) {
                    FintrackLabelSmallText(
                        text = category.name,
                        color = color,
                        fontSize = 10.sp
                    )
                }
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(
            text = stringResource(Res.string.label_most_used),
            fontSize = 9.sp
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.take(3).forEach { source ->
                Chip(
                    color = GlassBlue,
                    onClick = { onItemClick(source) }
                ) {
                    FintrackLabelSmallText(
                        text = source.name,
                        color = GlassBlue,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedTagChips(
    items: List<Tag>,
    selectedItems: Set<Tag>,
    colors: List<PickableColor>,
    onItemClick: (Tag) -> Unit
) {
    if (items.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(
            text = stringResource(Res.string.label_most_used),
            fontSize = 9.sp
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.take(3).forEach { tag ->
                val active = selectedItems.contains(tag)
                val color = colors.firstOrNull { it.id == tag.colorId }?.color ?: GlassBlue
                Chip(
                    color = color,
                    active = active,
                    onClick = { onItemClick(tag) }
                ) {
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.label_tag_prefix, tag.name),
                        color = if (active) Color.White else color,
                        fontSize = 10.sp
                    )
                }
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackLabelSmallText(
            text = stringResource(Res.string.label_most_used),
            fontSize = 9.sp
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.take(3).forEach { person ->
                val active = selectedItems.contains(person)
                Chip(
                    color = GlassGreen,
                    active = active,
                    onClick = { onItemClick(person) }
                ) {
                    FintrackLabelSmallText(
                        text = person.name,
                        color = if (active) Color.White else GlassGreen,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
