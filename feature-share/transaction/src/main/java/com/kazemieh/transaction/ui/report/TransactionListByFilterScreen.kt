package com.kazemieh.transaction.ui.report

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    val lazyPagingItems = viewModel.uiTransactionWithRelations.collectAsLazyPagingItems()

    TransactionListByFilterContent(
        lazyPagingItems,
        enableAnimationChart,
        onDelete = onDelete,
        onEdit = onEdit
    )

}

@Composable
fun TransactionListByFilterContent(
    uiTransactionWithRelations: LazyPagingItems<TransactionWithRelations>,
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

        transactionListContent(uiTransactionWithRelations, onDelete, onEdit)

    }

}


