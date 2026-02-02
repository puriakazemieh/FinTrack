package com.kazemieh.transaction.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.EmptyListScreen


fun LazyListScope.transactionListContent(
    lazyPagingItems: LazyPagingItems<TransactionWithRelations>,
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {}
) {
    val refreshState = lazyPagingItems.loadState.refresh
    val appendState = lazyPagingItems.loadState.append
    val isEmpty = lazyPagingItems.itemCount == 0


    if (refreshState is LoadState.Loading && isEmpty) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }


    if (refreshState is LoadState.Error && isEmpty) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${refreshState.error.localizedMessage}")
            }
        }
        return
    }


    if (isEmpty) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyListScreen(stringResource(R.string.transaction))
            }
        }
        return
    }

    items(
        count = lazyPagingItems.itemCount,
        key = lazyPagingItems.itemKey { it.transaction.id },
        contentType = lazyPagingItems.itemContentType { "TransactionItem" },
    ) { index ->
        val item = lazyPagingItems[index] ?: return@items
        TransactionItem(
            uiTransactionWithRelation = item,
            onDelete = { onDelete(item) },
            onEdit = { onEdit(item) }
        )
    }

    if (appendState is LoadState.Loading) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (appendState is LoadState.Error) {
        item {
            Text(text = "Error: ${appendState.error.localizedMessage}")
        }
    }
}
