package com.kazemieh.asset.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onBack: () -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.GOLD) }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showCustomSheet by remember { mutableStateOf(false) }

    FintrackScreen(
        title = stringResource(Res.string.title_add_asset),
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
                        name = name,
                        type = type,
                        quantity = quantity.toDoubleOrNull() ?: 0.0,
                        purchasePrice = purchasePrice.toLongOrNull() ?: 0L,
                        description = description,
                        colorId = 1,
                        iconId = 1
                    )
                    viewModel.onIntent(AssetIntent.AddAsset(asset))
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
