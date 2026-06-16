package com.kazemieh.debt.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.DebtType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.debt.ui.add.AddDebtBottomSheet
import com.kazemieh.designsystem.GlassBg0
import com.kazemieh.designsystem.GlassBg1
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebtsScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    viewModel: DebtViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(DebtIntent.ObserveAllDebts)
    }

    var showAddDebt by remember { mutableStateOf(false) }

    val totalCredits = state.debts.filter { it.debt.type == DebtType.OWED_TO_ME && !it.debt.isSettled }
        .sumOf { it.debt.amount }
    val totalDebts = state.debts.filter { it.debt.type == DebtType.OWED_BY_ME && !it.debt.isSettled }
        .sumOf { it.debt.amount }

    FintrackScreen(
        title = stringResource(Res.string.navigation_debts),
        sub = stringResource(Res.string.title_debts_management),
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.navigation_debts),
                query = "",
                onQueryChange = {},
                onAddClick = { showAddDebt = true },
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
                items = state.debts.map {
                    EntityItem(
                        id = it.debt.id,
                        name = it.person.name,
                        sub = it.debt.description,
                        badge = it.debt.amount.toPersianPrice(),
                        color = if (it.debt.type == DebtType.OWED_TO_ME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                onItemClick = { /* Navigate to detail? */ },
                onDeleteClick = { viewModel.onIntent(DebtIntent.DeleteDebt(it.id)) },
                onEditClick = { /* TODO */ },
                showActions = true
            )
        }

        if (showAddDebt) {
            AddDebtBottomSheet(
                onDismiss = { showAddDebt = false }
            )
        }
    }
}
