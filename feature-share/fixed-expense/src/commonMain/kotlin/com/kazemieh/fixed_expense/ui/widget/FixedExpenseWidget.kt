package com.kazemieh.fixed_expense.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.glass.WidgetCard
import com.kazemieh.designsystem.component.glass.WidgetEmptyState
import com.kazemieh.fixed_expense.ui.list.FixedExpenseListViewModel
import com.kazemieh.fixed_expense.ui.list.FixedExpenseListIntent
import com.kazemieh.fixed_expense.ui.list.FixedExpenseListEffect
import com.kazemieh.fixed_expense.ui.component.FixedExpensePostingAction
import com.kazemieh.fixed_expense.ui.component.FixedExpenseRepeatConfirmation
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.money.Currency
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FixedExpenseWidget(
    onMore: () -> Unit,
    onMenu: () -> Unit = {},
    onAdd: () -> Unit = {},
    onEdit: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FixedExpenseListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val glassColors = LocalGlassColors.current
    // A posted one-time expense remains visible so its explicit repeat action is reachable.
    val activeExpenses = state.expenses.filter { expense ->
        expense.isActive || state.postingStatuses[expense.id]?.let { status ->
            status.hasPrimaryPosting && state.dateRange?.let { range ->
                status.periodDueDate in range.start..range.end
            } != false
        } == true
    }
    // Soonest due first — the ones the user is about to pay are the most useful to see.
    val upcoming = activeExpenses.sortedBy { it.nextDueDate }

    var showMoreItems by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FixedExpenseListEffect.ShowMessage -> snackbar.showSnackbar(effect.message.resolveString())
            }
        }
    }

    Box(modifier) {
        WidgetCard(
            title = stringResource(Res.string.title_upcoming_fixed_expenses_label),
            count = activeExpenses.size.takeIf { it > 0 },
            onMore = onMore,
            onMenu = onMenu,
            onAdd = onAdd,
            modifier = Modifier
        ) {
            if (activeExpenses.isEmpty()) {
                WidgetEmptyState(
                    icon = Icons.Default.EventRepeat,
                    text = stringResource(Res.string.fixed_expense_empty)
                )
            } else {
                Column {
                    val itemsToShow = if (showMoreItems) 10 else 5
                    upcoming.take(itemsToShow).forEach { expense ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEdit(expense.id) }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    FintrackTitleSmallText(
                                        text = expense.title.takeIf { it.isNotBlank() }
                                            ?: expense.categoryName
                                            ?: stringResource(Res.string.label_unknown_person),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        maxLines = Int.MAX_VALUE
                                    )
                                    FintrackLabelSmallText(
                                        text = stringResource(Res.string.label_next_due, DateUtils.formatDate(expense.nextDueDate)),
                                        color = glassColors.text3,
                                        fontSize = 9.sp
                                    )
                                }
                                MoneyText(
                                    amount = expense.amount,
                                    currency = Currency.valueOf(expense.currencyCode),
                                    size = 11
                                )
                            }
                            FixedExpensePostingAction(
                                status = state.postingStatuses[expense.id],
                                busy = expense.id in state.postingIds,
                                enabled = state.postingIds.isEmpty(),
                                statusFailed = state.postingStatusFailed,
                                feedback = state.postingMessages[expense.id],
                                failed = expense.id in state.postingFailures,
                                onRegister = { viewModel.onIntent(FixedExpenseListIntent.RegisterAsTransaction(expense)) }
                            )
                        }
                    }

                    if (upcoming.size > 5 && !showMoreItems) {
                        TextButton(
                            onClick = { showMoreItems = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(32.dp).align(Alignment.Start)
                        ) {
                            FintrackLabelSmallText(
                                text = stringResource(Res.string.view_all),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        state.repeatConfirmation?.let { command ->
            FixedExpenseRepeatConfirmation(
                command = command,
                busy = command.expense.id in state.postingIds,
                error = state.postingMessages[command.expense.id].takeIf { command.expense.id in state.postingFailures },
                onDismiss = { viewModel.onIntent(FixedExpenseListIntent.DismissRepeat) },
                onConfirm = { viewModel.onIntent(FixedExpenseListIntent.ConfirmRepeat) }
            )
        }
    }
}
