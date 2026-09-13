package com.kazemieh.tag.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.toSignedPersianPrice
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntityItem
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TagDetailScreen(
    tagId: Long,
    onBack: () -> Unit,
    onNavigateToNoteEdit: (Long) -> Unit,
    viewModel: TagDetailViewModel = koinViewModel { parametersOf(tagId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.recent_transactions),
        stringResource(Res.string.notes),
        stringResource(Res.string.shopping_list),
        stringResource(Res.string.navigation_debts),
        stringResource(Res.string.navigation_installment),
        stringResource(Res.string.title_check_management),
        stringResource(Res.string.title_fixed_expense_management),
        stringResource(Res.string.label_budgets)
    )

    FintrackScreen(
        title = state.tag?.name ?: "",
        sub = stringResource(Res.string.title_tag_management),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
            
            if (selectedTabIndex == 0) {
                EntityList(
                    title = stringResource(Res.string.recent_transactions),
                    query = "",
                    onQueryChange = {},
                    items = state.transactions.map { twr ->
                        EntityItem(
                            id = twr.transaction.id,
                            name = twr.transaction.description ?: stringResource(Res.string.transaction),
                            sub = twr.transaction.amount.toSignedPersianPrice(),
                            iconId = twr.category?.iconId ?: 1,
                            colorId = twr.category?.colorId ?: 1
                        )
                    },
                    onItemClick = { }, onEditClick = { }, onDeleteClick = { },
                    onAddClick = { },
                    showActions = false,
                    showAddFab = false
                )
            } else if (selectedTabIndex == 1) {
                EntityList(
                    title = stringResource(Res.string.notes),
                    query = "",
                    onQueryChange = {},
                    items = state.notes.map { note ->
                        EntityItem(
                            id = note.id,
                            name = note.title,
                            sub = note.content.take(30),
                            iconId = 1,
                            colorId = 1
                        )
                    },
                    onItemClick = { item -> onNavigateToNoteEdit(item.id) }, onEditClick = { }, onDeleteClick = { },
                    onAddClick = { },
                    showActions = false,
                    showAddFab = false
                )
            } else if (selectedTabIndex == 2) {
                EntityList(
                    title = stringResource(Res.string.shopping_list),
                    query = "",
                    onQueryChange = {},
                    items = state.shoppingItems.map { item ->
                        EntityItem(
                            id = item.id,
                            name = item.name,
                            sub = item.note,
                            iconId = 1,
                            colorId = 1
                        )
                    },
                    onItemClick = {}, onEditClick = {}, onDeleteClick = {},
                    onAddClick = {},
                    showActions = false,
                    showAddFab = false
                )
            } else if (selectedTabIndex == 3) {
                TagDetailEntityList(
                    title = stringResource(Res.string.navigation_debts),
                    items = state.debts.map { debt ->
                        EntityItem(
                            id = debt.debt.id,
                            name = debt.debt.description ?: debt.person.name,
                            sub = debt.person.name,
                            badge = debt.debt.amount.toPersianPrice(),
                            color = if (debt.debt.isSettled) LocalGlassColors.current.text3 else GlassGreen
                        )
                    }
                )
            } else if (selectedTabIndex == 4) {
                TagDetailEntityList(
                    title = stringResource(Res.string.navigation_installment),
                    items = state.installments.map { installment ->
                        EntityItem(
                            id = installment.installment.id,
                            name = installment.installment.title,
                            sub = installment.category?.name,
                            badge = installment.installment.installmentAmount.toPersianPrice(),
                            color = if (installment.installment.isCompleted) LocalGlassColors.current.text3 else GlassGreen
                        )
                    }
                )
            } else if (selectedTabIndex == 5) {
                TagDetailEntityList(
                    title = stringResource(Res.string.title_check_management),
                    items = state.checks.map { check ->
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
            } else if (selectedTabIndex == 6) {
                TagDetailEntityList(
                    title = stringResource(Res.string.title_fixed_expense_management),
                    items = state.fixedExpenses.map { expense ->
                        EntityItem(
                            id = expense.id,
                            name = expense.title,
                            sub = expense.description ?: expense.categoryName,
                            badge = expense.amount.toPersianPrice(),
                            color = if (expense.isActive) GlassGreen else LocalGlassColors.current.text3
                        )
                    }
                )
            } else {
                TagDetailEntityList(
                    title = stringResource(Res.string.label_budgets),
                    items = state.budgets.map { budget ->
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
private fun TagDetailEntityList(title: String, items: List<EntityItem>) {
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


