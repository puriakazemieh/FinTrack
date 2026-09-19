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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.kazemieh.designsystem.component.bottomsheet.DeleteBottomSheet
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.GlassAmber
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassPurple
import com.kazemieh.designsystem.GlassRed
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
import com.kazemieh.common.toPersianDigits
import fintrack.core.designsystem.generated.resources.asset_type_gold_extended
import fintrack.core.designsystem.generated.resources.asset_type_fx_physical
import fintrack.core.designsystem.generated.resources.asset_type_crypto
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
            val totalValueStr = state.totalValue.toSignedPersianPrice()
            val currencySymbol = com.kazemieh.designsystem.LocalCurrency.current.symbol
            val totalSummary = remember(state.totalValue, currencySymbol) {
                listOf(
                    EntitySummary(
                        label = UiText.StringResourceText(Res.string.label_total_assets_value),
                        value = totalValueStr,
                        unit = currencySymbol
                    )
                )
            }

            EntityList(
                title = stringResource(Res.string.title_assets_management),
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(AssetIntent.UpdateSearchQuery(it)) },
                onAddClick = { onAddAsset(null) },
                summary = totalSummary,
                items = state.filteredAssets.map { asset ->
                    val displayName = if (asset.name.isNotBlank()) "${asset.name} (${asset.type.getLabel()})" else asset.type.getLabel()
                    EntityItem(
                        id = asset.id ?: 0L,
                        name = displayName,
                        sub = stringResource(Res.string.label_units_count, asset.quantity.toString().toPersianDigits()),
                        badge = stringResource(Res.string.label_percentage_value, asset.profitOrLossPercentage).toPersianDigits(),
                        color = if (asset.profitOrLoss >= 0) GlassGreen else GlassRed,
                        sub2 = asset.totalCurrentValue.toSignedPersianPrice() + " " + currencySymbol
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
                    assetPendingDeletion = state.assets.find { it.id == item.id }
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
            },
            onDelete = { assetPendingDeletion = it }
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
                viewModel.onIntent(AssetIntent.DeleteAsset(asset.id ?: 0L, deleteTransaction = false))
                transactionToDelete = null
            },
            confirmClicked = {
                viewModel.onIntent(AssetIntent.DeleteAsset(asset.id ?: 0L, deleteTransaction = true))
                transactionToDelete = null
            }
        )
    }
}
