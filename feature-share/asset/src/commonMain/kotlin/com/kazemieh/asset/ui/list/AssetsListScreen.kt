package com.kazemieh.asset.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.asset.ui.component.AssetActionsSheet
import com.kazemieh.asset.ui.component.AssetHistorySheet
import com.kazemieh.asset.ui.component.StocksPortfolio
import com.kazemieh.common.model.Asset
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.component.glass.EntityItem
import com.kazemieh.designsystem.component.glass.EntityList
import com.kazemieh.designsystem.component.glass.EntitySummary
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.model.UiText
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.currency_toman
import fintrack.core.designsystem.generated.resources.label_percentage_value
import fintrack.core.designsystem.generated.resources.label_retry
import fintrack.core.designsystem.generated.resources.label_total_assets_value
import fintrack.core.designsystem.generated.resources.label_units_count
import fintrack.core.designsystem.generated.resources.title_assets_management
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AssetsListScreen(
    onAddAsset: () -> Unit,
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
            EntityList(
                title = stringResource(Res.string.title_assets_management),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(AssetIntent.UpdateSearchQuery(it)) },
                onAddClick = onAddAsset,
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
                onEditClick = { /* TODO */ },
                onDeleteClick = { /* TODO */ },
                showActions = true
            )
        }
    }

    selectedAssetForActions?.let { asset ->
        AssetActionsSheet(
            asset = asset,
            onDismiss = { selectedAssetForActions = null },
            onEdit = { /* TODO: Implement Edit */ },
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
