package com.kazemieh.budget.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.budget.ui.list.BudgetViewModel
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetWidget(
    viewModel: BudgetViewModel = koinViewModel(),
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val budgets = state.dailyBudgets + state.weeklyBudgets + state.monthlyBudgets + state.yearlyBudgets
    val totalBudget = budgets.sumOf { it.budget.amount }
    val spentBudget = budgets.sumOf { it.spentAmount }
    val progress = if (totalBudget > 0) spentBudget.toFloat() / totalBudget else 0f

    WidgetCard(
        title = stringResource(Res.string.label_budgets),
        onMore = onMore,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                    CircularProgress(progress = progress)
                    FintrackLabelSmallText(
                        text = stringResource(Res.string.percentage_format, (progress * 100).toInt()),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    FintrackLabelSmallText(text = stringResource(Res.string.label_budget_sum))
                    MoneyText(amount = totalBudget, size = 16)
                }
            }

            if (budgets.isNotEmpty()) {
                budgets.take(2).forEach { budgetProgress ->
                    BudgetRow(
                        budgetProgress = budgetProgress,
                        onEdit = onMore
                    )
                }
            }
        }
    }
}
