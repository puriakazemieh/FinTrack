package com.kazemieh.financialsource.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.ReorderableEntityScaffold
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.financialsource.ui.delete.DeleteSourceBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SourcesScreen(
    onBack: () -> Unit,
    onNavigateToTransactions: ((Source) -> Unit)? = null,
    viewModel: SourceViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) { viewModel.onIntent(SourceIntent.LoadAllSource) }
    val state by viewModel.state.collectAsState()
    var reorderedList by remember(state.sources) { mutableStateOf(state.sources) }

    ReorderableEntityScaffold(
        title = stringResource(Res.string.source),
        sub = stringResource(Res.string.title_source_management),
        onBack = onBack,
        isReorderMode = state.isReorderShow,
        onStartReorder = { viewModel.onIntent(SourceIntent.OnToggleReorder) },
        onConfirmReorder = {
            val positions = reorderedList.mapIndexed { index, source ->
                source.id!! to index
            }.toMap()
            viewModel.onIntent(SourceIntent.UpdatePositions(positions))
            viewModel.onIntent(SourceIntent.OnToggleReorder)
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.source),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(SourceIntent.UpdateSearchQuery(it)) },
                showActions = !state.isReorderShow,
                isReorderMode = state.isReorderShow,
                onMove = { from, to ->
                    reorderedList = reorderedList.toMutableList().apply {
                        add(to, removeAt(from))
                    }
                },
                items = reorderedList.filter {
                    it.name.contains(state.searchQuery, ignoreCase = true) ||
                            it.description?.contains(state.searchQuery, ignoreCase = true) == true
                }.map { source ->
                    EntityItem(
                        id = source.id ?: 0L,
                        name = source.name,
                        sub = if (source.type == 1) source.cardNumber else source.description,
                        badge = source.formattedBalance + " " + stringResource(Res.string.currency_toman),
                        iconId = source.iconId,
                        colorId = source.colorId
                    )
                },
                onAddClick = { viewModel.onIntent(SourceIntent.OnAddSourceClick) },
                onFilterClick = onNavigateToTransactions?.let { callback ->
                    { item ->
                        state.sources.find { it.id == item.id }?.let { callback(it) }
                    }
                },
                onEditClick = { item ->
                    state.sources.find { it.id == item.id }?.let {
                        viewModel.onIntent(SourceIntent.OnEditClick(it))
                    }
                },
                onDeleteClick = { item ->
                    state.sources.find { it.id == item.id }?.let {
                        viewModel.onIntent(SourceIntent.OnDeleteClick(it))
                    }
                }
            )
        }

        if (state.isAddShow) {
            AddSourceBottomSheet(
                selectedSource = state.selectedSources,
                onDismiss = { viewModel.onIntent(SourceIntent.ResetFlags) },
                onNavigateToTransactions = onNavigateToTransactions,
                setSource = { viewModel.onIntent(SourceIntent.SelectedSource(it)) }
            )
        }

        if (state.isDeleteShow && state.selectedSources != null) {
            DeleteSourceBottomSheet(
                source = state.selectedSources!!,
                onDismiss = { viewModel.onIntent(SourceIntent.ResetFlags) },
                deleted = { viewModel.onIntent(SourceIntent.ResetFlags) }
            )
        }
    }
}
