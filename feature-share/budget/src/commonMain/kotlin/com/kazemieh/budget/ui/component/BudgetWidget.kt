package com.kazemieh.budget.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.toPersianDigits
import com.kazemieh.budget.ui.list.BudgetViewModel
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.glass.WidgetEmptyState
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetWidget(
    viewModel: BudgetViewModel = koinViewModel(),
    onMore: () -> Unit,
    onAdd: () -> Unit = {},
    onEditBudget: (com.kazemieh.common.model.BudgetWithProgress) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val budgets = state.dailyBudgets + state.weeklyBudgets + state.monthlyBudgets + state.yearlyBudgets
    val totalBudget = budgets.sumOf { it.budget.amount }
    val spentBudget = budgets.sumOf { it.spentAmount }
    val progress = if (totalBudget > 0) spentBudget.toFloat() / totalBudget else 0f
    val overCount = budgets.count { it.progress > 1f }
    // Surface the budgets closest to (or over) their limit first — that's what needs attention.
    val mostUsed = budgets.sortedByDescending { it.progress }

    WidgetCard(
        title = stringResource(Res.string.label_budgets),
        count = budgets.size.takeIf { it > 0 },
        accent = if (overCount > 0) GlassRed else null,
        badge = if (overCount > 0) stringResource(Res.string.label_over_count, overCount) else null,
        onMore = onMore,
        onAdd = onAdd,
        modifier = modifier
    ) {
        if (budgets.isEmpty()) {
            WidgetEmptyState(
                icon = Icons.Default.PieChart,
                text = stringResource(Res.string.budget_empty)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                        CircularProgress(
                            progress = progress,
                            modifier = Modifier.size(40.dp)
                        )
                        FintrackLabelSmallText(
                            text = stringResource(
                                Res.string.percentage_format,
                                (progress * 100).toInt().toLong().toPersianDigits()
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }

                    Column {
                        FintrackLabelSmallText(text = stringResource(Res.string.label_budget_sum), fontSize = 9.sp)
                        MoneyText(amount = totalBudget, size = 13)
                    }
                }

                mostUsed.take(2).forEach { budgetProgress ->
                    BudgetRow(
                        budgetProgress = budgetProgress,
                        onEdit = { onEditBudget(budgetProgress) }
                    )
                }
            }
        }
    }
}
