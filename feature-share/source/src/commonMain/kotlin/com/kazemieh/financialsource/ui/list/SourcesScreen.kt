package com.kazemieh.financialsource.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.SummaryHeader
import com.kazemieh.designsystem.component.model.UiText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Source
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.LocalHideBalance
import com.kazemieh.designsystem.LocalGlassColors
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
    onNavigateToDetail: ((Source) -> Unit)? = null,
    viewModel: SourceViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) { viewModel.onIntent(SourceIntent.LoadAllSource) }
    val state by viewModel.state.collectAsStateWithLifecycle()
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
            SourceBalancesHeader(state.balancesByCurrency)
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
                        badge = stringResource(Res.string.label_amount_with_unit,
                            source.balance.toSignedPersianPrice(),
                            sourceCurrency(source.currencyCode).symbol),
                        color = when {
                            source.balance < 0 -> MaterialTheme.colorScheme.error
                            source.balance == 0L -> LocalGlassColors.current.text2
                            else -> GlassGreen
                        },
                        iconId = source.iconId,
                        colorId = source.colorId,
                        isDefault = source.isDefault
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
                onSetDefaultClick = { item ->
                    state.sources.find { it.id == item.id }?.let {
                        viewModel.onIntent(SourceIntent.SetDefaultSource(it))
                    }
                },
                onDeleteClick = { item ->
                    state.sources.find { it.id == item.id }?.let {
                        viewModel.onIntent(SourceIntent.OnDeleteClick(it))
                    }
                },
                onItemClick = { item ->
                    state.sources.find { it.id == item.id }?.let {
                        onNavigateToDetail?.invoke(it)
                    }
                }
            )
        }

        if (state.isAddShow) {
            AddSourceBottomSheet(
                selectedSource = state.selectedSources,
                onDismiss = { viewModel.onIntent(SourceIntent.ResetFlags) },
                onNavigateToTransactions = onNavigateToTransactions,
                // Management closes the form; only a picker publishes a selected source.
                setSource = { viewModel.onIntent(SourceIntent.ResetFlags) }
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

@Composable
private fun SourceBalancesHeader(balances: List<SourceCurrencyBalance>) {
    if (balances.isEmpty()) return
    val hidden = LocalHideBalance.current
    LazyRow {
        items(balances, key = { it.currency.code }) { total ->
            Box(Modifier.width(260.dp)) {
                SummaryHeader(listOf(EntitySummary(
                    label = UiText.StringResourceText(Res.string.source_balance_total_label, listOf(total.currency.code)),
                    value = total.formattedBalance(hidden)
                        ?: stringResource(Res.string.source_balance_out_of_range),
                    unit = total.currency.symbol,
                    color = when {
                        hidden -> LocalGlassColors.current.text2
                        total.balance == null -> MaterialTheme.colorScheme.error
                        total.balance < 0 -> MaterialTheme.colorScheme.error
                        total.balance == 0L -> LocalGlassColors.current.text2
                        else -> GlassGreen
                    }
                )))
            }
        }
    }
}
