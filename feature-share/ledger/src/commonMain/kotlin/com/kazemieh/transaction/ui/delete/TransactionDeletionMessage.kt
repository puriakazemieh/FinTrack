package com.kazemieh.transaction.ui.delete

import androidx.compose.runtime.Composable
import com.kazemieh.common.model.Transaction
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.LocalCalendarSystem
import com.kazemieh.designsystem.formatCalendarDate
import com.kazemieh.money.Currency
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Describe the saved record being deleted, never an unsaved edit of that record. */
@Composable
internal fun transactionDeletionMessage(
    transaction: Transaction,
    categoryName: String? = null,
    sourceName: String? = null,
    destinationName: String? = null
): String {
    val unit = Currency.valueOf(transaction.currencyCode).symbol
    val amount = stringResource(Res.string.label_amount_with_unit, transaction.amount.toPersianPrice(), unit)
    val type = stringResource(when (transaction.type) {
        TransactionType.INCOME -> Res.string.income
        TransactionType.EXPENSE -> Res.string.payment
        TransactionType.TRANSFER -> Res.string.transfer
        TransactionType.ALL -> Res.string.transaction
    })
    val date = formatCalendarDate(transaction.timeStamp, LocalCalendarSystem.current)
    val summary = if (categoryName != null && sourceName != null) {
        val source = if (transaction.type == TransactionType.TRANSFER && destinationName != null)
            stringResource(Res.string.transfer_from_to_label, sourceName, destinationName) else sourceName
        stringResource(Res.string.delete_transaction_summary, type, amount, categoryName, source, date)
    } else {
        listOf(stringResource(Res.string.label_transaction_net_summary,
            transaction.amount.toPersianPrice(), unit, type), date).joinToString("\n")
    }
    return listOfNotNull(summary, transaction.description?.trim()?.takeIf { it.isNotEmpty() }).joinToString("\n")
}
