package com.kazemieh.asset.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetType
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.FintrackBackgroundBlobs
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAssetSheet(
    onDismiss: () -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FintrackBackgroundBlobs()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FintrackTitleLargeText(stringResource(Res.string.title_add_custom_asset))

                FintrackOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { FintrackBodyMediumText(stringResource(Res.string.source_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                FintrackOutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { FintrackBodyMediumText(stringResource(Res.string.label_quantity)) },
                    modifier = Modifier.fillMaxWidth()
                )

                FintrackOutlinedTextField(
                    value = currentPrice,
                    onValueChange = { currentPrice = it },
                    label = { FintrackBodyMediumText(stringResource(Res.string.label_current_unit_value)) },
                    modifier = Modifier.fillMaxWidth()
                )

                FintrackOutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { FintrackBodyMediumText(stringResource(Res.string.description)) },
                    modifier = Modifier.fillMaxWidth()
                )

                FintrackButton(
                    text = stringResource(Res.string.save_),
                    onClick = {
                        val asset = Asset(
                            name = name,
                            type = AssetType.CUSTOM,
                            quantity = quantity.toDoubleOrNull() ?: 0.0,
                            purchasePrice = currentPrice.toLongOrNull() ?: 0L,
                            description = description,
                            colorId = 1,
                            iconId = 1
                        )
                        viewModel.onIntent(AssetIntent.AddAsset(asset))
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
