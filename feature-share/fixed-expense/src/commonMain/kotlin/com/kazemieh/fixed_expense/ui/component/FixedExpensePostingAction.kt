package com.kazemieh.fixed_expense.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.util.DateUtils
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.MoneyText
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.component.model.asString
import com.kazemieh.domain.usecase.FixedExpensePostingStatus
import com.kazemieh.fixed_expense.ui.list.FixedExpensePostingCommand
import com.kazemieh.money.Currency
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** The same period, feedback and financial action on list and home. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FixedExpensePostingAction(
    status: FixedExpensePostingStatus?,
    busy: Boolean,
    enabled: Boolean,
    statusFailed: Boolean,
    feedback: UiText?,
    failed: Boolean,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(Modifier.widthIn(max = 190.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FintrackLabelSmallText(text = when {
                    statusFailed -> stringResource(Res.string.fixed_expense_posting_status_failed)
                    status == null -> stringResource(Res.string.fixed_expense_posting_loading)
                    else -> stringResource(Res.string.fixed_expense_posting_period, DateUtils.formatDate(status.periodDueDate))
                })
                if (status != null) {
                    FintrackLabelSmallText(text = if (status.hasPrimaryPosting)
                        stringResource(Res.string.fixed_expense_posting_count, status.postingCount.toString().toPersianDigits())
                        else stringResource(Res.string.fixed_expense_not_posted))
                }
            }
            OutlinedButton(
                onClick = onRegister,
                enabled = enabled && status?.canRegister == true && !statusFailed && !busy,
                modifier = Modifier.heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (busy) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                FintrackLabelMediumText(text = stringResource(when {
                    busy -> Res.string.fixed_expense_posting_busy
                    status?.hasPrimaryPosting == true -> Res.string.fixed_expense_repeat_action
                    else -> Res.string.action_register_transaction
                }))
            }
        }
        feedback?.let { FintrackInlineFeedback(text = it.asString(), isError = failed) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedExpenseRepeatConfirmation(
    command: FixedExpensePostingCommand,
    busy: Boolean,
    error: UiText?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Reuse the app's compact, scrollable confirmation shell and parallel 48dp actions.
    DeleteBottomSheet(
        title = stringResource(Res.string.fixed_expense_repeat_title),
        confirmButtonText = stringResource(Res.string.fixed_expense_repeat_action),
        confirmButtonColors = ButtonDefaults.buttonColors(),
        dismissClicked = onDismiss,
        confirmClicked = onConfirm,
        isLoading = busy,
        message = stringResource(Res.string.fixed_expense_repeat_message),
        errorText = error?.asString(),
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                FintrackTitleMediumText(text = command.expense.title, maxLines = Int.MAX_VALUE)
                MoneyText(amount = command.expense.amount, currency = Currency.valueOf(command.expense.currencyCode))
                FintrackBodyMediumText(text = stringResource(Res.string.fixed_expense_posting_period,
                    DateUtils.formatDate(command.periodDueDate)))
            }
        }
    )
}
