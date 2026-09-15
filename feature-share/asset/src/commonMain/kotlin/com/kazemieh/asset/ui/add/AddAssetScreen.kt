package com.kazemieh.asset.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetEffect
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    assetId: Long? = null,
    initialMarketCode: String? = null,
    onBack: () -> Unit,
    onRegisterTransaction: (Asset, String, TransactionType) -> Unit = { _, _, _ -> },
    viewModel: AssetViewModel = koinViewModel()
) {
    val assetState by viewModel.state.collectAsStateWithLifecycle()
    val glassColors = LocalGlassColors.current

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.GOLD) }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var marketCode by remember { mutableStateOf("") }
    var selectedMarketRate by remember { mutableStateOf<AssetRate?>(null) }
    var description by remember { mutableStateOf("") }
    var addedAsset by remember { mutableStateOf<Asset?>(null) }
    var isBuy by remember { mutableStateOf(true) }

    var showTypePicker by remember { mutableStateOf(false) }
    var showMarketPicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var calculatorTarget by remember { mutableStateOf(AssetAmountTarget.PurchasePrice) }

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

    LaunchedEffect(initialMarketCode, assetState.marketRates) {
        if (assetId != null || initialMarketCode == null) return@LaunchedEffect
        assetState.marketRates.find { it.code.equals(initialMarketCode, ignoreCase = true) }?.let { rate ->
            selectedMarketRate = rate
            type = rate.type
            name = rate.name
            marketCode = rate.code
            if (purchasePrice.isBlank()) purchasePrice = rate.price.toString()
        }
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
        LazyColumn(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(glassColors.glass, RoundedCornerShape(14.dp))
                        .border(1.dp, glassColors.glassEdge, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val buyActive = isBuy
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .then(if (buyActive) Modifier.background(GlassGreen.copy(alpha = 0.14f)).border(1.dp, GlassGreen.copy(alpha = 0.33f), RoundedCornerShape(10.dp)) else Modifier)
                            .clickable { isBuy = true }.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) { FintrackBodyMediumText(stringResource(Res.string.label_asset_purchase), color = if (buyActive) GlassGreen else glassColors.text3) }

                    val sellActive = !isBuy
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .then(if (sellActive) Modifier.background(GlassRed.copy(alpha = 0.14f)).border(1.dp, GlassRed.copy(alpha = 0.33f), RoundedCornerShape(10.dp)) else Modifier)
                            .clickable { isBuy = false }.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) { FintrackBodyMediumText(stringResource(Res.string.label_asset_sale), color = if (sellActive) GlassRed else glassColors.text3) }
                }
            }

            item {
                Field(
                    label = stringResource(Res.string.label_asset_type),
                    required = true,
                    onClick = { showTypePicker = true }
                ) {
                    PickerValue(
                        label = type.label(),
                        icon = Icons.Default.Category
                    )
                }
            }

            item {
                AssetTextInputCard(
                    label = stringResource(Res.string.label_asset_name_hint),
                    value = name,
                    onValueChange = { name = it },
                    required = true
                )
            }

            item {
                LargeAmountCard(
                    amount = quantity,
                    onAmountChange = { quantity = it },
                    onCalcClick = {
                        calculatorTarget = AssetAmountTarget.Quantity
                        showCalculator = true
                    },
                    label = stringResource(Res.string.label_quantity),
                    suffixLabel = stringResource(Res.string.label_asset_unit),
                    autoFocus = false,
                    isError = false
                )
            }

            item {
                LargeAmountCard(
                    amount = purchasePrice,
                    onAmountChange = { purchasePrice = it },
                    onCalcClick = {
                        calculatorTarget = AssetAmountTarget.PurchasePrice
                        showCalculator = true
                    },
                    label = stringResource(if (isBuy) Res.string.label_purchase_price else Res.string.label_sale_price),
                    autoFocus = false,
                    isError = false
                )
            }

            item {
                AssetTextInputCard(
                    label = stringResource(Res.string.description),
                    value = description,
                    onValueChange = { description = it },
                    required = false
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
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
    }

    if (showTypePicker) {
        AssetTypePickerSheet(
            onSelect = { selectedType ->
                type = selectedType
                showTypePicker = false
                if (selectedType != AssetType.CUSTOM) {
                    showMarketPicker = true
                }
            },
            onDismiss = { showTypePicker = false }
        )
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

    if (showCalculator) {
        CalculatorBottomSheet(
            initialAmount = if (calculatorTarget == AssetAmountTarget.Quantity) quantity else purchasePrice,
            onConfirm = {
                if (calculatorTarget == AssetAmountTarget.Quantity) quantity = it else purchasePrice = it
                showCalculator = false
            },
            onDismiss = { showCalculator = false }
        )
    }

    addedAsset?.let { asset ->
        val operation = stringResource(if (isBuy) Res.string.label_asset_purchase else Res.string.label_asset_sale)
        val transactionDescription = stringResource(Res.string.asset_transaction_description, operation, asset.name)
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
                    onRegisterTransaction(asset, transactionDescription, if (isBuy) TransactionType.EXPENSE else TransactionType.INCOME)
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

private enum class AssetAmountTarget { Quantity, PurchasePrice }

@Composable
private fun AssetTextInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean
) {
    val glassColors = LocalGlassColors.current
    GlassCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FintrackLabelMediumText(label, color = glassColors.text3)
                if (required) {
                    FintrackLabelSmallText(
                        stringResource(Res.string.label_required_marker),
                        color = GlassRed,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { FintrackBodyMediumText(label, color = glassColors.text3) },
                colors = glassTextFieldColors(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = glassColors.text, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssetTypePickerSheet(
    onSelect: (AssetType) -> Unit,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = glassColors.bg0,
        dragHandle = null
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            FintrackTitleLargeText(stringResource(Res.string.title_select_asset_type))
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AssetType.entries) { assetType ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onSelect(assetType) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FintrackBodyMediumText(assetType.label())
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
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
    val glassColors = LocalGlassColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = glassColors.bg0,
        dragHandle = null
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
                LazyColumn(modifier = Modifier.heightIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableRates, key = { it.code }) { rate ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelect(rate) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    FintrackBodyMediumText(rate.name)
                                    FintrackLabelMediumText(rate.code.uppercase(), color = glassColors.text3)
                                }
                                FintrackLabelMediumText(rate.price.toSignedPersianPrice(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AssetType.label(): String = stringResource(
    when (this) {
        AssetType.GOLD -> Res.string.asset_type_gold_extended
        AssetType.FX -> Res.string.asset_type_fx_physical
        AssetType.STOCK -> Res.string.asset_type_crypto
        AssetType.CUSTOM -> Res.string.asset_type_custom
    }
)

@Composable
private fun PickerValue(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FintrackBodyMediumText(text = label, fontWeight = FontWeight.SemiBold)
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}
