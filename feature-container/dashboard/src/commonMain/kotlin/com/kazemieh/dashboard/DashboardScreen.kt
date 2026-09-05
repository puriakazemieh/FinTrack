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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.ai_insights.ui.AIAdvisorWidget
import com.kazemieh.asset.ui.component.AssetWidget
import com.kazemieh.budget.ui.component.BudgetWidget
import com.kazemieh.check.ui.add.AddCheckBottomSheet
import com.kazemieh.check.ui.widget.CheckWidget
import com.kazemieh.common.toPersianDigits
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
import com.kazemieh.fixed_expense.ui.detail.AddFixedExpenseBottomSheet
import com.kazemieh.fixed_expense.ui.widget.FixedExpenseWidget
import com.kazemieh.gamification.ui.AchievementWidget
import com.kazemieh.goals.presentation.add.AddGoalBottomSheet
import com.kazemieh.goals.presentation.component.GoalWidget
import com.kazemieh.installment.ui.add.AddInstallmentBottomSheet
import com.kazemieh.installment.ui.widget.InstallmentWidget
import com.kazemieh.notes.ui.NotesWidget
import com.kazemieh.notes.ui.edit.AddNoteBottomSheet
import com.kazemieh.shopping.ui.AddShoppingBottomSheet
import com.kazemieh.shopping.ui.ShoppingWidget
import com.kazemieh.sms_reader.ui.SmsBanner
import com.kazemieh.sms_reader.ui.SmsDetectionSheet
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import com.kazemieh.transaction.ui.delete.DeleteTransactionBottomSheet
import com.kazemieh.transaction.ui.main.BalanceHero
import com.kazemieh.budget.ui.add.AddBudgetBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.msg_hello
import fintrack.core.designsystem.generated.resources.action_delete_all
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    showAddTransaction: Boolean = false,
    smsDraftId: Long = -1L,
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
    var repeatTemplate by remember {
        mutableStateOf<com.kazemieh.common.model.TransactionWithRelations?>(null)
    }

    LaunchedEffect(showAddTransaction, smsDraftId) {
        if (smsDraftId > 0L) {
            viewModel.onIntent(DashboardIntent.OpenSmsDraftTransaction(smsDraftId))
        } else if (showAddTransaction) {
            viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet())
        }
    }


    FintrackScreen {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for NavigationBar
        ) {
            // todo disable
            /*item {
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
            }*/

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

                item { Spacer(Modifier.height(space.mediumSmall)) }
                item {
                    SmsBanner(
                        count = state.smsDrafts.size,
                        onClick = { viewModel.onIntent(DashboardIntent.ToggleSmsDetectionSheet) },
                        modifier = Modifier.padding(
                            horizontal = space.large,
                            vertical = space.small
                        )
                    )
                }
            }

//            item { Spacer(Modifier.height(space.large)) }

//            item {
//                QuickActions(
//                    onActionClick = {
//                        viewModel.onIntent(
//                            DashboardIntent.ShowTransactionBottomSheet(type = it)
//                        )
//                    },
//                    onSearchClick = onNavigateToSearch,
//                    modifier = Modifier.padding(horizontal = space.large)
//                )
//            }

            item { Spacer(Modifier.height(space.mediumSmall)) }

            state.dashboardWidgets.forEach { cfg ->
                if (!cfg.visible) return@forEach
                if (cfg.widget.toolFeature()
                        ?.let { it in state.disabledTools } == true
                ) return@forEach
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
                        onAdd = {
                            when (it) {
                                DashboardWidget.RECENT_TRANSACTIONS -> viewModel.onIntent(DashboardIntent.ShowTransactionBottomSheet())
                                DashboardWidget.BUDGET -> viewModel.onIntent(DashboardIntent.ToggleBudgetSheet())
                                DashboardWidget.NOTES -> viewModel.onIntent(DashboardIntent.ToggleNoteSheet())
                                DashboardWidget.SHOPPING -> viewModel.onIntent(DashboardIntent.ToggleShoppingSheet())
                                DashboardWidget.FIXED_EXPENSE -> viewModel.onIntent(DashboardIntent.ToggleFixedExpenseSheet())
                                DashboardWidget.GOAL -> viewModel.onIntent(DashboardIntent.ToggleGoalSheet())
                                DashboardWidget.INSTALLMENT -> viewModel.onIntent(DashboardIntent.ToggleInstallmentSheet())
                                DashboardWidget.CHECK -> viewModel.onIntent(DashboardIntent.ToggleCheckSheet())
                                else -> {}
                            }
                        },
                        onEditBudget = { budget ->
                            viewModel.onIntent(DashboardIntent.ToggleBudgetSheet(budget))
                        },
                        onEditNote = { note ->
                            viewModel.onIntent(DashboardIntent.ToggleNoteSheet(note))
                        },
                        onEditFixedExpense = { id ->
                            viewModel.onIntent(DashboardIntent.ToggleFixedExpenseSheet(id))
                        },
                        onEditShopping = { item ->
                            viewModel.onIntent(DashboardIntent.ToggleShoppingSheet(item))
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

        if (state.showSmsDetection) {
            SmsDetectionSheet(
                drafts = state.smsDrafts,
                categories = state.categories,
                sources = state.sources,
                mostUsedCategories = state.mostUsedCategories,
                mostUsedSources = state.mostUsedSources,
                currency = state.currency,
                onQuickRegister = { draft ->
                    viewModel.onIntent(DashboardIntent.QuickRegisterSms(draft))
                },
                onEdit = { draft ->
                    viewModel.onIntent(
                        DashboardIntent.ShowTransactionBottomSheet(
                            smsDraft = draft,
                            type = draft.type
                        )
                    )
                },
                onDelete = { draft ->
                    viewModel.onIntent(DashboardIntent.ShowDeleteSmsConfirmation(true, draft))
                },
                onDeleteAll = {
                    viewModel.onIntent(DashboardIntent.ShowDeleteAllSmsConfirmation(true))
                },
                onUpdateDraft = { viewModel.onIntent(DashboardIntent.UpdateSmsDraft(it)) },
                onDismiss = { viewModel.onIntent(DashboardIntent.ToggleSmsDetectionSheet) }
            )
        }

        if (state.showDeleteSmsConfirmation && state.smsDraftToDelete != null) {
            com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet(
                itemName = state.smsDraftToDelete!!.bankName,
                dismissClicked = { viewModel.onIntent(DashboardIntent.ShowDeleteSmsConfirmation(false)) },
                confirmClicked = {
                    viewModel.onIntent(DashboardIntent.IgnoreSmsDraft(state.smsDraftToDelete!!))
                }
            )
        }
        
        if (state.showDeleteAllSmsConfirmation) {
            com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet(
                itemName = stringResource(Res.string.action_delete_all),
                dismissClicked = { viewModel.onIntent(DashboardIntent.ShowDeleteAllSmsConfirmation(false)) },
                confirmClicked = {
                    viewModel.onIntent(DashboardIntent.IgnoreAllSmsDrafts)
                }
            )
        }

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

        if (state.showDeleteTransaction) {
            DeleteTransactionBottomSheet(
                transactionWithRelations = state.transactionWithRelations,
                onDismiss = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet()) },
                transactionDeleted = { viewModel.onIntent(DashboardIntent.DeleteTransactionBottomSheet()) },
            )
        }

        // todo disable
        /*if (state.showCustomizeSheet) {
            DashboardCustomizeSheet(
                items = state.dashboardWidgets,
                onApply = { viewModel.onIntent(DashboardIntent.SetWidgetLayout(it)) },
                onDismiss = { viewModel.onIntent(DashboardIntent.ToggleCustomizeSheet) }
            )
        }*/

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

        if (state.showBudgetSheet) {
            AddBudgetBottomSheet(
                budgetWithProgress = state.selectedBudget,
                onDismiss = {
                    viewModel.onIntent(DashboardIntent.ToggleBudgetSheet())
                    viewModel.onIntent(DashboardIntent.AnimationEnabled)
                }
            )
        }

        if (state.showGoalSheet) {
            AddGoalBottomSheet(
                goal = state.selectedGoal,
                onDismiss = {
                    viewModel.onIntent(DashboardIntent.ToggleGoalSheet())
                    viewModel.onIntent(DashboardIntent.AnimationEnabled)
                }
            )
        }

        if (state.showInstallmentSheet) {
            AddInstallmentBottomSheet(
                installmentId = state.selectedInstallmentId,
                onDismiss = {
                    viewModel.onIntent(DashboardIntent.ToggleInstallmentSheet())
                },
                onSuccess = {
                    viewModel.onIntent(DashboardIntent.AnimationEnabled)
                }
            )
        }

        if (state.showCheckSheet) {
            AddCheckBottomSheet(
                checkId = state.selectedCheckId,
                onDismiss = {
                    viewModel.onIntent(DashboardIntent.ToggleCheckSheet())
                    viewModel.onIntent(DashboardIntent.AnimationEnabled)
                }
            )
        }

        if (state.showFixedExpenseSheet) {
            AddFixedExpenseBottomSheet(
                expenseId = state.selectedFixedExpenseId,
                onDismiss = {
                    viewModel.onIntent(DashboardIntent.ToggleFixedExpenseSheet())
                    viewModel.onIntent(DashboardIntent.AnimationEnabled)
                }
            )
        }

        if (state.showNoteSheet) {
            AddNoteBottomSheet(
                note = state.selectedNote,
                onDismiss = {
                    viewModel.onIntent(DashboardIntent.ToggleNoteSheet())
                    viewModel.onIntent(DashboardIntent.AnimationEnabled)
                }
            )
        }

        if (state.showShoppingSheet) {
            AddShoppingBottomSheet(
                item = state.selectedShoppingItem,
                onDismiss = {
                    viewModel.onIntent(DashboardIntent.ToggleShoppingSheet())
                    viewModel.onIntent(DashboardIntent.AnimationEnabled)
                }
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
    onAdd: (DashboardWidget) -> Unit,
    onEditBudget: (com.kazemieh.common.model.BudgetWithProgress) -> Unit,
    onEditNote: (com.kazemieh.common.model.Note) -> Unit,
    onEditFixedExpense: (Long) -> Unit,
    onEditShopping: (com.kazemieh.common.model.ShoppingItem) -> Unit,
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
            onAdd = { onAdd(DashboardWidget.RECENT_TRANSACTIONS) },
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

        DashboardWidget.BUDGET -> BudgetWidget(
            onMore = onNavigateToBudget,
            onAdd = { onAdd(DashboardWidget.BUDGET) },
            onEditBudget = onEditBudget,
            modifier = padding
        )

        DashboardWidget.GOAL -> GoalWidget(
            onMore = onNavigateToGoal,
            onAdd = { onAdd(DashboardWidget.GOAL) },
            modifier = padding
        )

        DashboardWidget.INSTALLMENT -> InstallmentWidget(
            onMore = onNavigateToInstallment,
            onAdd = { onAdd(DashboardWidget.INSTALLMENT) },
            modifier = padding
        )

        DashboardWidget.CHECK -> CheckWidget(
            onMore = onNavigateToCheck,
            onAdd = { onAdd(DashboardWidget.CHECK) },
            modifier = padding
        )

        DashboardWidget.FIXED_EXPENSE -> FixedExpenseWidget(
            onMore = onNavigateToFixedExpense,
            onAdd = { onAdd(DashboardWidget.FIXED_EXPENSE) },
            onEdit = onEditFixedExpense,
            modifier = padding
        )

        DashboardWidget.AI_ADVISOR -> AIAdvisorWidget(
            onMore = onNavigateToAIAdvisor,
            modifier = padding
        )

        DashboardWidget.ASSET -> AssetWidget(
            onMore = onNavigateToAssets,
            modifier = padding
        )

        DashboardWidget.SHOPPING -> ShoppingWidget(
            onMore = onNavigateToShopping,
            onAdd = { onAdd(DashboardWidget.SHOPPING) },
            onEdit = onEditShopping,
            modifier = padding
        )

        DashboardWidget.NOTES -> NotesWidget(
            onMore = onNavigateToNotes,
            onAdd = { onAdd(DashboardWidget.NOTES) },
            onEditNote = onEditNote,
            modifier = padding
        )
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

        // Search lives in the Quick Actions row and notifications in Settings, so the header
        // keeps only the dashboard-customize button.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderIconButton(icon = Icons.Default.Tune, onClick = onCustomizeClick)
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
