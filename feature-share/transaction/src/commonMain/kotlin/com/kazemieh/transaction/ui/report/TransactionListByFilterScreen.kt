package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.transaction.ui.component.transactionListContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListByFilterScreen(
    selectedSources: Set<Source>,
    selectedCategories: Set<Category>,
    selectedTags: Set<Tag>,
    selectedPersons: Set<Person>,
    selectedTransactionType: TransactionType,
    fromTimestamp: Long? = null,
    toTimestamp: Long? = null,
    enableAnimationChart: Boolean = true,
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {},
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    LaunchedEffect(
        selectedSources, selectedCategories, selectedTags, selectedPersons,
        selectedTransactionType, fromTimestamp, toTimestamp
    ) {
        viewModel.onIntent(
            TransactionReportIntent.SetFilters(
                sources = selectedSources,
                categories = selectedCategories,
                tags = selectedTags,
                persons = selectedPersons,
                type = selectedTransactionType,
                fromTimestamp = fromTimestamp,
                toTimestamp = toTimestamp
            )
        )
    }

    val state = viewModel.state.collectAsStateWithLifecycle().value
    val listState = rememberLazyListState()

    TransactionListByFilterContent(
        state = state,
        listState = listState,
        enableAnimationChart = enableAnimationChart,
        onDelete = onDelete,
        onEdit = onEdit,
        onLoadMore = { viewModel.onIntent(TransactionReportIntent.LoadNextPage) },
        onRetryRefresh = { viewModel.onIntent(TransactionReportIntent.RetryRefresh) },
        onRetryAppend = { viewModel.onIntent(TransactionReportIntent.RetryAppend) }
    )
}


@Composable
fun TransactionListByFilterContent(
    state: TransactionReportState,
    listState: LazyListState,
    enableAnimationChart: Boolean,
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {},
    onLoadMore: () -> Unit,
    onRetryRefresh: () -> Unit,
    onRetryAppend: () -> Unit,
) {
    val space = LocalSpacing.current

    LaunchedEffect(state.items.size, state.endReached, state.isAppending, state.isRefreshing) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastVisible ->
                val shouldLoadMore = lastVisible >= (state.items.size - 5).coerceAtLeast(0)
                if (shouldLoadMore && !state.endReached && !state.isAppending && !state.isRefreshing) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = space.large, vertical = space.mediumSmall)
    ) {
        item { ShowTransactionReportCard(enableAnimationChart = enableAnimationChart) }
        item { Spacer(Modifier.height(space.mediumSmall)) }

        transactionListContent(
            state = state.toListState(),
            onDelete = onDelete,
            onEdit = onEdit,
            onRetryRefresh = onRetryRefresh,
            onRetryAppend = onRetryAppend
        )
    }
}