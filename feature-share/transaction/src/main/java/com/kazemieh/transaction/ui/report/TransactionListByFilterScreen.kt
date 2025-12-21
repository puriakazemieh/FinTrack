package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
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

    TransactionListByFilterContent(lazyPagingItems, state.isLoading, enableAnimationChart) {
        viewModel.onIntent(
            TransactionReportIntent.DeleteTransactionReport(it.transaction)
        )
    }


}

@Composable
fun TransactionListByFilterContent(
    uiTransactionWithRelations: LazyPagingItems<TransactionWithRelations>,
    loading: Boolean,
    enableAnimationChart: Boolean,
    onDelete: (TransactionWithRelations) -> Unit = {}
) {
    val space = LocalSpacing.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = space.large)
    ) {
        item { Spacer(Modifier.height(space.mediumSmall)) }

        item {
            ShowTransactionReportCard(enableAnimationChart = enableAnimationChart)
        }

        item { Spacer(Modifier.height(space.large)) }

        if (loading) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            transactionListContent(uiTransactionWithRelations, onDelete)
        }

        item { Spacer(Modifier.height(space.mediumSmall)) }
    }

}


