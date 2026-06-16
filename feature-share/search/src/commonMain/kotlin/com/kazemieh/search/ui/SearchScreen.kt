package com.kazemieh.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.SearchBar
import com.kazemieh.transaction.ui.add.AddTransactionBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    onBack: () -> Unit,
    onNavigateToCategory: (Category) -> Unit = {},
    onNavigateToSource: (Source) -> Unit = {},
    onNavigateToPerson: (Person) -> Unit = {},
    onNavigateToTag: (Tag) -> Unit = {},
    onNavigateToTransactionType: (com.kazemieh.common.model.TransactionType) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(SearchIntent.ClearQuery(""))
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.GoBack -> onBack()
                is SearchEffect.NavigateToTransaction -> {
                    viewModel.onIntent(SearchIntent.ShowTransactionBottomSheet(effect.transaction))
                }
                is SearchEffect.NavigateToCategory -> onNavigateToCategory(effect.category)
                is SearchEffect.NavigateToSource -> onNavigateToSource(effect.source)
                is SearchEffect.NavigateToPerson -> onNavigateToPerson(effect.person)
                is SearchEffect.NavigateToTag -> onNavigateToTag(effect.tag)
                is SearchEffect.NavigateToTransactionType -> onNavigateToTransactionType(effect.type)
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = space.medium, vertical = space.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                SearchBar(
                    query = state.query,
                    onQueryChange = { viewModel.onIntent(SearchIntent.UpdateQuery(it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.query.isEmpty()) {
                SearchEmptyState(
                    recentSearches = state.recentSearches,
                    quickFilters = state.quickFilters,
                    mostUsedCategories = state.mostUsedCategories,
                    mostUsedSources = state.mostUsedSources,
                    mostUsedPersons = state.mostUsedPersons,
                    mostUsedTags = state.mostUsedTags,
                    onRecentClick = { viewModel.onIntent(SearchIntent.SelectRecentSearch(it)) },
                    onDeleteRecent = { viewModel.onIntent(SearchIntent.DeleteRecentSearch(it)) },
                    onQuickFilterClick = { viewModel.onIntent(SearchIntent.SelectQuickFilter(it)) },
                    onCategoryClick = onNavigateToCategory,
                    onSourceClick = onNavigateToSource,
                    onPersonClick = onNavigateToPerson,
                    onTagClick = onNavigateToTag
                )
            } else {
                SearchResultList(
                    query = state.query,
                    transactions = state.transactions,
                    categories = state.categories,
                    sources = state.sources,
                    persons = state.persons,
                    tags = state.tags,
                    onTransactionClick = { tx ->
                        viewModel.onIntent(
                            SearchIntent.SelectRecentSearch(
                                tx.transaction.description ?: ""
                            )
                        )
                        viewModel.onIntent(SearchIntent.ShowTransactionBottomSheet(tx))
                    },
                    onCategoryClick = { cat ->
                        viewModel.onIntent(SearchIntent.SelectRecentSearch(cat.name))
                        onNavigateToCategory(cat)
                    },
                    onSourceClick = { src ->
                        viewModel.onIntent(SearchIntent.SelectRecentSearch(src.name))
                        onNavigateToSource(src)
                    },
                    onPersonClick = { p ->
                        viewModel.onIntent(SearchIntent.SelectRecentSearch(p.name))
                        onNavigateToPerson(p)
                    },
                    onTagClick = { t ->
                        viewModel.onIntent(SearchIntent.SelectRecentSearch(t.name))
                        onNavigateToTag(t)
                    }
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        if (state.showAddTransaction) {
            AddTransactionBottomSheet(
                transactionWithRelations = state.transactionWithRelations,
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(SearchIntent.ShowTransactionBottomSheet()) },
                transactionAdded = { viewModel.onIntent(SearchIntent.ShowTransactionBottomSheet()) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchEmptyState(
    recentSearches: List<String>,
    quickFilters: List<QuickFilter>,
    mostUsedCategories: List<Category>,
    mostUsedSources: List<Source>,
    mostUsedPersons: List<Person>,
    mostUsedTags: List<Tag>,
    onRecentClick: (String) -> Unit,
    onDeleteRecent: (String) -> Unit,
    onQuickFilterClick: (QuickFilter) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onSourceClick: (Source) -> Unit,
    onPersonClick: (Person) -> Unit,
    onTagClick: (Tag) -> Unit
) {
    val space = LocalSpacing.current
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = space.large)) {
        if (recentSearches.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(space.large))
                FintrackTitleMediumText(text = stringResource(Res.string.title_recent_searches), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(space.medium))
            }
            items(recentSearches) { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecentClick(search) }
                        .padding(vertical = space.small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = GlassText3,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(space.small))
                        FintrackBodyMediumText(text = search)
                    }
                    IconButton(onClick = { onDeleteRecent(search) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(space.large))
            FintrackTitleMediumText(text = stringResource(Res.string.title_quick_filters), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(space.medium))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space.small),
                verticalArrangement = Arrangement.spacedBy(space.small)
            ) {
                quickFilters.forEach { filter ->
                    val label = when (filter) {
                        QuickFilter.INCOME -> stringResource(Res.string.incoming)
                        QuickFilter.EXPENSE -> stringResource(Res.string.outcoming)
                        QuickFilter.TRANSFER -> stringResource(Res.string.transfer)
                        QuickFilter.RECENT_TRANSACTIONS -> stringResource(Res.string.recent_transactions)
                    }
                    Chip(onClick = { onQuickFilterClick(filter) }) {
                        FintrackLabelMediumText(text = label)
                    }
                }

                mostUsedCategories.forEach { category ->
                    Chip(onClick = { onCategoryClick(category) }) {
                        FintrackLabelMediumText(text = category.name)
                    }
                }

                mostUsedSources.forEach { source ->
                    Chip(onClick = { onSourceClick(source) }) {
                        FintrackLabelMediumText(text = source.name)
                    }
                }

                mostUsedPersons.forEach { person ->
                    Chip(onClick = { onPersonClick(person) }) {
                        FintrackLabelMediumText(text = person.name)
                    }
                }

                mostUsedTags.forEach { tag ->
                    Chip(onClick = { onTagClick(tag) }) {
                        FintrackLabelMediumText(text = tag.name)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(space.large)) }
    }
}

@Composable
private fun SearchResultList(
    query: String,
    transactions: List<TransactionWithRelations>,
    categories: List<Category>,
    sources: List<Source>,
    persons: List<Person>,
    tags: List<Tag>,
    onTransactionClick: (TransactionWithRelations) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onSourceClick: (Source) -> Unit,
    onPersonClick: (Person) -> Unit,
    onTagClick: (Tag) -> Unit
) {
    val space = LocalSpacing.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (transactions.isNotEmpty()) {
            items(transactions) { tx ->
                ResultItem(
                    type = stringResource(Res.string.transaction),
                    text = tx.transaction.description ?: "",
                    query = query,
                    onClick = { onTransactionClick(tx) }
                )
            }
        }
        if (categories.isNotEmpty()) {
            items(categories) { cat ->
                ResultItem(
                    type = stringResource(Res.string.category),
                    text = cat.name,
                    query = query,
                    onClick = { onCategoryClick(cat) }
                )
            }
        }
        if (sources.isNotEmpty()) {
            items(sources) { src ->
                ResultItem(
                    type = stringResource(Res.string.source),
                    text = src.name,
                    query = query,
                    onClick = { onSourceClick(src) }
                )
            }
        }
        if (persons.isNotEmpty()) {
            items(persons) { p ->
                ResultItem(
                    type = stringResource(Res.string.person_name),
                    text = p.name,
                    query = query,
                    onClick = { onPersonClick(p) }
                )
            }
        }
        if (tags.isNotEmpty()) {
            items(tags) { t ->
                ResultItem(
                    type = stringResource(Res.string.tag_name),
                    text = t.name,
                    query = query,
                    onClick = { onTagClick(t) }
                )
            }
        }
    }
}


@Composable
private fun ResultItem(type: String, text: String, query: String, onClick: () -> Unit) {
    val space = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = space.large, vertical = space.medium)
    ) {
        FintrackBodyMediumText(text = highlightText(type, text, query))
    }
}

@Composable
private fun highlightText(type: String, text: String, query: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("$type: ")
        }

        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var start = 0
        while (true) {
            val index = if (query.isEmpty()) -1 else lowerText.indexOf(lowerQuery, start)
            if (index == -1) {
                append(text.substring(start))
                break
            }
            append(text.substring(start, index))
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
        }
    }
}
