package com.kazemieh.fintrack.dashboard


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.category.ui.CategoryListBottomSheet
import com.kazemieh.category.ui.add.AddCategoryBottomSheet
import com.kazemieh.financialsource.R
import com.kazemieh.financialsource.ui.SourceList
import com.kazemieh.financialsource.ui.SourceListBottomSheet
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.tag.ui.TagListBottomSheet
import com.kazemieh.transaction.ui.main.TotalTransactionCard
import com.kazemieh.transaction.ui.main.TransactionListScreen
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item { TotalTransactionCard() }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                SourceList {
                    viewModel.onIntent(
                        DashboardIntent.ShowAddSource(
                            showAddSource = true,
                            fromSourceList = false
                        )
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item { TransactionListScreen() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.onIntent(DashboardIntent.ShowAddTransaction(true)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.BottomStart),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_source)
                )
            }
        }

        if (state.showAddTransaction) {
            AddTransactionBottomSheet(
                source = state.source,
                category = state.category,
                tags = state.tags,
                onDismiss = { viewModel.onIntent(DashboardIntent.ShowAddTransaction(false)) },
                onSourceClicked = { viewModel.onIntent(DashboardIntent.ShowSourceList(true)) },
                onCategoryClicked = { type ->
                    viewModel.onIntent(
                        DashboardIntent.ShowCategoryList(
                            categoryList = true,
                            selectedTransactionType = type
                        )
                    )
                },
                onTagClicked = { viewModel.onIntent(DashboardIntent.ShowTagList(true)) }
            )
        }

        Source(
            showSourceList = state.showSourceList,
            showAddSource = state.showAddSource,
            fromSourceList = state.fromSourceList,
            onIntent = viewModel::onIntent
        )

        Category(
            showCategoryList = state.showCategoryList,
            showAddCategory = state.showAddCategory,
            selectedTransactionType = state.selectedTransactionType,
            onIntent = viewModel::onIntent
        )

        Tag(
            showTagList = state.showTagList,
            selectedTags = state.tags,
            onIntent = viewModel::onIntent
        )
    }
}


@Composable
fun Source(
    showSourceList: Boolean,
    showAddSource: Boolean,
    fromSourceList: Boolean,
    onIntent: (DashboardIntent) -> Unit
) {
    if (showSourceList) {
        SourceListBottomSheet(
            onAddSourceClick = {
                onIntent(
                    DashboardIntent.ShowAddSource(
                        showAddSource = true,
                        fromSourceList = true
                    )
                )
            },
            onSourceClick = { id, name ->
                onIntent(DashboardIntent.SetSource(id to name))
            },
            onDismiss = {
                onIntent(DashboardIntent.ShowSourceList(false))
            }
        )
    }

    if (showAddSource) {
        AddSourceBottomSheet(
            onDismiss = {
                onIntent(
                    DashboardIntent.ShowAddSource(
                        showAddSource = false,
                        fromSourceList = false
                    )
                )
            },
            setSource = { id, name ->
                if (fromSourceList)
                    onIntent(DashboardIntent.SetSource(id to name))
                else onIntent(
                    DashboardIntent.ShowAddSource(
                        showAddSource = false,
                        fromSourceList = false
                    )
                )
            }
        )
    }
}

@Composable
fun Category(
    showCategoryList: Boolean,
    showAddCategory: Boolean,
    selectedTransactionType: Int,
    onIntent: (DashboardIntent) -> Unit
) {

    if (showCategoryList) {
        CategoryListBottomSheet(
            selectedTransactionType = selectedTransactionType,
            onCategoryClick = { id, name ->
                onIntent(DashboardIntent.SetCategory(id to name))
            },
            onAddCategoryClick = {
                onIntent(DashboardIntent.ShowAddCategory(true))
            },
            onDismiss = {
                onIntent(DashboardIntent.ShowCategoryList(false))
            }
        )
    }

    if (showAddCategory) {
        AddCategoryBottomSheet(
            selectedTransactionType = selectedTransactionType,
            onDismiss = {
                onIntent(DashboardIntent.ShowAddCategory(false))
            },
            setCategory = { id, name ->
                onIntent(DashboardIntent.SetCategory(id to name))
            }
        )
    }
}

@Composable
fun Tag(
    showTagList: Boolean,
    selectedTags: Set<Pair<Int, String>>?,
    onIntent: (DashboardIntent) -> Unit
) {

    if (showTagList) {
        TagListBottomSheet(
            selectedTags = selectedTags,
            onSubmitClick = { selectedTags: Set<Pair<Int, String>>? ->
                onIntent(DashboardIntent.SetAllSelectedTags(selectedTags))
            },
            onDismiss = {
                onIntent(DashboardIntent.ShowTagList(false))
            }
        )
    }
}

