package com.kazemieh.asset.ui.list


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetFilter
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.analytics.RefreshTrigger
import com.kazemieh.asset.ui.component.AssetActionsSheet
import com.kazemieh.asset.ui.component.AssetHistorySheet
import com.kazemieh.asset.ui.component.StocksPortfolio
import com.kazemieh.category.ui.list.CategoryFilterSelectionContent
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.aggregateByMarket
import com.kazemieh.common.toPersianDigits
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.SheetFrame
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.financialsource.ui.list.SourceFilterSelectionContent
import com.kazemieh.person.ui.list.PersonFilterSelectionContent
import com.kazemieh.tag.ui.list.TagFilterSelectionContent
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.asset_type_crypto
import fintrack.core.designsystem.generated.resources.asset_type_custom
import fintrack.core.designsystem.generated.resources.asset_type_fx_physical
import fintrack.core.designsystem.generated.resources.asset_type_gold_extended
import fintrack.core.designsystem.generated.resources.asset_type_stock
import fintrack.core.designsystem.generated.resources.btn_clear_all
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.financial_sources
import fintrack.core.designsystem.generated.resources.label_asset_profit_loss_short
import fintrack.core.designsystem.generated.resources.label_percentage
import fintrack.core.designsystem.generated.resources.label_retry
import fintrack.core.designsystem.generated.resources.label_total_assets_value
import fintrack.core.designsystem.generated.resources.label_type
import fintrack.core.designsystem.generated.resources.label_units_count
import fintrack.core.designsystem.generated.resources.persons
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.title_assets_management
import fintrack.core.designsystem.generated.resources.title_quick_filters
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AssetType.getLabel(): String = stringResource(
    when (this) {
        AssetType.GOLD -> Res.string.asset_type_gold_extended
        AssetType.FX -> Res.string.asset_type_fx_physical
        AssetType.STOCK -> Res.string.asset_type_stock
        AssetType.CRYPTO -> Res.string.asset_type_crypto
        AssetType.CUSTOM -> Res.string.asset_type_custom
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsListScreen(
    onAddAsset: (Long?) -> Unit,
    onBack: () -> Unit,
    onNavigateToTransactions: ((String) -> Unit)? = null,
    viewModel: AssetViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedAssetForActions by remember { mutableStateOf<Asset?>(null) }
    var selectedAssetForHistory by remember { mutableStateOf<Asset?>(null) }
    var assetPendingDeletion by remember { mutableStateOf<Asset?>(null) }
    var transactionToDelete by remember { mutableStateOf<Asset?>(null) }
    var showAssetFilter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onIntent(AssetIntent.AssetListOpened)
    }

    FintrackScreen(
        title = stringResource(Res.string.title_assets_management),
        trailingContent = {
            IconButton(onClick = { viewModel.onIntent(AssetIntent.SyncRates(RefreshTrigger.MANUAL)) }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.label_retry)
                )
            }
        },
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val positions = state.assets.aggregateByMarket()
            val totalPurchaseValue = positions.sumOf { it.totalPurchaseValue }
            val totalProfitOrLoss = state.totalValue - totalPurchaseValue
            val totalValueStr = state.totalValue.toPersianPrice()
            val currencySymbol = com.kazemieh.designsystem.LocalCurrency.current.symbol
            val totalSummary = remember(
                state.totalValue,
                totalProfitOrLoss,
                currencySymbol
            ) {
                listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.label_total_assets_value),
                        value = totalValueStr,
                        unit = currencySymbol
                    ),
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.label_asset_profit_loss_short),
                        value = totalProfitOrLoss.toSignedPersianPrice(),
                        unit = currencySymbol,
                        color = if (totalProfitOrLoss >= 0L) GlassGreen else GlassRed
                    )
                )
            }

            val visibleAssets = state.filteredAssets

            EntityList(
                title = stringResource(Res.string.title_assets_management),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(AssetIntent.UpdateSearchQuery(it)) },
                onAddClick = { onAddAsset(null) },
                summary = totalSummary,
                items = visibleAssets.map { asset ->
                    val displayName = asset.name.ifBlank { asset.type.getLabel() }
                    EntityItem(
                        id = asset.id ?: 0L,
                        name = displayName,
                        sub = "${asset.type.getLabel()} · ${
                            stringResource(
                                Res.string.label_units_count,
                                asset.quantity.formatAssetQuantity()
                            )
                        }",
                        badge = stringResource(
                            Res.string.label_percentage,
                            String.format("%.1f", asset.profitOrLossPercentage)
                        ),
                        color = if (asset.profitOrLoss >= 0) GlassGreen else GlassRed,
                        sub2 = "${asset.totalCurrentValue.toPersianPrice()} $currencySymbol"
                    )
                },
                onItemClick = { item ->
                    state.assets.find { it.id == item.id }?.let { asset ->
                        viewModel.onIntent(AssetIntent.ActionsOpened(asset.type))
                        selectedAssetForActions = asset
                    }
                },
                onFilterClick = null,
                onEditClick = { item ->
                    onAddAsset(item.id)
                },
                onDeleteClick = { item ->
                    assetPendingDeletion = state.assets.find { it.id == item.id }
                },
                searchTrailing = {
                    IconButton(onClick = {
                        viewModel.onIntent(AssetIntent.FilterOpened)
                        showAssetFilter = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(Res.string.title_quick_filters),
                            tint = if (state.filter == AssetFilter()) MaterialTheme.colorScheme.onSurfaceVariant else GlassGreen
                        )
                    }
                },
                showActions = false
            )

            StocksPortfolio(assets = positions)
        }
    }

    selectedAssetForActions?.let { asset ->
        AssetActionsSheet(
            asset = asset,
            onDismiss = { selectedAssetForActions = null },
            onEdit = {
                viewModel.onIntent(AssetIntent.ActionSelected("edit"))
                onAddAsset(it.id)
            },
            onViewHistory = {
                viewModel.onIntent(AssetIntent.ActionSelected("view_history"))
                selectedAssetForHistory = it
                selectedAssetForActions = null
            },
            onDelete = {
                viewModel.onIntent(AssetIntent.ActionSelected("delete"))
                assetPendingDeletion = it
            }
        )
    }

    selectedAssetForHistory?.let { asset ->
        AssetHistorySheet(
            asset = asset,
            onDismiss = { selectedAssetForHistory = null }
        )
    }

    assetPendingDeletion?.let { asset ->
        DeleteBottomSheet(
            itemName = asset.name,
            itemType = stringResource(Res.string.title_assets_management),
            dismissClicked = { assetPendingDeletion = null },
            confirmClicked = {
                assetPendingDeletion = null
                transactionToDelete = asset
            }
        )
    }

    transactionToDelete?.let { asset ->
        DeleteBottomSheet(
            title = "پاک کردن تراکنش مرتبط",
            itemName = asset.name,
            itemType = "تراکنش مربوط به دارایی",
            confirmButtonText = "بله، پاک شود",
            dismissButtonText = "خیر، فقط دارایی",
            dismissClicked = {
                viewModel.onIntent(
                    AssetIntent.DeleteAsset(
                        asset.id ?: 0L,
                        deleteTransaction = false
                    )
                )
                transactionToDelete = null
            },
            confirmClicked = {
                viewModel.onIntent(
                    AssetIntent.DeleteAsset(
                        asset.id ?: 0L,
                        deleteTransaction = true
                    )
                )
                transactionToDelete = null
            }
        )
    }

    if (showAssetFilter) {
        AssetFilterBottomSheet(
            filter = state.filter,
            onFilterChanged = { viewModel.onIntent(AssetIntent.UpdateFilter(it)) },
            onDismiss = { showAssetFilter = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AssetFilterBottomSheet(
    filter: AssetFilter,
    onFilterChanged: (AssetFilter) -> Unit,
    onDismiss: () -> Unit
) {
    SheetFrame(
        title = stringResource(Res.string.title_quick_filters),
        sub = stringResource(Res.string.title_assets_management),
        onDismiss = onDismiss,
        horizontalPadding = 20.dp,
        trailingContent = {
            if (filter != AssetFilter()) {
                TextButton(onClick = { onFilterChanged(AssetFilter()) }) {
                    com.kazemieh.designsystem.component.FintrackLabelMediumText(
                        text = stringResource(Res.string.btn_clear_all),
                        color = GlassRed
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AssetFilterSection(title = stringResource(Res.string.label_type)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssetType.entries.forEach { type ->
                        val active = filter.types.isEmpty() || type in filter.types
                        Chip(
                            active = active,
                            color = GlassGreen,
                            onClick = {
                                val current =
                                    if (filter.types.isEmpty()) AssetType.entries.toSet() else filter.types
                                val updated =
                                    if (type in current) current - type else current + type
                                onFilterChanged(filter.copy(types = if (updated.size == AssetType.entries.size) emptySet() else updated))
                            }
                        ) {
                            com.kazemieh.designsystem.component.FintrackLabelMediumText(
                                text = type.getLabel(),
                                color = if (active) com.kazemieh.designsystem.LocalGlassColors.current.bg0 else com.kazemieh.designsystem.LocalGlassColors.current.text
                            )
                        }
                    }
                }
            }

            AssetFilterSection(title = stringResource(Res.string.financial_sources)) {
                SourceFilterSelectionContent(
                    selectedSources = filter.sources,
                    isAllSelected = filter.sources.isEmpty(),
                    onSelectionChanged = { selected, isAll ->
                        onFilterChanged(filter.copy(sources = if (isAll) emptySet() else selected))
                    }
                )
            }

            AssetFilterSection(title = stringResource(Res.string.category)) {
                CategoryFilterSelectionContent(
                    selectedCategories = filter.categories,
                    selectedTransactionType = TransactionType.ALL,
                    isAllSelected = filter.categories.isEmpty(),
                    onSelectionChanged = { selected, isAll ->
                        onFilterChanged(filter.copy(categories = if (isAll) emptySet() else selected))
                    }
                )
            }

            AssetFilterSection(title = stringResource(Res.string.persons)) {
                PersonFilterSelectionContent(
                    selectedPersons = filter.persons,
                    isAllSelected = filter.persons.isEmpty(),
                    onSelectionChanged = { selected, isAll ->
                        onFilterChanged(filter.copy(persons = if (isAll) emptySet() else selected))
                    }
                )
            }

            AssetFilterSection(title = stringResource(Res.string.tags)) {
                TagFilterSelectionContent(
                    selectedTags = filter.tags,
                    isAllSelected = filter.tags.isEmpty(),
                    onSelectionChanged = { selected, isAll ->
                        onFilterChanged(filter.copy(tags = if (isAll) emptySet() else selected))
                    }
                )
            }
        }
    }
}

private fun Double.formatAssetQuantity(): String {
    val rounded =
        if (this % 1.0 == 0.0) toLong().toString() else toString().trimEnd('0').trimEnd('.')
    return rounded.toPersianDigits()
}

@Composable
private fun AssetFilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        com.kazemieh.designsystem.component.FintrackLabelMediumText(title)
        content()
    }
}
