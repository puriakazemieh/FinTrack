package com.kazemieh.fixed_expense.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.fixed_expense.ui.list.FixedExpenseListViewModel
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FixedExpenseWidget(
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FixedExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val activeExpenses = state.expenses.filter { it.isActive }

    if (activeExpenses.isNotEmpty()) {
        WidgetCard(
            title = stringResource(Res.string.title_upcoming_fixed_expenses_label),
            onMore = onMoreClick,
            modifier = modifier
        ) {
            Column {
                activeExpenses.take(2).forEach { expense ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        FintrackTitleSmallText(
                            text = expense.categoryName ?: stringResource(Res.string.label_unknown_person),
                            fontWeight = FontWeight.SemiBold
                        )
                        FintrackBodyMediumText(
                            text = stringResource(Res.string.label_toman_recurrence_summary, expense.amount.toPersianPrice(), expense.recurrence.name)
                        )
                    }
                }
            }
        }
    }
}
