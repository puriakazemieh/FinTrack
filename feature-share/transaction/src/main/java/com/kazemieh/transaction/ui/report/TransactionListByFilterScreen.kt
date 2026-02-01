package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
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
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {},
    viewModel: TransactionReportViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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

    TransactionListByFilterContent(
        lazyPagingItems,
        state.isLoading,
        enableAnimationChart,
        onDelete = onDelete,
        onEdit = onEdit
    )

}

@Composable
fun TransactionListByFilterContent(
    uiTransactionWithRelations: LazyPagingItems<TransactionWithRelations>,
    loading: Boolean,
    enableAnimationChart: Boolean,
    onDelete: (TransactionWithRelations) -> Unit = {},
    onEdit: (TransactionWithRelations) -> Unit = {}
) {
    val space = LocalSpacing.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = space.large, vertical = space.mediumSmall)
    ) {

        item { ShowTransactionReportCard(enableAnimationChart = enableAnimationChart) }

        item { Spacer(Modifier.height(space.mediumSmall)) }

        transactionListContent(uiTransactionWithRelations, loading, onDelete, onEdit)

    }

}


