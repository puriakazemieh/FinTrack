package com.kazemieh.financialsource.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.financialsource.ui.delete.DeleteSourceBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.source
import fintrack.core.designsystem.generated.resources.title_source_management
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

    FintrackScreen(
        title = stringResource(Res.string.source),
        sub = stringResource(Res.string.title_source_management),
        onClose = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EntityList(
                title = stringResource(Res.string.source),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(SourceIntent.UpdateSearchQuery(it)) },
                showActions = !state.isReorderShow,
                isReorderMode = state.isReorderShow,
                onMove = { from, to ->
                    val list = state.sources.toMutableList()
                    list.add(to, list.removeAt(from))
                    val positions = list.mapIndexed { index, source ->
                        source.id!! to index
                    }.toMap()
                    viewModel.onIntent(SourceIntent.UpdatePositions(positions))
                },
                items = state.filteredSources.map { source ->
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
