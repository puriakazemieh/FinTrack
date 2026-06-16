package com.kazemieh.asset.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.kazemieh.designsystem.component.*
import fintrack.core.designsystem.generated.resources.*
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { FintrackTitleLargeText(stringResource(Res.string.title_assets_management)) },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(AssetIntent.SyncRates) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.label_retry)) // Using existing retry label for sync description
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAsset) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.title_add_asset))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
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
                FintrackBodySmallText(stringResource(Res.string.label_units_count, asset.quantity.toString()))
            }
            Column(horizontalAlignment = Alignment.End) {
                FintrackBodyLargeText(asset.totalCurrentValue.toSignedPersianPrice())
                FintrackBodyMediumText(
                    text = stringResource(Res.string.label_percentage_value, asset.profitOrLossPercentage),
                    color = if (asset.profitOrLoss >= 0) Color.Green else Color.Red
                )
            }
        }
    }
}
