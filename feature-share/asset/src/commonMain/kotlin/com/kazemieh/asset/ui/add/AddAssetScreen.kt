package com.kazemieh.asset.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetEffect
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.FintrackScreen
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    assetId: Long? = null,
    onBack: () -> Unit,
    onRegisterTransaction: (Asset, String) -> Unit = { _, _ -> },
    viewModel: AssetViewModel = koinViewModel()
) {
    val assetState by viewModel.state.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.GOLD) }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var marketCode by remember { mutableStateOf("") }
    var selectedMarketRate by remember { mutableStateOf<AssetRate?>(null) }
    var showMarketPicker by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var addedAsset by remember { mutableStateOf<Asset?>(null) }

    LaunchedEffect(assetId) {
        if (assetId != null) {
            viewModel.onIntent(AssetIntent.LoadAsset(assetId))
        }
    }

    LaunchedEffect(assetState.selectedAsset) {
        assetState.selectedAsset?.let { asset ->
            name = asset.name
            type = asset.type
            quantity = asset.quantity.toString()
            purchasePrice = asset.purchasePrice.toString()
            marketCode = asset.marketCode.orEmpty()
            description = asset.description ?: ""
        }
    }

    LaunchedEffect(assetState.selectedAsset?.marketCode, assetState.marketRates) {
        val selectedCode = assetState.selectedAsset?.marketCode ?: return@LaunchedEffect
        selectedMarketRate = assetState.marketRates.find { it.code.equals(selectedCode, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(AssetIntent.SyncRates)
        viewModel.effect.collect { effect ->
            if (effect is AssetEffect.AssetAdded) addedAsset = effect.asset
        }
    }

    FintrackScreen(
        title = stringResource(if (assetId == null) Res.string.title_add_asset else Res.string.title_edit_asset),
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FintrackOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { FintrackBodyMediumText(stringResource(Res.string.label_asset_name_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Asset Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AssetType.entries.forEach { assetType ->
                    FilterChip(
                        selected = type == assetType,
                        onClick = { type = assetType },
                        label = { FintrackLabelMediumText(assetType.label()) }
                    )
                }
            }

            FintrackOutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { FintrackBodyMediumText(stringResource(Res.string.label_quantity)) },
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = purchasePrice,
                onValueChange = { purchasePrice = it },
                label = { FintrackBodyMediumText(stringResource(Res.string.label_purchase_price)) },
                isPrice = true,
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = selectedMarketRate?.name.orEmpty(),
                onValueChange = {},
                onClick = { showMarketPicker = true },
                readOnly = true,
                label = { FintrackBodyMediumText(stringResource(Res.string.label_asset_market_code)) },
                placeholder = { FintrackLabelMediumText(stringResource(Res.string.hint_asset_market_code)) },
                suffix = selectedMarketRate?.let { rate ->
                    { FintrackLabelMediumText(rate.price.toSignedPersianPrice()) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { FintrackBodyMediumText(stringResource(Res.string.description)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            FintrackButton(
                text = stringResource(Res.string.save_),
                onClick = {
                    val asset = Asset(
                        id = assetId,
                        name = name,
                        type = type,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        purchasePrice = purchasePrice.toLongOrNull() ?: 0L,
                        currentPrice = selectedMarketRate?.price,
                        marketCode = marketCode.ifBlank { null },
                        description = description,
                        colorId = 1,
                        iconId = 1
                    )
                    if (assetId == null) {
                        viewModel.onIntent(AssetIntent.AddAsset(asset))
                    } else {
                        viewModel.onIntent(AssetIntent.UpdateAsset(asset))
                        onBack()
                    }
                },
                enabled = name.isNotBlank() && (quantity.toDoubleOrNull() ?: 0.0) > 0.0 &&
                    (purchasePrice.toLongOrNull() ?: 0L) > 0L,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showMarketPicker) {
        MarketRatePickerSheet(
            type = type,
            rates = assetState.marketRates,
            isLoading = assetState.isLoading,
            onSelect = { rate ->
                selectedMarketRate = rate
                type = rate.type
                name = rate.name
                marketCode = rate.code
                if (purchasePrice.isBlank()) purchasePrice = rate.price.toString()
                viewModel.onIntent(AssetIntent.MarketRateSelected(rate))
                showMarketPicker = false
            },
            onRefresh = { viewModel.onIntent(AssetIntent.SyncRates) },
            onDismiss = { showMarketPicker = false }
        )
    }

    addedAsset?.let { asset ->
        val transactionDescription = stringResource(Res.string.asset_purchase_transaction_description, asset.name)
        AlertDialog(
            onDismissRequest = {
                addedAsset = null
                viewModel.onIntent(AssetIntent.AssetTransactionPromptAnswered(false))
                onBack()
            },
            title = { FintrackTitleLargeText(stringResource(Res.string.title_asset_transaction_prompt)) },
            text = { FintrackBodyMediumText(stringResource(Res.string.msg_asset_transaction_prompt, asset.name)) },
            confirmButton = {
                TextButton(onClick = {
                    addedAsset = null
                    viewModel.onIntent(AssetIntent.AssetTransactionPromptAnswered(true))
                    onRegisterTransaction(asset, transactionDescription)
                }) { FintrackLabelMediumText(stringResource(Res.string.check_record_transaction_yes)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    addedAsset = null
                    viewModel.onIntent(AssetIntent.AssetTransactionPromptAnswered(false))
                    onBack()
                }) { FintrackLabelMediumText(stringResource(Res.string.check_record_transaction_no)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketRatePickerSheet(
    type: AssetType,
    rates: List<AssetRate>,
    isLoading: Boolean,
    onSelect: (AssetRate) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val availableRates = rates.filter { it.type == type }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            FintrackTitleLargeText(stringResource(Res.string.title_select_asset_market))
            Spacer(Modifier.height(8.dp))
            if (isLoading && availableRates.isEmpty()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
            }
            if (availableRates.isEmpty()) {
                FintrackBodyMediumText(stringResource(Res.string.msg_market_rates_unavailable))
                TextButton(onClick = onRefresh) {
                    FintrackLabelMediumText(stringResource(Res.string.label_retry))
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 480.dp)) {
                    items(availableRates, key = { it.code }) { rate ->
                        ListItem(
                            headlineContent = { FintrackBodyMediumText(rate.name) },
                            supportingContent = { FintrackLabelMediumText(rate.code.uppercase()) },
                            trailingContent = { FintrackLabelMediumText(rate.price.toSignedPersianPrice()) },
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(rate) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                        HorizontalDivider()
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AssetType.label(): String = stringResource(
    when (this) {
        AssetType.GOLD -> Res.string.asset_type_gold
        AssetType.FX -> Res.string.asset_type_fx
        AssetType.STOCK -> Res.string.asset_type_stock
        AssetType.CUSTOM -> Res.string.asset_type_custom
    }
)
