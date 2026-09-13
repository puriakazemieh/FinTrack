package com.kazemieh.person.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.CheckStatus
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.person.ui.detail.PersonDetailIntent
import com.kazemieh.person.ui.detail.PersonDetailViewModel
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PersonDetailScreen(
    personId: Long,
    onBack: () -> Unit,
    addDebtSheet: @Composable (personId: Long, debtId: Long?, onDismiss: () -> Unit) -> Unit,
    addTransactionSheet: @Composable (onDismiss: () -> Unit) -> Unit,
    viewModel: PersonDetailViewModel = koinViewModel { parametersOf(personId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    var showAddDebt by remember { mutableStateOf(false) }
    var selectedDebtId by remember { mutableStateOf<Long?>(null) }
    var showAddTransaction by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.navigation_debts),
        stringResource(Res.string.recent_transactions),
        stringResource(Res.string.navigation_installment),
        stringResource(Res.string.title_check_management),
        stringResource(Res.string.title_fixed_expense_management),
        stringResource(Res.string.label_budgets)
    )

    FintrackScreen(
        title = state.person?.name ?: stringResource(Res.string.person_name),
        sub = state.person?.description ?: "",
        onClose = onBack
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
                        text = {
                            FintrackLabelMediumText(
                                text = title,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                EntityList(
                    title = stringResource(Res.string.navigation_debts),
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onIntent(PersonDetailIntent.UpdateSearchQuery(it)) },
                    onAddClick = {
                        selectedDebtId = null
                        showAddDebt = true
                    },
                    summary = listOf(
                        EntitySummary(
                            label = UiText.StringResourceText(Res.string.total_credits),
                            value = state.totalCredits.toPersianPrice(),
                            unit = com.kazemieh.designsystem.LocalCurrency.current.symbol,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        EntitySummary(
                            label = UiText.StringResourceText(Res.string.total_debts),
                            value = state.totalDebts.toPersianPrice(),
                            unit = com.kazemieh.designsystem.LocalCurrency.current.symbol,
                            color = MaterialTheme.colorScheme.error
                        ),
                        EntitySummary(
                            label = UiText.StringResourceText(Res.string.balance_total),
                            value = state.balance.toPersianPrice(),
                            unit = com.kazemieh.designsystem.LocalCurrency.current.symbol,
                            color = if (state.balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    ),
                    items = state.filteredDebts.map {
                        EntityItem(
                            id = it.debt.id,
                            name = it.debt.description ?: stringResource(Res.string.description),
                            sub = if (it.debt.isSettled) stringResource(Res.string.debt_settled) else stringResource(
                                Res.string.label_active
                            ),
                            badge = it.debt.amount.toPersianPrice(),
                            color = if (it.debt.type == DebtType.OWED_TO_ME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            trailingContent = {
                                if (!it.debt.isSettled) {
                                    val glassColors = LocalGlassColors.current
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(glassColors.glass)
                                            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(9.dp))
                                            .clickable {
                                                viewModel.onIntent(
                                                    PersonDetailIntent.SettleDebt(
                                                        it.debt.id,
                                                        "" // Handled in VM usually
                                                    )
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = GlassGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        )
                    },
                    onItemClick = { item ->
                        selectedDebtId = item.id
                        showAddDebt = true
                    },
                    onDeleteClick = { viewModel.onIntent(PersonDetailIntent.DeleteDebt(it.id)) },
                    onEditClick = { item ->
                        selectedDebtId = item.id
                        showAddDebt = true
                    },
                    showActions = true
                )
            } else if (selectedTabIndex == 1) {
                EntityList(
                    title = stringResource(Res.string.recent_transactions),
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onIntent(PersonDetailIntent.UpdateSearchQuery(it)) },
                    onAddClick = { showAddTransaction = true },
                    items = state.transactions.map {
                        EntityItem(
                            id = it.transaction.id,
                            name = it.category?.name ?: "",
                            sub = it.transaction.description,
                            badge = it.transaction.amount.toSignedPersianPrice(),
                            color = if (it.transaction.amount >= 0) GlassGreen else MaterialTheme.colorScheme.error,
                            iconId = it.category?.iconId,
                            colorId = it.category?.colorId
                        )
                    },
                    onEditClick = {},
                    onDeleteClick = {},
                    showActions = false
                )
            } else if (selectedTabIndex == 2) {
                EntityList(
                    title = stringResource(Res.string.navigation_installment),
                    query = "",
                    onQueryChange = {},
                    onAddClick = {},
                    items = state.installments.map { item ->
                        EntityItem(
                            id = item.installment.id,
                            name = item.installment.title,
                            sub = item.category?.name ?: item.source?.name,
                            badge = item.installment.installmentAmount.toPersianPrice(),
                            color = if (item.installment.isCompleted) {
                                LocalGlassColors.current.text3
                            } else {
                                GlassGreen
                            }
                        )
                    },
                    onEditClick = {},
                    onDeleteClick = {},
                    showActions = false,
                    showAddFab = false
                )
            } else if (selectedTabIndex == 3) {
                EntityList(
                    title = stringResource(Res.string.title_check_management),
                    query = "",
                    onQueryChange = {},
                    onAddClick = {},
                    items = state.checks.map { check ->
                        val statusText = when (check.status) {
                            CheckStatus.PENDING -> stringResource(Res.string.label_check_status_ongoing)
                            CheckStatus.PASSED -> stringResource(Res.string.label_check_status_passed)
                            CheckStatus.REJECTED -> stringResource(Res.string.label_check_status_returned)
                            CheckStatus.CANCELLED -> stringResource(Res.string.label_check_status_cancelled)
                        }
                        EntityItem(
                            id = check.id,
                            name = check.description ?: state.person?.name.orEmpty(),
                            sub = statusText,
                            badge = check.amount.toPersianPrice(),
                            color = when (check.status) {
                                CheckStatus.PENDING -> MaterialTheme.colorScheme.primary
                                CheckStatus.PASSED -> GlassGreen
                                CheckStatus.REJECTED -> MaterialTheme.colorScheme.error
                                CheckStatus.CANCELLED -> LocalGlassColors.current.text3
                            }
                        )
                    },
                    onEditClick = {},
                    onDeleteClick = {},
                    showActions = false,
                    showAddFab = false
                )
            } else if (selectedTabIndex == 4) {
                EntityList(
                    title = stringResource(Res.string.title_fixed_expense_management),
                    query = "",
                    onQueryChange = {},
                    onAddClick = {},
                    items = state.fixedExpenses.map { expense ->
                        EntityItem(
                            id = expense.id,
                            name = expense.title,
                            sub = expense.description ?: expense.categoryName,
                            badge = expense.amount.toPersianPrice(),
                            color = if (expense.isActive) GlassGreen else LocalGlassColors.current.text3
                        )
                    },
                    onEditClick = {},
                    onDeleteClick = {},
                    showActions = false,
                    showAddFab = false
                )
            } else {
                EntityList(
                    title = stringResource(Res.string.label_budgets),
                    query = "",
                    onQueryChange = {},
                    onAddClick = {},
                    items = state.budgets.map { budget ->
                        EntityItem(
                            id = budget.budget.id ?: 0L,
                            name = budget.category?.name ?: stringResource(Res.string.label_budgets),
                            badge = budget.budget.amount.toPersianPrice(),
                            color = GlassGreen
                        )
                    },
                    onEditClick = {},
                    onDeleteClick = {},
                    showActions = false,
                    showAddFab = false
                )
            }
        }

        if (showAddDebt) {
            addDebtSheet(personId, selectedDebtId) { showAddDebt = false }
        }

        if (showAddTransaction) {
            addTransactionSheet { showAddTransaction = false }
        }
    }
}
