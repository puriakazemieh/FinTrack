package com.kazemieh.asset.ui.list


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.asset.ui.component.AssetActionsSheet
import com.kazemieh.asset.ui.component.AssetHistorySheet
import com.kazemieh.asset.ui.component.StocksPortfolio
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassPurple
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.label_percentage_value
import fintrack.core.designsystem.generated.resources.label_retry
import fintrack.core.designsystem.generated.resources.label_total_assets_value
import fintrack.core.designsystem.generated.resources.label_units_count
import fintrack.core.designsystem.generated.resources.title_assets_management
import fintrack.core.designsystem.generated.resources.asset_type_gold
import fintrack.core.designsystem.generated.resources.asset_type_fx
import fintrack.core.designsystem.generated.resources.asset_type_stock
import fintrack.core.designsystem.generated.resources.asset_type_custom
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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

    FintrackScreen(
        title = stringResource(Res.string.title_assets_management),
        trailingContent = {
            IconButton(onClick = { viewModel.onIntent(AssetIntent.SyncRates) }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.label_retry)
                )
            }
        },
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AssetHero(
                totalValue = state.totalValue,
                composition = state.composition
            )

            EntityList(
                title = stringResource(Res.string.title_assets_management),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(AssetIntent.UpdateSearchQuery(it)) },
                onAddClick = { onAddAsset(null) },
                summary = listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.label_total_assets_value),
                        value = state.totalValue.toPersianPrice(),
                        unit = stringResource(Res.string.currency_toman),
                        color = MaterialTheme.colorScheme.primary
                    )
                ),
                items = state.filteredAssets.map { asset ->
                    EntityItem(
                        id = asset.id ?: 0L,
                        name = asset.name,
                        sub = stringResource(Res.string.label_units_count, asset.quantity.toString()),
                        badge = stringResource(Res.string.label_percentage_value, asset.profitOrLossPercentage),
                        color = if (asset.profitOrLoss >= 0) Color.Green else Color.Red,
                        sub2 = asset.totalCurrentValue.toSignedPersianPrice() + " " + stringResource(Res.string.currency_toman)
                    )
                },
                onItemClick = { item ->
                    selectedAssetForActions = state.assets.find { it.id == item.id }
                },
                onFilterClick = onNavigateToTransactions?.let { callback ->
                    { item ->
                        state.assets.find { it.id == item.id }?.let { callback(it.name) }
                    }
                },
                onEditClick = { item ->
                    onAddAsset(item.id)
                },
                onDeleteClick = { item ->
                    viewModel.onIntent(AssetIntent.DeleteAsset(item.id))
                },
                showActions = true
            )
            
            StocksPortfolio(assets = state.assets)
        }
    }

    selectedAssetForActions?.let { asset ->
        AssetActionsSheet(
            asset = asset,
            onDismiss = { selectedAssetForActions = null },
            onEdit = { onAddAsset(it.id) },
            onViewHistory = { 
                selectedAssetForHistory = it
                selectedAssetForActions = null
            }
        )
    }

    selectedAssetForHistory?.let { asset ->
        AssetHistorySheet(
            asset = asset,
            onDismiss = { selectedAssetForHistory = null }
        )
    }
}

@Composable
private fun AssetHero(
    totalValue: Long,
    composition: Map<AssetType, Double>,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.padding(16.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FintrackLabelSmallText(
                text = stringResource(Res.string.label_total_assets_value),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FintrackBodyMediumText(
                text = totalValue.toPersianPrice() + " " + stringResource(Res.string.currency_toman),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            CompositionBar(composition)
        }
    }
}

@Composable
private fun CompositionBar(
    composition: Map<AssetType, Double>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        ) {
            val gold = composition[AssetType.GOLD] ?: 0.0
            val fx = composition[AssetType.FX] ?: 0.0
            val stock = composition[AssetType.STOCK] ?: 0.0
            val custom = composition[AssetType.CUSTOM] ?: 0.0

            if (gold > 0) Box(Modifier.weight(gold.toFloat()).fillMaxSize().background(GlassAmber))
            if (fx > 0) Box(Modifier.weight(fx.toFloat()).fillMaxSize().background(GlassGreen))
            if (stock > 0) Box(Modifier.weight(stock.toFloat()).fillMaxSize().background(GlassBlue))
            if (custom > 0) Box(Modifier.weight(custom.toFloat()).fillMaxSize().background(GlassPurple))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            composition.forEach { (type, percentage) ->
                if (percentage > 0) {
                    val label = when (type) {
                        AssetType.GOLD -> stringResource(Res.string.asset_type_gold)
                        AssetType.FX -> stringResource(Res.string.asset_type_fx)
                        AssetType.STOCK -> stringResource(Res.string.asset_type_stock)
                        AssetType.CUSTOM -> stringResource(Res.string.asset_type_custom)
                    }
                    FintrackLabelSmallText(
                        text = "$label ${(percentage * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
