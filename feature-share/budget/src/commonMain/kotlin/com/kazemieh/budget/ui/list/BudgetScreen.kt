package com.kazemieh.budget.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.budget.ui.add.AddBudgetBottomSheet
import com.kazemieh.budget.ui.component.BudgetHero
import com.kazemieh.budget.ui.component.BudgetRow
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.glass.ScreenHeader
import com.kazemieh.jalali.JalaliCalendar
import fintrack.core.designsystem.generated.resources.*
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(Res.string.title_my_budgets),
                sub = currentMonthYear,
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = space.large,
                    vertical = space.medium
                )
            ) {
                item {
                    BudgetHero(
                        totalBudget = state.budgets.sumOf { it.budget.amount },
                        usedBudget = state.budgets.sumOf { it.spentAmount }
                    )
                }

                item { Spacer(modifier = Modifier.height(space.large)) }

                items(state.budgets) { budgetProgress ->
                    BudgetRow(
                        budgetProgress = budgetProgress,
                        onEdit = { viewModel.onIntent(BudgetIntent.ShowAddBudget(budgetProgress)) }
                    )
                    Spacer(modifier = Modifier.height(space.medium))
                }
            }
        }

        FAB(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(top = 100.dp, start = space.large, end = space.large)
        ) {
            viewModel.onIntent(BudgetIntent.ShowAddBudget())
        }

        if (state.isAddBudgetShow) {
            AddBudgetBottomSheet(
                budgetWithProgress = state.selectedBudget,
                onDismiss = { viewModel.onIntent(BudgetIntent.ShowAddBudget()) }
            )
        }
    }
}
