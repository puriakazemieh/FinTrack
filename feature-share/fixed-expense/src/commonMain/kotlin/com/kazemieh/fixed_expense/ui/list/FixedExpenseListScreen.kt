package com.kazemieh.fixed_expense.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FixedExpenseListScreen(
    onBack: () -> Unit,
    viewModel: FixedExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }

    val totalMonthlyAmount = state.expenses.filter { it.isActive }.sumOf { it.amount }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
                .padding(padding)
                .padding(100.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScreenHeader(
                    title = stringResource(Res.string.title_fixed_expense_management),
                    sub = stringResource(Res.string.sub_fixed_expense_management),
                    onClose = onBack
                )

                EntityList(
                    title = stringResource(Res.string.title_fixed_expense_management),
                    query = "",
                    onQueryChange = {},
                    onAddClick = { showAddExpense = true },
                    summary = listOf(
                        EntitySummary(
                            label = stringResource(Res.string.label_approx_monthly_total),
                            value = totalMonthlyAmount.toPersianPrice(),
                            unit = stringResource(Res.string.currency_toman),
                            color = MaterialTheme.colorScheme.primary
                        )
                    ),
                    items = state.expenses.map {
                        EntityItem(
                            id = it.id,
                            name = it.categoryName ?: stringResource(Res.string.label_unknown_person),
                            sub = "${it.recurrence.name} - ${it.description ?: ""}",
                            badge = it.amount.toPersianPrice(),
                            color = if (it.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    },
                    onEditClick = { /* TODO */ },
                    onDeleteClick = { viewModel.onIntent(FixedExpenseListIntent.DeleteExpense(it.id)) },
                    showActions = true
                )
            }
        }
    }

    if (showAddExpense) {
        AddFixedExpenseBottomSheet(
            onDismiss = { showAddExpense = false }
        )
    }
}
