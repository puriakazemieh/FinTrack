package com.kazemieh.budget.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.budget.ui.add.AddBudgetBottomSheet
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.label_budget_consumed
import fintrack.core.designsystem.generated.resources.label_remaining
import fintrack.core.designsystem.generated.resources.title_my_budgets
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    val currentMonthYear = remember {
        val today = JalaliCalendar.today()
        "${today.monthString} ${today.year}"
    }

    val totalBudget = state.budgets.sumOf { it.budget.amount }
    val totalSpent = state.budgets.sumOf { it.spentAmount }
    val totalRemaining = (totalBudget - totalSpent).coerceAtLeast(0)

    FintrackScreen(
        title = stringResource(Res.string.title_my_budgets),
        sub = currentMonthYear,
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.title_my_budgets),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(BudgetIntent.UpdateSearchQuery(it)) },
                onAddClick = { viewModel.onIntent(BudgetIntent.ShowAddBudget()) },
                summary = listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.title_my_budgets),
                        value = totalBudget.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman)
                    ),
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.label_budget_consumed),
                        value = totalSpent.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = GlassGreen
                    ),
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.label_remaining),
                        value = totalRemaining.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.primary
                    )
                ),
                items = state.filteredBudgets.map {
                    val progress = (it.spentAmount.toFloat() / it.budget.amount).coerceIn(0f, 1f)
                    val percentage = (progress * 100).toInt()
                    EntityItem(
                        id = it.budget.id ?: 0L,
                        name = it.category?.name ?: "",
                        sub = "${it.spentAmount.toPersianPrice()} / ${it.budget.amount.toPersianPrice()}",
                        badge = "$percentage%",
                        color = if (progress >= 0.8f) MaterialTheme.colorScheme.error else GlassGreen,
                        iconId = it.category?.iconId,
                        colorId = it.category?.colorId
                    )
                },
                onEditClick = { item ->
                    state.budgets.find { it.budget.id == item.id }?.let {
                        viewModel.onIntent(BudgetIntent.ShowAddBudget(it))
                    }
                },
                onDeleteClick = { viewModel.onIntent(BudgetIntent.DeleteBudget(it.id)) },
                showActions = true
            )
        }

        if (state.isAddBudgetShow) {
            AddBudgetBottomSheet(
                budgetWithProgress = state.selectedBudget,
                onDismiss = { viewModel.onIntent(BudgetIntent.ShowAddBudget()) }
            )
        }
    }
}
