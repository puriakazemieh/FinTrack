@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.kazemieh.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.dashboard.component.QuickActions
import com.kazemieh.dashboard.component.RecentTransactionsWidget
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.delete.DeleteTransactionBottomSheet
import com.kazemieh.transaction.ui.main.BalanceHero
import com.kazemieh.transaction.ui.main.rememberTransactionItemsProvider
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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
        // Decorative background blobs for glass effect
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = 400.dp.toPx()
                ),
                center = Offset(size.width * 0.8f, size.height * 0.2f),
                radius = 400.dp.toPx()
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.8f),
                    radius = 500.dp.toPx()
                ),
                center = Offset(size.width * 0.2f, size.height * 0.8f),
                radius = 500.dp.toPx()
            )
        }

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
                QuickActions(
                    onActionClick = {
                        viewModel.onIntent(
                            DashboardIntent.ShowTransactionBottomSheet(
                                type = it
                            )
                        )
                    },
                    onSearchClick = { /* Handle search */ },
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                RecentTransactionsWidget(
                    onMoreClick = { /* Navigate to transactions */ },
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                FintrackHeadlineSmallText(
                    text = stringResource(Res.string.recent_transactions),
                    fontWeight = FontWeight.W700,
                    modifier = Modifier.padding(horizontal = space.large, vertical = space.medium)
                )
            }

            transactionItems()
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
                    text = stringResource(Res.string.msg_hello),
                    color = GlassText3
                )
                FintrackTitleMediumText(
                    text = stringResource(Res.string.placeholder_user_name),
                    fontWeight = FontWeight.SemiBold,
                    color = GlassText
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIconButton(icon = Icons.Default.Notifications) { /* Notification action */ }
            HeaderIconButton(icon = Icons.Default.Search) { /* Search action */ }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(GlassColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GlassText,
            modifier = Modifier.size(18.dp)
        )
    }
}
