package com.kazemieh.fixed_expense.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import fintrack.core.designsystem.generated.resources.sub_fixed_expense_management
import fintrack.core.designsystem.generated.resources.title_fixed_expense_management
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FixedExpenseListScreen(
    onBack: () -> Unit,
    viewModel: FixedExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }
    var selectedExpenseId by remember { mutableStateOf<Long?>(null) }

    val totalMonthlyAmount = state.expenses.filter { it.isActive }.sumOf { it.amount }

    FintrackScreen(
        title = stringResource(Res.string.title_fixed_expense_management),
        sub = stringResource(Res.string.sub_fixed_expense_management),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    EntityItem(
                        id = it.id,
                        name = it.categoryName ?: stringResource(Res.string.label_unknown_person),
                        sub = "${it.recurrence.name} - ${it.description ?: ""}",
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
