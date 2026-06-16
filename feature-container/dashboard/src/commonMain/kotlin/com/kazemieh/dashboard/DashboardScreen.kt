@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.kazemieh.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.asset.ui.component.AssetWidget
import com.kazemieh.budget.ui.component.BudgetWidget
import com.kazemieh.check.ui.widget.CheckWidget
import com.kazemieh.dashboard.component.QuickActions
import com.kazemieh.dashboard.component.RecentTransactionsWidget
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenDark
import com.kazemieh.designsystem.GlassGreenDeep
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.fixed_expense.ui.widget.FixedExpenseWidget
import com.kazemieh.installment.ui.widget.InstallmentWidget
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.delete.DeleteTransactionBottomSheet
import com.kazemieh.transaction.ui.main.BalanceHero
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.msg_hello
import fintrack.core.designsystem.generated.resources.placeholder_user_initial
import fintrack.core.designsystem.generated.resources.placeholder_user_name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onNavigateToTransactions: (Any?) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToCheck: () -> Unit = {},
    onNavigateToFixedExpense: () -> Unit = {},
    onNavigateToAssets: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val listState = rememberLazyListState()


    FintrackScreen {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for NavigationBar
        ) {
            item {
                DashboardHeader(
                    onNavigateToSearch = onNavigateToSearch,
                    modifier = Modifier.padding(
                        horizontal = space.large,
                        vertical = space.mediumSmall
                    )
                )
            }

            item {
                BalanceHero(
                    isBalanceVisible = state.isBalanceVisible,
                    onToggleVisibility = { viewModel.onIntent(DashboardIntent.ToggleBalanceVisibility) },
                    onAddSourceClick = { viewModel.onIntent(DashboardIntent.ShowAddSource()) },
                    onSourceClick = { source ->
                        viewModel.onIntent(DashboardIntent.ShowAddSource(source))
                    },
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                QuickActions(
                    onActionClick = {
                        viewModel.onIntent(
                            DashboardIntent.ShowTransactionBottomSheet(type = it)
                        )
                    },
                    onSearchClick = onNavigateToSearch,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                RecentTransactionsWidget(
                    onMoreClick = { onNavigateToTransactions(true) },
                    onEdit = { transactionWithRelations ->
                        viewModel.onIntent(
                            DashboardIntent.ShowTransactionBottomSheet(transactionWithRelations)
                        )
                    },
                    onDelete = { transactionWithRelations ->
                        viewModel.onIntent(
                            DashboardIntent.DeleteTransactionBottomSheet(transactionWithRelations)
                        )
                    },
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                BudgetWidget(
                    onMoreClick = onNavigateToBudget,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                InstallmentWidget(
                    onMoreClick = { /* Navigate to installments */ },
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                CheckWidget(
                    onMoreClick = onNavigateToCheck,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                FixedExpenseWidget(
                    onMoreClick = onNavigateToFixedExpense,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                AssetWidget(
                    onMoreClick = onNavigateToAssets,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

        }

        FAB(modifier = Modifier.padding(bottom = 60.dp)) { viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet()) }

        if (state.showAddTransaction) {
            AddTransactionBottomSheet(
                transactionWithRelations = state.transactionWithRelations,
                initialType = state.initialTransactionType,
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet()) },
                transactionAdded = { viewModel.onIntent(DashboardIntent.AnimationEnabled) },
            )
        }

        if (state.showDeleteTransaction) {
            DeleteTransactionBottomSheet(
                snackbarHostState = snackbarHostState,
                transactionWithRelations = state.transactionWithRelations,
                onDismiss = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet()) },
                transactionDeleted = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet()) },
            )
        }

        if (state.showAddSource) {
            AddSourceBottomSheet(
                selectedSource = state.selectedSource,
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(DashboardIntent.ShowAddSource()) },
                onNavigateToTransactions = { source ->
                    viewModel.onIntent(DashboardIntent.ShowAddSource())
                    onNavigateToTransactions(source)
                },
                setSource = { viewModel.onIntent(DashboardIntent.ShowAddSource()) }
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit = {}
) {
    val space = LocalSpacing.current
    val glassColors = LocalGlassColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Brush.linearGradient(listOf(GlassGreen, GlassGreenDeep))),
                contentAlignment = Alignment.Center
            ) {
                FintrackLabelMediumText(
                    text = stringResource(Res.string.placeholder_user_initial),
                    color = GlassGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Column {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.msg_hello)
                )
                FintrackTitleMediumText(
                    text = stringResource(Res.string.placeholder_user_name),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIconButton(icon = Icons.Default.Notifications) { /* Notification action */ }
            HeaderIconButton(icon = Icons.Default.Search, onClick = onNavigateToSearch)
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(glassColors.glass)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}
