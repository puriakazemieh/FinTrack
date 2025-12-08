package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.kazemieh.common.ld
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.transaction.ui.component.TransactionItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionListByFilterScreen(
    selectedSources: Set<Pair<Int, String>>,
    selectedCategories: Set<Pair<Int, String>>,
    selectedTags: Set<Pair<Int, String>>,
    selectedPersons: Set<Pair<Int, String>>,
    selectedTransactionType: Int,
    fromTimestamp: Long? = null,
    toTimestamp: Long? = null,
    enableAnimationChart: Boolean = true,
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(selectedCategories) {
        viewModel.onIntent(TransactionReportIntent.SelectedCategory(selectedCategories))
    }

    LaunchedEffect(selectedSources) {
        viewModel.onIntent(TransactionReportIntent.SelectedSource(selectedSources))
    }

    LaunchedEffect(selectedTags) {
        viewModel.onIntent(TransactionReportIntent.SelectedTag(selectedTags))
    }

    LaunchedEffect(selectedPersons) {
        viewModel.onIntent(TransactionReportIntent.SelectedPerson(selectedPersons))
    }

    LaunchedEffect(selectedTransactionType) {
        viewModel.onIntent(TransactionReportIntent.SelectedType(selectedTransactionType))
    }

    LaunchedEffect(fromTimestamp, toTimestamp) {
        viewModel.onIntent(TransactionReportIntent.SelectedDate(fromTimestamp, toTimestamp))
    }

    val lazyPagingItems = viewModel.uiTransactionWithRelations.collectAsLazyPagingItems()
    val isListEmpty = lazyPagingItems.itemCount == 0
    val isNotLoading = lazyPagingItems.loadState.refresh !is LoadState.Loading
    val isError = lazyPagingItems.loadState.refresh is LoadState.Error
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            ShowTransactionReportCard(enableAnimationChart = enableAnimationChart)
        }

        item { Spacer(Modifier.height(16.dp)) }
        if (isListEmpty && isNotLoading && !isError) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyListScreen()
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
                        onDelete = {
                            viewModel.onIntent(
                                TransactionReportIntent.DeleteTransactionReport(item.transaction)
                            )
                        }
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

        item { Spacer(Modifier.height(8.dp)) }
    }


}


