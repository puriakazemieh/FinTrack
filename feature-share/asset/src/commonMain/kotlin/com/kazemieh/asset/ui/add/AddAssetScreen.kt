package com.kazemieh.asset.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.asset.ui.component.CustomAssetSheet
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetType
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
    viewModel: AssetViewModel = koinViewModel()
) {
    val assetState by viewModel.state.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.GOLD) }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showCustomSheet by remember { mutableStateOf(false) }

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
            description = asset.description ?: ""
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
                        label = { FintrackLabelMediumText(assetType.name) }
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
                modifier = Modifier.fillMaxWidth()
            )

            FintrackOutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { FintrackBodyMediumText(stringResource(Res.string.description)) },
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = { showCustomSheet = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                FintrackLabelMediumText(stringResource(Res.string.btn_add_custom_asset))
            }

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
                        description = description,
                        colorId = 1,
                        iconId = 1
                    )
                    if (assetId == null) {
                        viewModel.onIntent(AssetIntent.AddAsset(asset))
                    } else {
                        viewModel.onIntent(AssetIntent.UpdateAsset(asset))
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showCustomSheet) {
        CustomAssetSheet(
            onDismiss = {
                showCustomSheet = false
                onBack()
            }
        )
    }
}
