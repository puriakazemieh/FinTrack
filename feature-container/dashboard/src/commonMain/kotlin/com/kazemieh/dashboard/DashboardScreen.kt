@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.kazemieh.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianDigits
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.asset.ui.component.AssetWidget
import com.kazemieh.budget.ui.component.BudgetWidget
import com.kazemieh.check.ui.widget.CheckWidget
import com.kazemieh.dashboard.component.QuickActions
import com.kazemieh.dashboard.component.RecentTransactionsWidget
import com.kazemieh.designsystem.GlassGreenDark
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.fixed_expense.ui.widget.FixedExpenseWidget
import com.kazemieh.goals.presentation.component.GoalWidget
import com.kazemieh.gamification.ui.AchievementWidget
import com.kazemieh.ai_insights.ui.AIAdvisorWidget
import com.kazemieh.installment.ui.widget.InstallmentWidget
import com.kazemieh.shopping.ui.ShoppingWidget
import com.kazemieh.notes.ui.NotesWidget
import com.kazemieh.sms_reader.ui.SmsBanner
import com.kazemieh.sms_reader.ui.SmsDetectionSheet
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
    showAddTransaction: Boolean = false,
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateToTransactions: (Any?) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToGoal: () -> Unit = {},
    onNavigateToInstallment: () -> Unit = {},
    onNavigateToCheck: () -> Unit = {},
    onNavigateToFixedExpense: () -> Unit = {},
    onNavigateToAIAdvisor: () -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    onNavigateToShopping: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToProfileEdit: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val listState = rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(showAddTransaction) {
        if (showAddTransaction) {
            viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet())
        }
    }


    FintrackScreen {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for NavigationBar
        ) {
            item {
                DashboardHeader(
                    userName = state.userName,
                    userInitial = state.userInitial,
                    level = state.streak.level,
                    onNavigateToSearch = onNavigateToSearch,
                    onProfileClick = onNavigateToProfileEdit,
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

            if (state.smsDrafts.isNotEmpty()) {
                item {
                    SmsBanner(
                        count = state.smsDrafts.size,
                        onClick = { viewModel.onIntent(DashboardIntent.ToggleSmsDetectionSheet) },
                        onClose = { /* Optionally hide for session */ },
                        modifier = Modifier.padding(horizontal = space.large, vertical = space.small)
                    )
                }
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
                    onMore = { onNavigateToTransactions(true) },
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
                AchievementWidget(
                    streak = state.streak,
                    achievements = state.achievements,
                    onMore = onNavigateToAchievements,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                BudgetWidget(
                    onMore = { onNavigateToBudget() },
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                GoalWidget(
                    onMore = { onNavigateToGoal() },
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                InstallmentWidget(
                    onMore = { onNavigateToInstallment() },
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                CheckWidget(
                    onMore = onNavigateToCheck,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                FixedExpenseWidget(
                    onMore = onNavigateToFixedExpense,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                AIAdvisorWidget(
                    onMore = onNavigateToAIAdvisor,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                AssetWidget(
                    onMore = onNavigateToAssets,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                ShoppingWidget(
                    onMore = onNavigateToShopping,
                    modifier = Modifier.padding(horizontal = space.large)
                )
            }

            item { Spacer(Modifier.height(space.large)) }

            item {
                NotesWidget(
                    onMore = onNavigateToNotes,
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
                smsDraft = state.smsDraft,
                onDismiss = { viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet()) },
                transactionAdded = { viewModel.onIntent(DashboardIntent.AnimationEnabled) },
            )
        }

        if (state.showSmsDetection) {
            SmsDetectionSheet(
                drafts = state.smsDrafts,
                categories = state.categories,
                sources = state.sources,
                onDraftClick = { draft ->
                    viewModel.onIntent(DashboardIntent.ToggleSmsDetectionSheet)
                    viewModel.onIntent(
                        DashboardIntent.ShowTransactionBottomSheet(
                            smsDraft = draft,
                            type = draft.type
                        )
                    )
                },
                onIgnore = { viewModel.onIntent(DashboardIntent.IgnoreSmsDraft(it)) },
                onUpdateDraft = { viewModel.onIntent(DashboardIntent.UpdateSmsDraft(it)) },
                onDismiss = { viewModel.onIntent(DashboardIntent.ToggleSmsDetectionSheet) }
            )
        }

        if (state.showDeleteTransaction) {
            DeleteTransactionBottomSheet(
                transactionWithRelations = state.transactionWithRelations,
                onDismiss = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet()) },
                transactionDeleted = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet()) },
            )
        }

        if (state.showAddSource) {
            AddSourceBottomSheet(
                selectedSource = state.selectedSource,
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
    userName: String,
    userInitial: String,
    level: Int = 1,
    onNavigateToSearch: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onProfileClick
            )
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(GlassGreenDark.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                FintrackLabelMediumText(
                    text = userInitial,
                    color = GlassGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                
                // Level small badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.toString().toPersianDigits(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.msg_hello)
                )
                FintrackTitleMediumText(
                    text = userName,
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
