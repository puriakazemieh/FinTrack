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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current

    var showAddDebt by remember { mutableStateOf(false) }
    var selectedDebtId by remember { mutableStateOf<Long?>(null) }
    var showAddTransaction by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.navigation_debts),
        stringResource(Res.string.recent_transactions)
    )

    FintrackScreen(
        title = state.person?.name ?: stringResource(Res.string.person_name),
        sub = state.person?.description ?: "",
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SecondaryTabRow(
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
            } else {
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
