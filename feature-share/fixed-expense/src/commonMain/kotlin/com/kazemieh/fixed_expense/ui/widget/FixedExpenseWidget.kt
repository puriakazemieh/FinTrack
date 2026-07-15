package com.kazemieh.fixed_expense.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.glass.WidgetEmptyState
import com.kazemieh.fixed_expense.ui.list.FixedExpenseListViewModel
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FixedExpenseWidget(
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FixedExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val glassColors = LocalGlassColors.current
    val activeExpenses = state.expenses.filter { it.isActive }
    // Soonest due first — the ones the user is about to pay are the most useful to see.
    val upcoming = activeExpenses.sortedBy { it.nextDueDate }

    WidgetCard(
        title = stringResource(Res.string.title_upcoming_fixed_expenses_label),
        count = activeExpenses.size.takeIf { it > 0 },
        onMore = onMore,
        modifier = modifier
    ) {
        if (activeExpenses.isEmpty()) {
            WidgetEmptyState(
                icon = Icons.Default.EventRepeat,
                text = stringResource(Res.string.fixed_expense_empty)
            )
        } else {
            Column {
                upcoming.take(3).forEach { expense ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FintrackTitleSmallText(
                                text = expense.categoryName ?: stringResource(Res.string.label_unknown_person),
                                fontWeight = FontWeight.SemiBold
                            )
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.label_next_due, DateUtils.formatDate(expense.nextDueDate)),
                                color = glassColors.text3
                            )
                        }
                        MoneyText(amount = expense.amount)
                    }
                }
            }
        }
    }
}
