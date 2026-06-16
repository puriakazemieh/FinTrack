package com.kazemieh.asset.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.asset.ui.component.AssetActionsSheet
import com.kazemieh.asset.ui.component.AssetHistorySheet
import com.kazemieh.asset.ui.component.StocksPortfolio
import com.kazemieh.common.model.Asset
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackHeadlineLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.label_percentage_value
import fintrack.core.designsystem.generated.resources.label_retry
import fintrack.core.designsystem.generated.resources.label_total_assets_value
import fintrack.core.designsystem.generated.resources.label_units_count
import fintrack.core.designsystem.generated.resources.title_add_asset
import fintrack.core.designsystem.generated.resources.title_assets_management
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsListScreen(
    onAddAsset: () -> Unit,
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAsset,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.title_add_asset)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TotalValueCard(state.totalValue)
            CompositionBar(state.composition)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StocksPortfolio(state.assets)
                }

                items(state.assets) { asset ->
                    AssetRow(
                        asset = asset,
                        onClick = { selectedAssetForActions = asset }
                    )
                }
            }
        }
    }

    selectedAssetForActions?.let { asset ->
        AssetActionsSheet(
            asset = asset,
            onDismiss = { selectedAssetForActions = null },
            onEdit = { /* TODO: Implement Edit */ },
            onViewHistory = { selectedAssetForHistory = it }
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
fun TotalValueCard(totalValue: Long) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FintrackTitleMediumText(stringResource(Res.string.label_total_assets_value))
            FintrackHeadlineLargeText(totalValue.toSignedPersianPrice())
        }
    }
}

@Composable
fun CompositionBar(composition: Map<*, Double>) {
    // Simplified composition bar
    Row(
        modifier = Modifier.fillMaxWidth().height(12.dp).padding(horizontal = 16.dp)
    ) {
        composition.forEach { (type, percentage) ->
            Box(
                modifier = Modifier.weight(percentage.toFloat().coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color((type.hashCode() * 0xFFFFFF) or 0xFF000000.toInt()))
            )
        }
    }
}

@Composable
fun AssetRow(
    asset: Asset,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FintrackTitleMediumText(asset.name)
                FintrackBodySmallText(
                    stringResource(
                        Res.string.label_units_count,
                        asset.quantity.toString()
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                FintrackBodyLargeText(asset.totalCurrentValue.toSignedPersianPrice())
                FintrackBodyMediumText(
                    text = stringResource(
                        Res.string.label_percentage_value,
                        asset.profitOrLossPercentage
                    ),
                    color = if (asset.profitOrLoss >= 0) Color.Green else Color.Red
                )
            }
        }
    }
}
