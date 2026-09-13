package com.kazemieh.financialsource.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SourceDetailScreen(
    sourceId: Long,
    onBack: () -> Unit,
    viewModel: SourceDetailViewModel = koinViewModel { parametersOf(sourceId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.recent_transactions),
        stringResource(Res.string.navigation_debts),
        stringResource(Res.string.navigation_installment),
        stringResource(Res.string.title_check_management),
        stringResource(Res.string.title_fixed_expense_management),
        stringResource(Res.string.label_budgets)
    )

    FintrackScreen(
        title = state.source?.name,
        sub = stringResource(Res.string.title_source_management),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> SourceDetailEntityList(
                    stringResource(Res.string.recent_transactions),
                    state.transactions.map { transaction ->
                        EntityItem(
                            id = transaction.transaction.id,
                            name = transaction.transaction.description ?: stringResource(Res.string.transaction),
                            sub = transaction.transaction.amount.toSignedPersianPrice(),
                            iconId = transaction.category?.iconId ?: 1,
                            colorId = transaction.category?.colorId ?: 1
                        )
                    }
                )
                1 -> SourceDetailEntityList(
                    stringResource(Res.string.navigation_debts),
                    state.debts.map { debt ->
                        EntityItem(
                            id = debt.debt.id,
                            name = debt.debt.description ?: debt.person.name,
                            sub = debt.person.name,
                            badge = debt.debt.amount.toPersianPrice(),
                            color = if (debt.debt.isSettled) LocalGlassColors.current.text3 else GlassGreen
                        )
                    }
                )
                2 -> SourceDetailEntityList(
                    stringResource(Res.string.navigation_installment),
                    state.installments.map { installment ->
                        EntityItem(
                            id = installment.installment.id,
                            name = installment.installment.title,
                            sub = installment.category?.name,
                            badge = installment.installment.installmentAmount.toPersianPrice(),
                            color = if (installment.installment.isCompleted) LocalGlassColors.current.text3 else GlassGreen
                        )
                    }
                )
                3 -> SourceDetailEntityList(
                    stringResource(Res.string.title_check_management),
                    state.checks.map { check ->
                        val status = when (check.status) {
                            CheckStatus.PENDING -> stringResource(Res.string.label_check_status_ongoing)
                            CheckStatus.PASSED -> stringResource(Res.string.label_check_status_passed)
                            CheckStatus.REJECTED -> stringResource(Res.string.label_check_status_returned)
                            CheckStatus.CANCELLED -> stringResource(Res.string.label_check_status_cancelled)
                        }
                        EntityItem(
                            id = check.id,
                            name = check.description ?: check.personName.orEmpty(),
                            sub = status,
                            badge = check.amount.toPersianPrice(),
                            color = if (check.status == CheckStatus.REJECTED) MaterialTheme.colorScheme.error else GlassGreen
                        )
                    }
                )
                4 -> SourceDetailEntityList(
                    stringResource(Res.string.title_fixed_expense_management),
                    state.fixedExpenses.map { expense ->
                        EntityItem(
                            id = expense.id,
                            name = expense.title,
                            sub = expense.description ?: expense.categoryName,
                            badge = expense.amount.toPersianPrice(),
                            color = if (expense.isActive) GlassGreen else LocalGlassColors.current.text3
                        )
                    }
                )
                else -> SourceDetailEntityList(
                    stringResource(Res.string.label_budgets),
                    state.budgets.map { budget ->
                        EntityItem(
                            id = budget.budget.id ?: 0L,
                            name = budget.category?.name ?: stringResource(Res.string.label_budgets),
                            badge = budget.budget.amount.toPersianPrice(),
                            color = GlassGreen
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SourceDetailEntityList(title: String, items: List<EntityItem>) {
    EntityList(
        title = title,
        query = "",
        onQueryChange = {},
        onAddClick = {},
        items = items,
        onEditClick = {},
        onDeleteClick = {},
        showActions = false,
        showAddFab = false
    )
}
