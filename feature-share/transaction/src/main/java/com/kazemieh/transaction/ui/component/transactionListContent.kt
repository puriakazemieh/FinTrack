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
    loading: Boolean,
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {}
) {

    val isListEmpty = lazyPagingItems.itemCount == 0
    val isNotLoading = lazyPagingItems.loadState.refresh !is LoadState.Loading
    val isError = lazyPagingItems.loadState.refresh is LoadState.Error
    if (loading) {
        item {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
    if (isListEmpty && isNotLoading && !isError) {
        item {
            Box(
                modifier = Modifier.fillParentMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyListScreen(stringResource(R.string.transaction))
            }
        }
    }
    if (!isListEmpty) {
        items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey { it.transaction.id },
            contentType = lazyPagingItems.itemContentType { "MyPagingItems" },
        ) { index ->
            val item = lazyPagingItems[index]
            if (item != null) {
                TransactionItem(
                    uiTransactionWithRelation = item,
                    onDelete = { onDelete(item) },
                    onEdit = { onEdit(item) }
                )
            }
        }
    }

    lazyPagingItems.apply {
        when {
            loadState.refresh is LoadState.Loading -> item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            loadState.append is LoadState.Loading -> item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            loadState.refresh is LoadState.Error -> item {
                val e = lazyPagingItems.loadState.refresh as LoadState.Error
                Text("Error: ${e.error.localizedMessage}")
            }
        }
    }
}