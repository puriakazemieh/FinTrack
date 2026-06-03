package com.kazemieh.transaction.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassBlueSoft
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassEdgeStrong
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenSoft
import com.kazemieh.designsystem.GlassGreenDark
import com.kazemieh.designsystem.GlassRedSoft
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.jalali.JalaliDatePickerBottomSheet
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.person.ui.list.PersonPickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.btn_add_person
import fintrack.core.designsystem.generated.resources.btn_add_tag
import fintrack.core.designsystem.generated.resources.btn_save_transaction
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.date
import fintrack.core.designsystem.generated.resources.dp_today
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.hint_transaction_description
import fintrack.core.designsystem.generated.resources.ic_1
import fintrack.core.designsystem.generated.resources.label_attachment_fa
import fintrack.core.designsystem.generated.resources.label_camera_fa
import fintrack.core.designsystem.generated.resources.label_char_count_limit
import fintrack.core.designsystem.generated.resources.label_gallery_fa
import fintrack.core.designsystem.generated.resources.label_note
import fintrack.core.designsystem.generated.resources.label_optional_fa
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
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    transactionAdded: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(transactionWithRelations, initialType) {
        viewModel.onIntent(AddTransactionIntent.FetchDefaultData(transactionWithRelations))
        initialType?.let { viewModel.onIntent(AddTransactionIntent.SelectedType(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddTransactionEffect.AddedTransaction -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            transactionAdded()
                            onDismiss()
                        }
                    }
                }

                AddTransactionEffect.OnDismiss -> onDismiss()
            }
        }
    }

    BottomSheetContent(state, viewModel::onIntent, sheetState, snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    state: AddTransactionState,
    onIntent: (intent: AddTransactionIntent) -> Unit,
    sheetState: SheetState,
    snackbarHostState: SnackbarHostState,
) {
    val backgroundBrush = remember(state.transactionType) {
        val topColor: Color = when (state.transactionType) {
            TransactionType.EXPENSE -> GlassRedSoft
            TransactionType.INCOME -> GlassGreenSoft
            TransactionType.TRANSFER -> GlassBlueSoft
            else -> GlassBg1
        }
        Brush.verticalGradient(listOf(topColor, GlassBg1, GlassBg0))
    }

    ModalBottomSheet(
        onDismissRequest = { onIntent(AddTransactionIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        AddFrame(
            title = if (state.oldTransaction == null) stringResource(Res.string.title_new_transaction) else stringResource(
                Res.string.edit
            ) + " " + stringResource(Res.string.transaction),
            sub = stringResource(Res.string.title_transaction_management),
            primaryLabel = stringResource(Res.string.btn_save_transaction),
            onPrimaryClick = { onIntent(AddTransactionIntent.Submit) },
            onClose = { onIntent(AddTransactionIntent.OnDismiss) },
            showHero = false,
            backgroundBrush = backgroundBrush
        ) {
            AddTransactionContent(state = state, onIntent = onIntent)
        }
    }

    when (state.topSheet) {
        AddTransactionSheet.SourcePicker -> {
            SourcePickerBottomSheet(
                snackbarHostState = snackbarHostState,
                onSourceClick = { onIntent(AddTransactionIntent.SetSource(it)) },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.SourceEndPicker -> {
            SourcePickerBottomSheet(
                snackbarHostState = snackbarHostState,
                onSourceClick = { onIntent(AddTransactionIntent.SetSourceEnd(it)) },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.CategoryPicker -> {
            CategoryPickerBottomSheet(
                transactionType = state.transactionType,
                snackbarHostState = snackbarHostState,
                onCategoryClick = { onIntent(AddTransactionIntent.SetCategory(it)) },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.TagPicker -> {
            TagPickerBottomSheet(
                snackbarHostState = snackbarHostState,
                selectedTags = state.tags,
                onSubmitClick = { onIntent(AddTransactionIntent.SetTags(it)) },
                onDismiss = { onIntent(AddTransactionIntent.PopSheet) }
            )
        }

        AddTransactionSheet.PersonPicker -> {
            PersonPickerBottomSheet(
                snackbarHostState = snackbarHostState,
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
                            date = "${jalaliCalendar.day.toFa()} / ${jalaliCalendar.monthString} / ${jalaliCalendar.year.toFa()}",
                            timeStamp = jalaliCalendar.toTimestamp()
                        )
                    )
                }
            )
        }

        null -> Unit
    }
}

@Composable
fun AddTransactionContent(
    state: AddTransactionState,
    onIntent: (AddTransactionIntent) -> Unit
) {
    val space = LocalSpacing.current
    val colors = FinTrackPickerColors.rainbow()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            GlassSegmentedSelector(
                selectedType = state.transactionType,
                onTypeSelected = { onIntent(AddTransactionIntent.SelectedType(it)) }
            )
        }

        item {
            LargeAmountCard(
                amount = state.amount,
                onAmountChange = { onIntent(AddTransactionIntent.SetAmount(it)) },
                onCalcClick = { /* Part B */ }
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
                        color = GlassBlue
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
                        color = GlassBlue
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
                    val color =
                        colors.firstOrNull { it.id == state.category?.colorId }?.color ?: GlassGreen
                    PickerValue(
                        label = state.category?.name ?: stringResource(Res.string.select_category),
                        color = color
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
                        color = GlassBlue
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
                    color = GlassText2
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
        }

        item {
            GlassCard(padding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ComposeRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.label_note),
                            color = GlassText3
                        )
                        FintrackLabelSmallText(
                            text = stringResource(
                                Res.string.label_char_count_limit,
                                state.description.length.toLong().toFa(),
                                250.toLong().toFa()
                            ), color = GlassText3
                        )
                    }
                    BasicTextField(
                        value = state.description,
                        onValueChange = { desc ->
                            if (desc.length <= 250) onIntent(
                                AddTransactionIntent.SetDescription(desc)
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = GlassText),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        cursorBrush = Brush.verticalGradient(listOf(GlassGreen, GlassGreen)),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = @Composable { innerTextField ->
                            Box {
                                if (state.description.isEmpty()) {
                                    FintrackBodyMediumText(
                                        text = stringResource(Res.string.hint_transaction_description),
                                        color = GlassText3
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
            PhotoDropUI()
        }
    }
}

@Composable
private fun PickerValue(
    label: String,
    color: Color
) {
    ComposeRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackBodyMediumText(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = GlassText
        )
        Icon(
            painter = painterResource(Res.drawable.ic_1), // Placeholder arrow? 
            contentDescription = null,
            tint = GlassText3,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun PhotoDropUI() {
    GlassCard(padding = 14.dp) {
        Column {
            ComposeRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.label_attachment_fa),
                    color = GlassText3
                )
                FintrackLabelSmallText(
                    text = stringResource(Res.string.label_optional_fa),
                    color = GlassText3,
                    fontSize = 9.sp
                )
            }
            ComposeRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PhotoActionCard(
                    icon = Icons.Default.CameraAlt,
                    label = stringResource(Res.string.label_camera_fa)
                )
                PhotoActionCard(
                    icon = Icons.Default.Image,
                    label = stringResource(Res.string.label_gallery_fa)
                )
            }
        }
    }
}

@Composable
private fun PhotoActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GlassColor)
            .border(1.5.dp, GlassEdgeStrong, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GlassText3,
                modifier = Modifier.size(18.dp)
            )
            FintrackLabelSmallText(text = label, color = GlassText3)
        }
    }
}
