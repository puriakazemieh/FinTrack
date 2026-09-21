package com.kazemieh.asset.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.asset.ui.AssetEffect
import com.kazemieh.asset.ui.AssetIntent
import com.kazemieh.asset.ui.AssetViewModel
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.Asset
import com.kazemieh.common.model.AssetRate
import com.kazemieh.common.model.AssetType
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.Person
import com.kazemieh.common.model.Source
import com.kazemieh.common.model.Tag
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.toSignedPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenDark
import com.kazemieh.designsystem.GlassGreenSoft
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassRedSoft
import com.kazemieh.designsystem.LocalCurrency
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.calculator.CalculatorBottomSheet
import com.kazemieh.designsystem.component.glass.AddFrame
import com.kazemieh.designsystem.component.glass.Field
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.LargeAmountCard
import com.kazemieh.designsystem.component.glass.RemovableChip
import com.kazemieh.designsystem.component.glass.SectionContainer
import com.kazemieh.designsystem.component.glassTextFieldColors
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import com.kazemieh.financialsource.ui.list.SourcePickerBottomSheet
import com.kazemieh.person.ui.list.PersonPickerBottomSheet
import com.kazemieh.tag.ui.list.TagPickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.asset_type_crypto
import fintrack.core.designsystem.generated.resources.asset_type_custom
import fintrack.core.designsystem.generated.resources.asset_type_fx
import fintrack.core.designsystem.generated.resources.asset_type_gold
import fintrack.core.designsystem.generated.resources.asset_type_stock
import fintrack.core.designsystem.generated.resources.btn_add_person
import fintrack.core.designsystem.generated.resources.btn_add_tag
import fintrack.core.designsystem.generated.resources.category
import fintrack.core.designsystem.generated.resources.hint_asset_market_code
import fintrack.core.designsystem.generated.resources.label_asset_type
import fintrack.core.designsystem.generated.resources.label_quantity
import fintrack.core.designsystem.generated.resources.label_asset_name
import fintrack.core.designsystem.generated.resources.label_related_persons
import fintrack.core.designsystem.generated.resources.label_retry
import fintrack.core.designsystem.generated.resources.label_tag_prefix
import fintrack.core.designsystem.generated.resources.label_units_count
import fintrack.core.designsystem.generated.resources.asset_unit_count
import fintrack.core.designsystem.generated.resources.asset_unit_gram
import fintrack.core.designsystem.generated.resources.asset_unit_share
import fintrack.core.designsystem.generated.resources.msg_market_rates_unavailable
import fintrack.core.designsystem.generated.resources.select_category
import fintrack.core.designsystem.generated.resources.select_source
import fintrack.core.designsystem.generated.resources.source
import fintrack.core.designsystem.generated.resources.tags
import fintrack.core.designsystem.generated.resources.title_assets_management
import fintrack.core.designsystem.generated.resources.title_person_management
import fintrack.core.designsystem.generated.resources.title_select_asset_market
import fintrack.core.designsystem.generated.resources.title_tag_management
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetScreen(
    assetId: Long?,
    initialMarketCode: String? = null,
    onBack: () -> Unit,
    onRegisterTransaction: (Asset, String, TransactionType) -> Unit,
    viewModel: AssetViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val glassColors = LocalGlassColors.current
    val isEdit = assetId != null && assetId != 0L
    val dismissForm = {
        viewModel.onIntent(AssetIntent.AssetFormDismissed(isEdit))
        onBack()
    }

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.GOLD) }
    var quantity by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var marketCode by remember { mutableStateOf(initialMarketCode) }
    var isBuy by remember { mutableStateOf(true) }
    var showMarketPicker by remember { mutableStateOf(false) }
    var showQuantityCalc by remember { mutableStateOf(false) }
    var showPriceCalc by remember { mutableStateOf(false) }
    var showTypePicker by remember { mutableStateOf(false) }

    val selectedMarketRate = state.marketRates.firstOrNull { it.code == marketCode }

    // Transaction Registration
    var registerTransaction by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedSource by remember { mutableStateOf<Source?>(null) }
    var selectedPersons by remember { mutableStateOf<Set<Person>>(emptySet()) }
    var selectedTags by remember { mutableStateOf<Set<Tag>>(emptySet()) }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSourcePicker by remember { mutableStateOf(false) }
    var showPersonPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }

    LaunchedEffect(
        state.defaultIncomeCategory,
        state.defaultExpenseCategory,
        state.defaultSource,
        isBuy
    ) {
        if (selectedCategory == null || state.defaultIncomeCategory != null || state.defaultExpenseCategory != null) {
            selectedCategory =
                if (isBuy) state.defaultExpenseCategory else state.defaultIncomeCategory
        }
        if (selectedSource == null) {
            selectedSource = state.defaultSource
        }
    }

    LaunchedEffect(assetId) {
        viewModel.onIntent(AssetIntent.AssetFormOpened(isEdit))
        if (assetId != null && assetId != 0L) {
            viewModel.onIntent(AssetIntent.LoadAsset(assetId))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(AssetIntent.SyncRates(com.kazemieh.common.analytics.RefreshTrigger.INITIAL))
    }

    LaunchedEffect(initialMarketCode, state.marketRates) {
        if ((assetId != null && assetId != 0L) || initialMarketCode == null) return@LaunchedEffect
        state.marketRates.firstOrNull { it.code.equals(initialMarketCode, ignoreCase = true) }?.let { rate ->
            viewModel.onIntent(AssetIntent.MarketRateSelected(rate))
            type = rate.type
            marketCode = rate.code
            if (name.isBlank()) name = rate.name
            if (purchasePrice.isBlank()) purchasePrice = rate.price.toString()
        }
    }

    LaunchedEffect(state.selectedAsset) {
        state.selectedAsset?.let { asset ->
            name = asset.name
            type = asset.type
            quantity = kotlin.math.abs(asset.quantity).toString()
            purchasePrice = asset.purchasePrice.toString()
            marketCode = asset.marketCode
            isBuy = asset.quantity >= 0
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AssetEffect.AssetAdded -> {
                    onBack()
                }

                is AssetEffect.ShowMessage -> {}
            }
        }
    }

    val q = quantity.toDoubleOrNull() ?: 0.0
    val p = purchasePrice.toLongOrNull() ?: 0L
    val totalPrice = (q * p).toLong()
    val quantityUnit = type.quantityUnit(marketCode)

    ModalBottomSheet(
        onDismissRequest = dismissForm,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        AddFrame(
            title = if (assetId != null && assetId != 0L) stringResource(Res.string.title_assets_management) else stringResource(
                Res.string.title_assets_management
            ),
            sub = "افزودن دارایی",
            iconId = 1,
            colorId = 1,
            heroName = name,
            hero = { AssetSymbolBadge(marketCode = marketCode, type = type) },
            primaryLabel = "ذخیره",
            onPrimaryClick = {
                val asset = Asset(
                    id = if (assetId == 0L) null else assetId,
                    name = name,
                    type = type,
                    quantity = if (isBuy) q else -q,
                    purchasePrice = p,
                    marketCode = marketCode,
                    colorId = 1,
                    iconId = 1
                )
                viewModel.onIntent(
                    AssetIntent.AddAsset(
                        asset = asset,
                        registerTransaction = registerTransaction,
                        category = selectedCategory,
                        source = selectedSource,
                        persons = selectedPersons,
                        tags = selectedTags,
                        isBuy = isBuy
                    )
                )
            },
            onClose = dismissForm,
            onFilterClick = {
                if (assetId != null) {
                    viewModel.onIntent(AssetIntent.DeleteAsset(assetId, deleteTransaction = false))
                    onBack()
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Buy / Sell Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (isBuy) GlassRedSoft else GlassGreenSoft),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .padding(4.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(if (isBuy) GlassRed else Color.Transparent)
                                .clickable { isBuy = true },
                            contentAlignment = Alignment.Center
                        ) {
                            FintrackTitleMediumText(
                                text = "خرید",
                                color = if (isBuy) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .padding(4.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(if (!isBuy) GlassGreen else Color.Transparent)
                                .clickable { isBuy = false },
                            contentAlignment = Alignment.Center
                        ) {
                            FintrackTitleMediumText(
                                text = "فروش",
                                color = if (!isBuy) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Field(
                        label = stringResource(Res.string.label_asset_type),
                        onClick = { showTypePicker = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FintrackBodyMediumText(text = type.label())
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                }

                if (type != AssetType.CUSTOM) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                viewModel.onIntent(AssetIntent.MarketPickerOpened(type))
                                showMarketPicker = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AssetSymbolBadge(marketCode = marketCode, type = type, size = 30.dp)
                                    Spacer(Modifier.size(8.dp))
                                    FintrackBodyMediumText(
                                        text = selectedMarketRate?.name
                                            ?: stringResource(Res.string.hint_asset_market_code)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            FintrackLabelMediumText(
                                text = stringResource(Res.string.label_asset_name),
                                color = glassColors.text3
                            )
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = {
                                FintrackBodyMediumText(
                                    text = "مثال: ربع سکه",
                                    color = glassColors.text3
                                )
                            },
                            colors = glassTextFieldColors(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = glassColors.text,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        }
                    }
                }

                item {
                    LargeAmountCard(
                        amount = quantity,
                        onAmountChange = { quantity = it },
                        onCalcClick = { showQuantityCalc = true },
                        label = stringResource(Res.string.label_quantity),
                        suffixLabel = quantityUnit
                    )
                }

                item {
                    FintrackTitleSmallText("قیمت واحد")
                    LargeAmountCard(
                        amount = purchasePrice,
                        onAmountChange = { purchasePrice = it },
                        onCalcClick = { showPriceCalc = true }
                    )
                }

                if (totalPrice > 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FintrackTitleSmallText("ارزش کل:")
                            FintrackTitleMediumText(
                                "${totalPrice.toPersianPrice()} ${LocalCurrency.current.symbol}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (assetId == null || assetId == 0L) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FintrackTitleMediumText("ثبت به عنوان تراکنش")
                            Switch(
                                checked = registerTransaction,
                                onCheckedChange = {
                                    registerTransaction = it
                                    viewModel.onIntent(AssetIntent.AssetTransactionPromptAnswered(it))
                                })
                        }
                    }

                    if (registerTransaction) {
                        // Category Field
                        item {
                            Field(
                                label = stringResource(Res.string.category),
                                required = true,
                                onClick = { showCategoryPicker = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FintrackBodyMediumText(
                                        selectedCategory?.name ?: stringResource(
                                            Res.string.select_category
                                        ), fontWeight = FontWeight.SemiBold
                                    )
                                    val icon =
                                        FinTrackIcons.findIcon(selectedCategory?.iconId).resource
                                    if (icon is org.jetbrains.compose.resources.DrawableResource) {
                                        Icon(
                                            painterResource(icon),
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            val cats =
                                if (isBuy) state.mostUsedExpenseCategories else state.mostUsedIncomeCategories
                            if (cats.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(cats) { cat ->
                                        val selected = selectedCategory?.id == cat.id
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { selectedCategory = cat }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            FintrackBodyMediumText(
                                                text = cat.name,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Source Field
                        item {
                            Field(
                                label = stringResource(Res.string.source),
                                required = true,
                                onClick = { showSourcePicker = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FintrackBodyMediumText(
                                        selectedSource?.name ?: stringResource(
                                            Res.string.select_source
                                        ), fontWeight = FontWeight.SemiBold
                                    )
                                    val icon =
                                        FinTrackIcons.findIcon(selectedSource?.iconId).resource
                                    if (icon is org.jetbrains.compose.resources.DrawableResource) {
                                        Icon(
                                            painterResource(icon),
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            if (state.mostUsedSources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.mostUsedSources) { src ->
                                        val selected = selectedSource?.id == src.id
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { selectedSource = src }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            FintrackBodyMediumText(
                                                text = src.name,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Persons
                        item {
                            SectionContainer(
                                title = stringResource(Res.string.label_related_persons),
                                sub = stringResource(Res.string.title_person_management),
                                onAddClick = { showPersonPicker = true },
                                addLabel = stringResource(Res.string.btn_add_person)
                            ) {
                                selectedPersons.forEach { person ->
                                    RemovableChip(
                                        label = person.name, color = GlassGreen,
                                        onRemove = {
                                            selectedPersons =
                                                selectedPersons.filter { it.id != person.id }
                                                    .toSet()
                                        },
                                        icon = {
                                            Box(
                                                modifier = Modifier.size(18.dp).clip(CircleShape)
                                                    .background(GlassGreen),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                FintrackLabelSmallText(
                                                    text = person.name.take(1),
                                                    fontWeight = FontWeight.Bold,
                                                    color = GlassGreenDark
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                            if (state.mostUsedPersons.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.mostUsedPersons) { p ->
                                        val selected = selectedPersons.contains(p)
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    selectedPersons =
                                                        if (selected) selectedPersons - p else selectedPersons + p
                                                }.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            FintrackBodyMediumText(
                                                text = p.name,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Tags
                        item {
                            SectionContainer(
                                title = stringResource(Res.string.tags),
                                sub = stringResource(Res.string.title_tag_management),
                                onAddClick = { showTagPicker = true },
                                addLabel = stringResource(Res.string.btn_add_tag)
                            ) {
                                val colors = FinTrackPickerColors.rainbow()
                                selectedTags.forEach { tag ->
                                    val color = colors.firstOrNull { it.id == tag.colorId }?.color
                                        ?: GlassBlue
                                    RemovableChip(
                                        label = stringResource(
                                            Res.string.label_tag_prefix,
                                            tag.name
                                        ), color = color,
                                        onRemove = {
                                            selectedTags =
                                                selectedTags.filter { it.id != tag.id }.toSet()
                                        }
                                    )
                                }
                            }
                            if (state.mostUsedTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.mostUsedTags) { t ->
                                        val selected = selectedTags.contains(t)
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    selectedTags =
                                                        if (selected) selectedTags - t else selectedTags + t
                                                }.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            FintrackBodyMediumText(
                                                text = t.name,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQuantityCalc) {
        CalculatorBottomSheet(
            initialAmount = quantity,
            onConfirm = { quantity = it; showQuantityCalc = false },
            onDismiss = { showQuantityCalc = false }
        )
    }

    if (showPriceCalc) {
        CalculatorBottomSheet(
            initialAmount = purchasePrice,
            onConfirm = { purchasePrice = it; showPriceCalc = false },
            onDismiss = { showPriceCalc = false }
        )
    }

    if (showCategoryPicker) {
        CategoryPickerBottomSheet(
            transactionType = if (isBuy) TransactionType.EXPENSE else TransactionType.INCOME,
            onCategoryClick = { selectedCategory = it; showCategoryPicker = false },
            onDismiss = { showCategoryPicker = false }
        )
    }

    if (showSourcePicker) {
        SourcePickerBottomSheet(
            onSourceClick = { selectedSource = it; showSourcePicker = false },
            onDismiss = { showSourcePicker = false }
        )
    }

    if (showPersonPicker) {
        PersonPickerBottomSheet(
            selectedPersons = selectedPersons,
            onSubmitClick = { selectedPersons = it ?: emptySet(); showPersonPicker = false },
            onDismiss = { showPersonPicker = false }
        )
    }

    if (showTagPicker) {
        TagPickerBottomSheet(
            selectedTags = selectedTags,
            onSubmitClick = { selectedTags = it ?: emptySet(); showTagPicker = false },
            onDismiss = { showTagPicker = false }
        )
    }

    if (showTypePicker) {
        AssetTypePickerBottomSheet(
            onSelect = { selectedType ->
                viewModel.onIntent(AssetIntent.AssetTypeSelected(selectedType))
                if (type != selectedType) {
                    type = selectedType
                    marketCode = null
                    name = ""
                    purchasePrice = ""
                }
                showTypePicker = false
            },
            onDismiss = { showTypePicker = false }
        )
    }

    if (showMarketPicker) {
        MarketRatePickerSheet(
            type = type,
            rates = state.marketRates,
            isLoading = state.isLoading,
            onDismiss = { showMarketPicker = false },
            onRefresh = { viewModel.onIntent(AssetIntent.SyncRates(com.kazemieh.common.analytics.RefreshTrigger.MANUAL)) },
            onSelect = {
                viewModel.onIntent(AssetIntent.MarketRateSelected(it))
                marketCode = it.code
                name = it.name
                purchasePrice = it.price.toString()
                showMarketPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketRatePickerSheet(
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
                LazyColumn(
                    modifier = Modifier.heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                AssetSymbolBadge(marketCode = rate.code, type = rate.type, size = 30.dp)
                                Spacer(Modifier.size(10.dp))
                                Column {
                                    FintrackBodyMediumText(rate.name)
                                    FintrackLabelMediumText(
                                        rate.code.uppercase(),
                                        color = glassColors.text3
                                    )
                                }
                                FintrackLabelMediumText(
                                    rate.price.toSignedPersianPrice(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
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
private fun AssetSymbolBadge(
    marketCode: String?,
    type: AssetType,
    size: androidx.compose.ui.unit.Dp = 64.dp
) {
    val symbol = when (marketCode?.lowercase()) {
        "btc" -> "₿"
        "eth" -> "Ξ"
        "usdt" -> "₮"
        "trx" -> "TRX"
        "doge" -> "Ð"
        "xrp" -> "XRP"
        "ton" -> "TON"
        "sol" -> "SOL"
        "gold_18k", "gold_24k", "coin_emami" -> "Au"
        "silver_999", "silver_925" -> "Ag"
        "usd" -> "$"
        "eur" -> "€"
        "gbp" -> "£"
        else -> when (type) {
            AssetType.GOLD -> "Au"
            AssetType.FX -> "¤"
            AssetType.CRYPTO -> "₿"
            AssetType.STOCK -> "▥"
            AssetType.CUSTOM -> "•"
        }
    }
    Box(
        modifier = Modifier.size(size).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        FintrackTitleMediumText(
            text = symbol,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AssetType.label(): String = stringResource(
    when (this) {
        AssetType.GOLD -> Res.string.asset_type_gold
        AssetType.FX -> Res.string.asset_type_fx
        AssetType.STOCK -> Res.string.asset_type_stock
        AssetType.CRYPTO -> Res.string.asset_type_crypto
        AssetType.CUSTOM -> Res.string.asset_type_custom
    }
)

@Composable
private fun AssetType.quantityUnit(marketCode: String?): String = when {
    this == AssetType.GOLD && marketCode?.lowercase()?.startsWith("coin_") == true ->
        stringResource(Res.string.asset_unit_count)
    this == AssetType.GOLD -> stringResource(Res.string.asset_unit_gram)
    this == AssetType.STOCK -> stringResource(Res.string.asset_unit_share)
    else -> marketCode?.uppercase() ?: stringResource(Res.string.asset_unit_count)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetTypePickerBottomSheet(
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
            FintrackTitleLargeText(stringResource(Res.string.label_asset_type))
            Spacer(Modifier.height(16.dp))
            AssetType.entries.filter { it != AssetType.STOCK }.forEach { t ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onSelect(t) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FintrackTitleMediumText(text = t.label())
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
