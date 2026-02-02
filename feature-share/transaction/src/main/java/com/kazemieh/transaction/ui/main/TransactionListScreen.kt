package com.kazemieh.transaction.ui.main

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.paging.compose.collectAsLazyPagingItems
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.transaction.ui.component.transactionListContent
import org.koin.androidx.compose.koinViewModel


@Composable
fun rememberTransactionItemsProvider(
    viewModel: TransactionViewModel = koinViewModel(),
    onEdit: (TransactionWithRelations) -> Unit = {},
    onDelete: (TransactionWithRelations) -> Unit = {},
): LazyListScope.() -> Unit {
    val pagingItems = viewModel.uiTransactionWithRelations.collectAsLazyPagingItems()

    val onEditState = rememberUpdatedState(onEdit)
    val onDeleteState = rememberUpdatedState(onDelete)

    return remember(pagingItems) {
        {
            transactionListContent(
                lazyPagingItems = pagingItems,
                onEdit = { onEditState.value(it) },
                onDelete = { onDeleteState.value(it) }
            )
        }
    }
}
