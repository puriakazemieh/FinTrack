package com.kazemieh.fixed_expense.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.label_approx_monthly_total
import fintrack.core.designsystem.generated.resources.label_unknown_person
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.kazemieh.common.DateFilterType
import com.kazemieh.common.DateRangeLabel
import com.kazemieh.common.model.RecurrenceType
import com.kazemieh.designsystem.component.glass.PeriodNavigator
import fintrack.core.designsystem.generated.resources.all
import fintrack.core.designsystem.generated.resources.custom_date
import fintrack.core.designsystem.generated.resources.frequency_daily
import fintrack.core.designsystem.generated.resources.frequency_monthly
import fintrack.core.designsystem.generated.resources.frequency_weekly
import fintrack.core.designsystem.generated.resources.frequency_yearly
import fintrack.core.designsystem.generated.resources.label_period_none
import fintrack.core.designsystem.generated.resources.label_range
import fintrack.core.designsystem.generated.resources.label_this_year
import fintrack.core.designsystem.generated.resources.last_month
import fintrack.core.designsystem.generated.resources.this_month
import fintrack.core.designsystem.generated.resources.this_week
import fintrack.core.designsystem.generated.resources.today
import fintrack.core.designsystem.generated.resources.yesterday
import fintrack.core.designsystem.generated.resources.title_fixed_expense_management
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedExpenseListScreen(
    onBack: () -> Unit,
    viewModel: FixedExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }
    var selectedExpenseId by remember { mutableStateOf<Long?>(null) }

    val totalMonthlyAmount = state.expenses.filter { it.isActive }.sumOf { it.amount }

    val displayLabel = when (val label = state.dateRange?.label) {
        is DateRangeLabel.Filter -> {
            when (label.type) {
                DateFilterType.TODAY -> stringResource(Res.string.today)
                DateFilterType.YESTERDAY -> stringResource(Res.string.yesterday)
                DateFilterType.THIS_WEEK -> stringResource(Res.string.this_week)
                DateFilterType.THIS_MONTH -> stringResource(Res.string.this_month)
                DateFilterType.LAST_MONTH -> stringResource(Res.string.last_month)
                DateFilterType.CUSTOM_RANGE -> stringResource(Res.string.custom_date)
                DateFilterType.THIS_YEAR -> stringResource(Res.string.label_this_year)
                else -> stringResource(Res.string.all)
            }
        }
        is DateRangeLabel.Text -> label.value
        null -> ""
    }

    FintrackScreen(
        title = stringResource(Res.string.title_fixed_expense_management),
        sub = displayLabel,
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PeriodNavigator(
                currentPeriod = state.dateRange?.filterType ?: DateFilterType.THIS_MONTH,
                periodLabel = displayLabel,
                periodSubLabel = "",
                onPeriodSelected = { viewModel.onIntent(FixedExpenseListIntent.ChangeFilterType(it)) },
                onPrevClick = { viewModel.onIntent(FixedExpenseListIntent.ShiftRange(com.kazemieh.common.Direction.PREVIOUS)) },
                onNextClick = { viewModel.onIntent(FixedExpenseListIntent.ShiftRange(com.kazemieh.common.Direction.NEXT)) },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                excludeCustomRange = true
            )

            EntityList(
                title = stringResource(Res.string.title_fixed_expense_management),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(FixedExpenseListIntent.UpdateSearchQuery(it)) },
                onAddClick = { selectedExpenseId = null; showAddExpense = true },
                summary = listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.label_approx_monthly_total),
                        value = totalMonthlyAmount.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.primary
                    )
                ),
                items = state.filteredExpenses.map {
                    val recurrenceLabel = when (it.recurrence) {
                        RecurrenceType.DAILY -> stringResource(Res.string.frequency_daily)
                        RecurrenceType.WEEKLY -> stringResource(Res.string.frequency_weekly)
                        RecurrenceType.MONTHLY -> stringResource(Res.string.frequency_monthly)
                        RecurrenceType.YEARLY -> stringResource(Res.string.frequency_yearly)
                        RecurrenceType.CUSTOM -> stringResource(Res.string.custom_date)
                        RecurrenceType.NONE -> stringResource(Res.string.label_period_none)
                        RecurrenceType.ONCE -> stringResource(Res.string.today)
                    }
                    EntityItem(
                        id = it.id,
                        name = it.title,
                        sub = "$recurrenceLabel" + (it.categoryName?.let { cat -> " - $cat" } ?: ""),
                        badge = it.amount.toPersianPrice(),
                        color = if (it.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                },
                onEditClick = { selectedExpenseId = it.id; showAddExpense = true },
                onDeleteClick = { item ->
                    viewModel.onIntent(
                        FixedExpenseListIntent.OnDeleteClick(state.expenses.find { it.id == item.id })
                    )
                },
                showActions = true
            )
        }

        if (showAddExpense) {
            AddFixedExpenseBottomSheet(
                expenseId = selectedExpenseId,
                onDismiss = { showAddExpense = false; selectedExpenseId = null }
            )
        }

        if (state.isDeleteShow && state.selectedExpense != null) {
            DeleteBottomSheet(
                itemName = state.selectedExpense?.categoryName
                    ?: state.selectedExpense?.description,
                dismissClicked = { viewModel.onIntent(FixedExpenseListIntent.OnDeleteClick(null)) },
                confirmClicked = { viewModel.onIntent(FixedExpenseListIntent.ConfirmDelete) }
            )
        }
    }
}
