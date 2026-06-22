package com.kazemieh.debt.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.debt.ui.add.AddDebtBottomSheet
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.navigation_debts
import fintrack.core.designsystem.generated.resources.title_debts_management
import fintrack.core.designsystem.generated.resources.total_credits
import fintrack.core.designsystem.generated.resources.total_debts
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebtsScreen(
    onBack: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit,
    viewModel: DebtViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(DebtIntent.ObserveAllDebts)
    }

    var selectedDebtForEdit by remember { mutableStateOf<Long?>(null) }
    var showAddDebt by remember { mutableStateOf(false) }

    val totalCredits =
        state.debts.filter { it.debt.type == DebtType.OWED_TO_ME && !it.debt.isSettled }
            .sumOf { it.debt.amount }
    val totalDebts =
        state.debts.filter { it.debt.type == DebtType.OWED_BY_ME && !it.debt.isSettled }
            .sumOf { it.debt.amount }

    FintrackScreen(
        title = stringResource(Res.string.navigation_debts),
        sub = stringResource(Res.string.title_debts_management),
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.navigation_debts),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(DebtIntent.UpdateSearchQuery(it)) },
                onAddClick = {
                    selectedDebtForEdit = null
                    showAddDebt = true
                },
                summary = listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.total_credits),
                        value = totalCredits.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.primary
                    ),
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.total_debts),
                        value = totalDebts.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.error
                    )
                ),
                items = state.filteredDebts.map {
                    EntityItem(
                        id = it.debt.id,
                        name = it.person.name,
                        sub = it.debt.description,
                        badge = it.debt.amount.toPersianPrice(),
                        color = if (it.debt.type == DebtType.OWED_TO_ME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                onItemClick = { item ->
                    state.debts.find { it.debt.id == item.id }?.let {
                        onNavigateToPersonDetail(it.person.id ?: 0L)
                    }
                },
                onDeleteClick = { viewModel.onIntent(DebtIntent.DeleteDebt(it.id)) },
                onEditClick = { item ->
                    selectedDebtForEdit = item.id
                    showAddDebt = true
                },
                showActions = true
            )
        }

        if (showAddDebt) {
            AddDebtBottomSheet(
                debtId = selectedDebtForEdit,
                onDismiss = { showAddDebt = false }
            )
        }
    }
}
