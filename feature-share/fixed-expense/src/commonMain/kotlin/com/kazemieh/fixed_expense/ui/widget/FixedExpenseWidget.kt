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
            title = "هزینه‌های ثابت پیش‌رو",
            onMore = onMoreClick,
            modifier = modifier
        ) {
            Column {
                activeExpenses.take(2).forEach { expense ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        FintrackTitleSmallText(
                            text = expense.categoryName ?: "نامشخص",
                            fontWeight = FontWeight.SemiBold
                        )
                        FintrackBodyMediumText(
                            text = "${expense.amount.toPersianPrice()} تومان - ${expense.recurrence.name}"
                        )
                    }
                }
            }
        }
    }
}
