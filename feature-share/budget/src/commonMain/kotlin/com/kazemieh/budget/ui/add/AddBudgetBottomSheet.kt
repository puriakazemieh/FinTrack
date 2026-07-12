package com.kazemieh.budget.ui.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.budget.ui.component.BudgetPeriodSelector
import com.kazemieh.common.model.BudgetWithProgress
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.bottomsheet.FormBottomSheetScaffold
import com.kazemieh.designsystem.component.bottomsheet.SelectableListBottomSheet
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.ItemUi
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetBottomSheet(
    viewModel: AddBudgetViewModel = koinViewModel(),
    budgetWithProgress: BudgetWithProgress? = null,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    LaunchedEffect(budgetWithProgress) {
        viewModel.onIntent(AddBudgetIntent.InitialData(budgetWithProgress?.budget, budgetWithProgress?.category))
        viewModel.onIntent(AddBudgetIntent.LoadCategories())
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is AddBudgetEffect.BudgetSaved) {
                onDismiss()
            }
        }
    }

    FormBottomSheetScaffold(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
        primaryButtonText = stringResource(Res.string.save_category),
        onPrimaryClick = { viewModel.onIntent(AddBudgetIntent.SaveBudget) }
    ) {
        FintrackTitleSmallText(
            text = if (budgetWithProgress == null) stringResource(Res.string.label_new_budget) else stringResource(Res.string.label_edit_budget),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(space.medium)) {
            Field(label = stringResource(Res.string.category), required = true) {
                if (state.selectedCategory != null) {
                    FinTrackLeadingIcon(
                        colorId = state.selectedCategory?.colorId ?: 1,
                        iconId = state.selectedCategory?.iconId ?: 1,
                        style = LeadingIconStyle.Badge,
                        size = 32.dp,
                        modifier = Modifier.clickable { showCategoryPicker = true }
                    )
                } else {
                    androidx.compose.material3.TextButton(onClick = { showCategoryPicker = true }) {
                        FintrackLabelMediumText(text = stringResource(Res.string.category_choose), color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            GlassCard(padding = 16.dp) {
                Column {
                    FintrackLabelSmallText(text = stringResource(Res.string.label_budget_amount), color = GlassText3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCalculator = true }
                            .padding(top = 8.dp)
                    ) {
                        MoneyText(amount = state.amount.toLongOrNull() ?: 0L, size = 28)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val amountOptions = listOf(
                            PresetAmount(Res.string.label_500k, "500000"),
                            PresetAmount(Res.string.label_1m, "1000000"),
                            PresetAmount(Res.string.label_2m, "2000000"),
                            PresetAmount(Res.string.label_3m, "3000000"),
                            PresetAmount(Res.string.label_5m, "5000000"),
                            PresetAmount(Res.string.label_10m, "10000000")
                        )
                        amountOptions.forEach { option ->
                            Chip(
                                active = state.amount == option.value,
                                onClick = { viewModel.onIntent(AddBudgetIntent.UpdateAmount(option.value)) }
                            ) {
                                FintrackLabelSmallText(text = stringResource(option.labelRes))
                            }
                        }
                    }
                }
            }

            BudgetPeriodSelector(
                selectedPeriod = state.period,
                onPeriodSelected = { viewModel.onIntent(AddBudgetIntent.UpdatePeriod(it)) }
            )

            GlassCard(padding = 14.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        FintrackLabelMediumText(text = stringResource(Res.string.label_budget_alert_80), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        FintrackLabelSmallText(text = stringResource(Res.string.label_budget_alert_desc), color = GlassText3)
                    }
                    Switch(
                        on = state.isAlertEnabled,
                        onToggle = { viewModel.onIntent(AddBudgetIntent.UpdateAlert(it)) }
                    )
                }
            }
        }
    }

    if (showCalculator) {
        CalculatorBottomSheet(
            initialAmount = state.amount,
            onConfirm = {
                viewModel.onIntent(AddBudgetIntent.UpdateAmount(it))
                showCalculator = false
            },
            onDismiss = { showCalculator = false }
        )
    }

    if (showCategoryPicker) {
        SelectableListBottomSheet(
            title = stringResource(Res.string.label_select_category),
            items = state.categories.map { it.toItemUi() }.toSet(),
            onConfirm = { selectedItems, _ ->
                val selectedItem = selectedItems.firstOrNull()
                state.categories.find { it.id == selectedItem?.id }?.let {
                    viewModel.onIntent(AddBudgetIntent.SelectCategory(it))
                }
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

private fun com.kazemieh.common.model.Category.toItemUi() = ItemUi(
    id = id ?: 0L,
    title = com.kazemieh.designsystem.component.model.UiText.DynamicString(name),
    iconId = iconId,
    colorId = colorId
)

private data class PresetAmount(
    val labelRes: org.jetbrains.compose.resources.StringResource,
    val value: String
)
