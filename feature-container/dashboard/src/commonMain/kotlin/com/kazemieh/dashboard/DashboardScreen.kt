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
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.setValue
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
    onNavigateToProfileEdit: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current
    val listState = rememberLazyListState()
    var smsBannerDismissed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var repeatTemplate by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<com.kazemieh.common.model.TransactionWithRelations?>(null)
    }

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
                    onNotificationsClick = onNavigateToNotifications,
                    onProfileClick = onNavigateToProfileEdit,
                    onCustomizeClick = { viewModel.onIntent(DashboardIntent.ToggleCustomizeSheet) },
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

            if (state.smsDrafts.isNotEmpty() && !smsBannerDismissed) {
                item {
                    SmsBanner(
                        count = state.smsDrafts.size,
                        onClick = { viewModel.onIntent(DashboardIntent.ToggleSmsDetectionSheet) },
                        onClose = { smsBannerDismissed = true },
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

            state.dashboardWidgets.forEach { cfg ->
                if (!cfg.visible) return@forEach
                if (cfg.widget.toolFeature()?.let { it in state.disabledTools } == true) return@forEach
                item(key = cfg.widget.name) {
                    DashboardWidgetContent(
                        widget = cfg.widget,
                        state = state,
                        onEditTransaction = { twr ->
                            viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet(twr))
                        },
                        onDeleteTransaction = { twr ->
                            viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet(twr))
                        },
                        onRepeatTransaction = { twr ->
                            repeatTemplate = twr
                            viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet())
                        },
                        onNavigateToTransactions = onNavigateToTransactions,
                        onNavigateToBudget = onNavigateToBudget,
                        onNavigateToGoal = onNavigateToGoal,
                        onNavigateToInstallment = onNavigateToInstallment,
                        onNavigateToCheck = onNavigateToCheck,
                        onNavigateToFixedExpense = onNavigateToFixedExpense,
                        onNavigateToAIAdvisor = onNavigateToAIAdvisor,
                        onNavigateToAssets = onNavigateToAssets,
                        onNavigateToShopping = onNavigateToShopping,
                        onNavigateToNotes = onNavigateToNotes,
                        onNavigateToAchievements = onNavigateToAchievements
                    )
                    Spacer(Modifier.height(space.large))
                }
            }
        }

        // The global add-transaction FAB lives in the bottom navigation bar; the dashboard
        // no longer draws its own to avoid two overlapping add buttons.

        if (state.showAddTransaction) {
            AddTransactionBottomSheet(
                transactionWithRelations = state.transactionWithRelations,
                template = repeatTemplate,
                initialType = state.initialTransactionType,
                smsDraft = state.smsDraft,
                onDismiss = {
                    repeatTemplate = null
                    viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet())
                },
                transactionAdded = {
                    repeatTemplate = null
                    viewModel.onIntent(DashboardIntent.AnimationEnabled)
                },
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

        if (state.showCustomizeSheet) {
            DashboardCustomizeSheet(
                items = state.dashboardWidgets,
                onApply = { viewModel.onIntent(DashboardIntent.SetWidgetLayout(it)) },
                onDismiss = { viewModel.onIntent(DashboardIntent.ToggleCustomizeSheet) }
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
private fun DashboardWidgetContent(
    widget: DashboardWidget,
    state: DashboardState,
    onEditTransaction: (com.kazemieh.common.model.TransactionWithRelations) -> Unit,
    onDeleteTransaction: (com.kazemieh.common.model.TransactionWithRelations) -> Unit,
    onRepeatTransaction: (com.kazemieh.common.model.TransactionWithRelations) -> Unit,
    onNavigateToTransactions: (Any?) -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToGoal: () -> Unit,
    onNavigateToInstallment: () -> Unit,
    onNavigateToCheck: () -> Unit,
    onNavigateToFixedExpense: () -> Unit,
    onNavigateToAIAdvisor: () -> Unit,
    onNavigateToAssets: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToAchievements: () -> Unit
) {
    val padding = Modifier.padding(horizontal = LocalSpacing.current.large)
    when (widget) {
        DashboardWidget.RECENT_TRANSACTIONS -> RecentTransactionsWidget(
            onMore = { onNavigateToTransactions(true) },
            onEdit = onEditTransaction,
            onDelete = onDeleteTransaction,
            onRepeat = onRepeatTransaction,
            modifier = padding
        )

        DashboardWidget.ACHIEVEMENTS -> AchievementWidget(
            streak = state.streak,
            achievements = state.achievements,
            onMore = onNavigateToAchievements,
            modifier = padding
        )

        DashboardWidget.BUDGET -> BudgetWidget(onMore = onNavigateToBudget, modifier = padding)
        DashboardWidget.GOAL -> GoalWidget(onMore = onNavigateToGoal, modifier = padding)
        DashboardWidget.INSTALLMENT -> InstallmentWidget(onMore = onNavigateToInstallment, modifier = padding)
        DashboardWidget.CHECK -> CheckWidget(onMore = onNavigateToCheck, modifier = padding)
        DashboardWidget.FIXED_EXPENSE -> FixedExpenseWidget(onMore = onNavigateToFixedExpense, modifier = padding)
        DashboardWidget.AI_ADVISOR -> AIAdvisorWidget(onMore = onNavigateToAIAdvisor, modifier = padding)
        DashboardWidget.ASSET -> AssetWidget(onMore = onNavigateToAssets, modifier = padding)
        DashboardWidget.SHOPPING -> ShoppingWidget(onMore = onNavigateToShopping, modifier = padding)
        DashboardWidget.NOTES -> NotesWidget(onMore = onNavigateToNotes, modifier = padding)
    }
}

@Composable
private fun DashboardHeader(
    modifier: Modifier = Modifier,
    userName: String,
    userInitial: String,
    level: Int = 1,
    onNavigateToSearch: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCustomizeClick: () -> Unit = {}
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
            HeaderIconButton(icon = Icons.Default.Tune, onClick = onCustomizeClick)
            HeaderIconButton(icon = Icons.Default.Notifications, onClick = onNotificationsClick)
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
