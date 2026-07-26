package com.kazemieh.fixed_expense.ui.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var showMoreItems by remember { mutableStateOf(false) }

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
                val itemsToShow = if (showMoreItems) 10 else 5
                upcoming.take(itemsToShow).forEach { expense ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FintrackTitleSmallText(
                                text = expense.title.takeIf { it.isNotBlank() }
                                    ?: expense.categoryName
                                    ?: stringResource(Res.string.label_unknown_person),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.label_next_due, DateUtils.formatDate(expense.nextDueDate)),
                                color = glassColors.text3,
                                fontSize = 9.sp
                            )
                        }
                        MoneyText(
                            amount = expense.amount,
                            size = 11
                        )
                    }
                }

                if (upcoming.size > 5 && !showMoreItems) {
                    TextButton(
                        onClick = { showMoreItems = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp).align(Alignment.Start)
                    ) {
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.view_all),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
