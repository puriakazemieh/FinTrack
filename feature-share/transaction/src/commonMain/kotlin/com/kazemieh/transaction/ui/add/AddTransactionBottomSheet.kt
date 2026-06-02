package com.kazemieh.transaction.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.person.ui.list.PersonPickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.*
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

                is AddTransactionEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message.resolveString(),
                            duration = SnackbarDuration.Short
                        )
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
    ModalBottomSheet(
        onDismissRequest = { onIntent(AddTransactionIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {

            AddTransactionContent(state = state, onIntent = onIntent)


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

                null -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionContent(
    state: AddTransactionState,
    onIntent: (AddTransactionIntent) -> Unit
) {
    val space = LocalSpacing.current
    val colors = FinTrackPickerColors.rainbow()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                ScreenHeader(
                    title = "ثبت تراکنش جدید",
                    onBack = { onIntent(AddTransactionIntent.OnDismiss) }
                )
            }

            item {
                GlassSegmentedSelector(
                    selectedType = state.transactionType,
                    onTypeSelected = { onIntent(AddTransactionIntent.SelectedType(it)) }
                )
            }

            item {
                LargeAmountCard(
                    amount = state.amount,
                    onCalcClick = { /* Part B */ }
                )
            }

            if (state.transactionType == TransactionType.TRANSFER) {
                item {
                    Field(label = stringResource(Res.string.source_from), required = true, error = state.isSourceError) {
                        PickerValue(
                            label = state.source?.name ?: stringResource(Res.string.select_source),
                            onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.SourcePicker)) },
                            color = GlassBlue
                        )
                    }
                }
                item {
                    Field(label = stringResource(Res.string.source_to), required = true, error = state.isSourceEndError) {
                        PickerValue(
                            label = state.sourceEnd?.name ?: stringResource(Res.string.select_source),
                            onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.SourceEndPicker)) },
                            color = GlassBlue
                        )
                    }
                }
            } else {
                item {
                    Field(label = stringResource(Res.string.category), required = true, error = state.isCategoryError) {
                        val color = colors.firstOrNull { it.id == state.category?.colorId }?.color ?: GlassGreen
                        PickerValue(
                            label = state.category?.name ?: stringResource(Res.string.select_category),
                            onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.CategoryPicker)) },
                            color = color
                        )
                    }
                }
                item {
                    Field(label = stringResource(Res.string.source), required = true, error = state.isSourceError) {
                        PickerValue(
                            label = state.source?.name ?: stringResource(Res.string.select_source),
                            onClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.SourcePicker)) },
                            color = GlassBlue
                        )
                    }
                }
            }

            item {
                Field(label = stringResource(Res.string.date), required = true) {
                    PickerValue(
                        label = state.date ?: "امروز",
                        onClick = { /* Handle date picker toggle or direct integration */ },
                        color = GlassText2
                    )
                }
            }

            item {
                SectionContainer(
                    title = "افراد مرتبط",
                    sub = "برای تقسیم خرج یا طلب",
                    onAddClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.PersonPicker)) },
                    addLabel = "+ افزودن شخص"
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
                                    Text(text = person.name.take(1), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GlassGreenDark)
                                }
                            }
                        )
                    }
                }
            }

            item {
                SectionContainer(
                    title = "تگ‌ها",
                    sub = "برچسب برای فیلتر",
                    onAddClick = { onIntent(AddTransactionIntent.ToggleSheet(AddTransactionSheet.TagPicker)) },
                    addLabel = "+ افزودن تگ"
                ) {
                    state.tags?.forEach { tag ->
                        val color = colors.firstOrNull { it.id == tag.colorId }?.color ?: GlassBlue
                        RemovableChip(
                            label = "#${tag.name}",
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
                        ComposeRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "یادداشت", style = MaterialTheme.typography.labelSmall, color = GlassText3)
                            Text(text = "${state.description.length} / ۲۵۰", style = MaterialTheme.typography.labelSmall, color = GlassText3)
                        }
                        BasicTextField(
                            value = state.description,
                            onValueChange = { desc -> if (desc.length <= 250) onIntent(AddTransactionIntent.SetDescription(desc)) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = GlassText),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = @Composable { innerTextField ->
                                Box {
                                    if (state.description.isEmpty()) {
                                        Text(text = "توضیحات تراکنش...", style = MaterialTheme.typography.bodyMedium, color = GlassText3)
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

        // Fixed CTA at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, GlassBg0.copy(alpha = 0.8f), GlassBg0)))
                .padding(14.dp)
        ) {
            Button(
                onClick = { onIntent(AddTransactionIntent.Submit) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlassGreen,
                    contentColor = GlassGreenDark
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "ذخیره تراکنش",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PickerValue(
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    ComposeRow(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
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
                Text(text = "پیوست عکس", style = MaterialTheme.typography.labelSmall, color = GlassText3)
                Text(text = "اختیاری", style = MaterialTheme.typography.labelSmall, color = GlassText3)
            }
            ComposeRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PhotoActionCard(icon = Icons.Default.CameraAlt, label = "دوربین")
                PhotoActionCard(icon = Icons.Default.Image, label = "گالری")
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = GlassText3, modifier = Modifier.size(18.dp))
            Text(text = label, fontSize = 9.sp, color = GlassText3)
        }
    }
}
