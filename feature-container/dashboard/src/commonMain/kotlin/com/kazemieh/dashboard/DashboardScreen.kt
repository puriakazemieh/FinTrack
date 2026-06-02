@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.kazemieh.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.FintrackHeadlineSmallText
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.delete.DeleteTransactionBottomSheet
import com.kazemieh.transaction.ui.main.BalanceHero
import com.kazemieh.transaction.ui.main.rememberTransactionItemsProvider
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val listState = rememberLazyListState()

    val transactionItems = rememberTransactionItemsProvider(
        listState = listState,
        onEdit = { viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet(it)) },
        onDelete = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet(it)) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for NavigationBar
        ) {
            item {
                DashboardHeader(
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
                    onAddSourceClick = { viewModel.onIntent(DashboardIntent.ShowAddSource) },
                    growthPercentage = state.growthPercentage,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                FintrackHeadlineSmallText(
                    text = stringResource(Res.string.recent_transactions),
                    modifier = Modifier.padding(horizontal = space.large, vertical = space.medium)
                )
            }

            transactionItems()
        }

        FAB { viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet()) }

        if (state.showAddTransaction) {
            AddTransactionBottomSheet(
                transactionWithRelations = state.transactionWithRelations,
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
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(DashboardIntent.ShowAddSource) },
                setSource = { viewModel.onIntent(DashboardIntent.ShowAddSource) }
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    modifier: Modifier = Modifier
) {
    val space = LocalSpacing.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_source_default), // Fallback avatar
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(space.medium))
            Column {
                Text(
                    text = "Hello, User", // Hardcoded greeting for now
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row {
            IconButton(onClick = { /* Notification action */ }) {
                Icon(Icons.Default.Notifications, contentDescription = null)
            }
            IconButton(onClick = { /* Search action */ }) {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        }
    }
}
