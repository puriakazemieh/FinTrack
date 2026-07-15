package com.kazemieh.budget.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.budget.ui.component.BudgetPeriodSelector
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetBottomSheet(
    viewModel: AddBudgetViewModel = koinViewModel(),
    budgetWithProgress: BudgetWithProgress? = null,
    defaultStartAt: Long? = null,
    defaultRangeEnd: Long? = null,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(budgetWithProgress, defaultStartAt) {
        viewModel.onIntent(
            AddBudgetIntent.InitialData(
                budgetWithProgress?.budget,
                budgetWithProgress?.category,
                defaultStartAt,
                defaultRangeEnd
            )
        )
        viewModel.onIntent(AddBudgetIntent.LoadCategories())
        viewModel.onIntent(AddBudgetIntent.LoadExtraData)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is AddBudgetEffect.BudgetSaved) {
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
        AddFrame(
            title = if (budgetWithProgress == null) stringResource(Res.string.label_new_budget) else stringResource(Res.string.label_edit_budget),
            sub = stringResource(Res.string.title_my_budgets),
            primaryLabel = stringResource(Res.string.save_category),
            onPrimaryClick = { viewModel.onIntent(AddBudgetIntent.SaveBudget) },
            onClose = onDismiss,
            showHero = false
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(space.medium),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    LargeAmountCard(
                        amount = state.amount,
                        onAmountChange = { viewModel.onIntent(AddBudgetIntent.UpdateAmount(it)) },
                        onCalcClick = { viewModel.onIntent(AddBudgetIntent.ToggleSheet(AddBudgetSheet.Calculator)) },
                        autoFocus = budgetWithProgress == null
                    )
                }

                item {
                    Field(
                        label = stringResource(Res.string.category),
                        required = true,
                        onClick = { viewModel.onIntent(AddBudgetIntent.ToggleSheet(AddBudgetSheet.CategoryPicker)) }
                    ) {
                        PickerValue(
                            label = state.selectedCategory?.name ?: stringResource(Res.string.category_choose),
                            icon = FinTrackIcons.findIcon(state.selectedCategory?.iconId).resource
                        )
                    }
                    MostUsedCategoryChips(
                        items = state.mostUsedCategories,
                        onItemClick = { viewModel.onIntent(AddBudgetIntent.SelectCategory(it)) }
                    )
                }

                item {
                    Field(
                        label = stringResource(Res.string.source),
                        required = false,
                        onClick = { viewModel.onIntent(AddBudgetIntent.ToggleSheet(AddBudgetSheet.SourcePicker)) }
                    ) {
                        PickerValue(
                            label = state.selectedSource?.name ?: stringResource(Res.string.all_source),
                            icon = FinTrackIcons.findIcon(state.selectedSource?.iconId).resource
                        )
                    }
                    MostUsedSourceChips(
                        items = state.mostUsedSources,
                        onItemClick = { viewModel.onIntent(AddBudgetIntent.SelectSource(it)) }
                    )
                }

                item {
                    SectionContainer(
                        title = stringResource(Res.string.tags),
                        onAddClick = { viewModel.onIntent(AddBudgetIntent.ToggleSheet(AddBudgetSheet.TagPicker)) },
                        addLabel = stringResource(Res.string.btn_add_tag)
                    ) {
                        state.selectedTags.forEach { tag ->
                            RemovableChip(
                                label = stringResource(Res.string.label_tag_prefix, tag.name),
                                color = GlassGreen,
                                onRemove = {
                                    val newSet = state.selectedTags.filter { it.id != tag.id }.toSet()
                                    viewModel.onIntent(AddBudgetIntent.SelectTags(newSet))
                                }
                            )
                        }
                    }
                }

                item {
                    BudgetPeriodSelector(
                        selectedPeriod = state.period,
                        onPeriodSelected = { viewModel.onIntent(AddBudgetIntent.UpdatePeriod(it)) }
                    )
                }

                item {
                    GlassCard(padding = 14.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                FintrackLabelMediumText(
                                    text = stringResource(Res.string.label_budget_alert_80),
                                    fontWeight = FontWeight.SemiBold
                                )
                                FintrackLabelSmallText(
                                    text = stringResource(Res.string.label_budget_alert_desc),
                                    color = GlassText3
                                )
                            }
                            Switch(
                                on = state.isAlertEnabled,
                                onToggle = { viewModel.onIntent(AddBudgetIntent.UpdateAlert(it)) }
                            )
                        }
                    }
                }
            }
        }
    }

    when (state.topSheet) {
        AddBudgetSheet.Calculator -> {
            CalculatorBottomSheet(
                initialAmount = state.amount,
                onConfirm = {
                    viewModel.onIntent(AddBudgetIntent.UpdateAmount(it))
                    viewModel.onIntent(AddBudgetIntent.ToggleSheet(null))
                },
                onDismiss = { viewModel.onIntent(AddBudgetIntent.ToggleSheet(null)) }
            )
        }

        AddBudgetSheet.CategoryPicker -> {
            CategoryPickerBottomSheet(
                transactionType = TransactionType.EXPENSE,
                onCategoryClick = {
                    viewModel.onIntent(AddBudgetIntent.SelectCategory(it))
                    viewModel.onIntent(AddBudgetIntent.ToggleSheet(null))
                },
                onDismiss = { viewModel.onIntent(AddBudgetIntent.ToggleSheet(null)) }
            )
        }

        AddBudgetSheet.SourcePicker -> {
            SourcePickerBottomSheet(
                onSourceClick = {
                    viewModel.onIntent(AddBudgetIntent.SelectSource(it))
                    viewModel.onIntent(AddBudgetIntent.ToggleSheet(null))
                },
                onDismiss = { viewModel.onIntent(AddBudgetIntent.ToggleSheet(null)) }
            )
        }

        AddBudgetSheet.TagPicker -> {
            val castedTags: Set<com.kazemieh.common.model.Tag> = state.selectedTags.map { 
                com.kazemieh.common.model.Tag(it.id, it.name, it.description, it.colorId, it.iconId) 
            }.toSet()
            TagPickerBottomSheet(
                selectedTags = castedTags,
                onSubmitClick = { tags ->
                    val mappedTags = tags?.map { 
                        com.kazemieh.common.model.Tag(it.id, it.name, it.description, it.colorId, it.iconId) 
                    }?.toSet() ?: emptySet()
                    viewModel.onIntent(AddBudgetIntent.SelectTags(mappedTags))
                    viewModel.onIntent(AddBudgetIntent.ToggleSheet(null))
                },
                onDismiss = { viewModel.onIntent(AddBudgetIntent.ToggleSheet(null)) }
            )
        }

        null -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MostUsedCategoryChips(
    items: List<com.kazemieh.common.model.Category>,
    onItemClick: (com.kazemieh.common.model.Category) -> Unit
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
    items: List<com.kazemieh.common.model.Source>,
    onItemClick: (com.kazemieh.common.model.Source) -> Unit
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
            else -> {
                Icon(
                    painter = painterResource(Res.drawable.ic_1),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
