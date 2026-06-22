package com.kazemieh.person.ui.detail

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
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.balance_total
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.debt_settled
import fintrack.core.designsystem.generated.resources.description
import fintrack.core.designsystem.generated.resources.label_active
import fintrack.core.designsystem.generated.resources.navigation_debts
import fintrack.core.designsystem.generated.resources.person_name
import fintrack.core.designsystem.generated.resources.total_credits
import fintrack.core.designsystem.generated.resources.total_debts
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PersonDetailScreen(
    personId: Long,
    onBack: () -> Unit,
    addDebtSheet: @Composable (personId: Long, debtId: Long?, onDismiss: () -> Unit) -> Unit,
    viewModel: PersonDetailViewModel = koinViewModel { parametersOf(personId) }
) {
    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current

    var showAddDebt by remember { mutableStateOf(false) }
    var selectedDebtId by remember { mutableStateOf<Long?>(null) }

    FintrackScreen(
        title = state.person?.name ?: stringResource(Res.string.person_name),
        sub = state.person?.description ?: "",
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        unit = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.primary
                    ),
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.total_debts),
                        value = state.totalDebts.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.error
                    ),
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.balance_total),
                        value = state.balance.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
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
                        color = if (it.debt.type == DebtType.OWED_TO_ME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                onItemClick = { /* Debt detail? */ },
                onDeleteClick = { viewModel.onIntent(PersonDetailIntent.DeleteDebt(it.id)) },
                onEditClick = { item ->
                    selectedDebtId = item.id
                    showAddDebt = true
                },
                showActions = true
            )
        }

        if (showAddDebt) {
            addDebtSheet(personId, selectedDebtId) { showAddDebt = false }
        }
    }
}
