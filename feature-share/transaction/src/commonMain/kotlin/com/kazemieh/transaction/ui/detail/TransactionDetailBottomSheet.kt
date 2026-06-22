package com.kazemieh.transaction.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.ImageStorage
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailBottomSheet(
    transactionWithRelations: TransactionWithRelations,
    onEdit: (TransactionWithRelations) -> Unit,
    onDelete: (TransactionWithRelations) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val glassColors = LocalGlassColors.current
    val colors = FinTrackPickerColors.rainbow()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                    .padding(bottom = 24.dp)
            ) {
                ScreenHeader(
                    title = stringResource(Res.string.transaction) + " " + stringResource(Res.string.more_details),
                    onClose = onDismiss,
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { onEdit(transactionWithRelations) }) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = GlassBlue)
                            }
                            IconButton(onClick = { onDelete(transactionWithRelations) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = GlassRed)
                            }
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        DetailAmountCard(transactionWithRelations)
                    }

                    item {
                        DetailMetadataSection(transactionWithRelations)
                    }

                    if (transactionWithRelations.transaction.description?.isNotEmpty() == true) {
                        item {
                            DetailDescriptionCard(transactionWithRelations.transaction.description!!)
                        }
                    }

                    if (transactionWithRelations.tags.isNotEmpty()) {
                        item {
                            DetailTagsSection(transactionWithRelations.tags, colors)
                        }
                    }

                    if (transactionWithRelations.persons.isNotEmpty()) {
                        item {
                            DetailPersonsSection(transactionWithRelations.persons)
                        }
                    }

                    transactionWithRelations.transaction.photoPath?.let { path ->
                        item {
                            DetailPhotoSection(path)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailAmountCard(item: TransactionWithRelations) {
    val (color, _) = getTransactionColors(item.transaction.type)
    val isTransfer = item.transaction.type == TransactionType.TRANSFER
    val amount = if (isTransfer) item.transaction.amountTransfer.toLong() else item.transaction.amount.toLong()
    
    GlassCard(tone = GlassTone.Strong, padding = 20.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FintrackHeadlineSmallText(
                text = stringResource(
                    Res.string.label_amount_with_unit,
                    amount.toPersianPrice(),
                    stringResource(Res.string.unit_toman_short)
                ),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = LocalGlassColors.current.text3,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                FintrackBodyMediumText(
                    text = item.transaction.date,
                    color = LocalGlassColors.current.text2
                )
            }
        }
    }
}

@Composable
private fun DetailMetadataSection(item: TransactionWithRelations) {
    val glassColors = LocalGlassColors.current
    val isTransfer = item.transaction.type == TransactionType.TRANSFER
    
    GlassCard(padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Category
            DetailInfoRow(
                label = stringResource(fintrack.core.designsystem.generated.resources.Res.string.category),
                value = item.category.name,
                iconId = item.category.iconId,
                colorId = item.category.colorId
            )
            
            HorizontalDivider(color = glassColors.glassEdge, thickness = 0.5.dp)
            
            // Source(s)
            if (isTransfer && item.sourceEnd != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailInfoRow(
                        label = stringResource(Res.string.source_from),
                        value = item.source.name,
                        iconId = item.source.iconId,
                        colorId = item.source.colorId
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = glassColors.text3,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DetailInfoRow(
                        label = stringResource(Res.string.source_to),
                        value = item.sourceEnd!!.name,
                        iconId = item.sourceEnd!!.iconId,
                        colorId = item.sourceEnd!!.colorId
                    )
                }
            } else {
                DetailInfoRow(
                    label = stringResource(Res.string.source),
                    value = item.source.name,
                    iconId = item.source.iconId,
                    colorId = item.source.colorId
                )
            }
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String, iconId: Int, colorId: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FintrackLabelMediumText(text = label, color = LocalGlassColors.current.text3)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FintrackBodyMediumText(text = value, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            FinTrackLeadingIcon(
                colorId = colorId,
                iconId = iconId,
                style = LeadingIconStyle.Badge,
                size = 32.dp,
                iconSize = 14.dp,
                corner = 8.dp
            )
        }
    }
}

@Composable
private fun DetailDescriptionCard(description: String) {
    GlassCard(padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FintrackLabelMediumText(text = stringResource(Res.string.description), color = LocalGlassColors.current.text3)
            FintrackBodyMediumText(text = description)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailTagsSection(tags: List<com.kazemieh.common.model.Tag>, colors: List<com.kazemieh.designsystem.picker.PickableColor>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelMediumText(text = stringResource(Res.string.tags), color = LocalGlassColors.current.text3)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                val color = colors.firstOrNull { it.id == tag.colorId }?.color ?: GlassBlue
                Chip(color = color, onClick = {}) {
                    FintrackLabelSmallText(text = "#${tag.name}", color = color)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailPersonsSection(persons: List<com.kazemieh.common.model.Person>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelMediumText(text = stringResource(Res.string.persons), color = LocalGlassColors.current.text3)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            persons.forEach { person ->
                Chip(color = GlassGreen, onClick = {}) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = GlassGreen)
                        Spacer(Modifier.width(4.dp))
                        FintrackLabelSmallText(text = person.name, color = GlassGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPhotoSection(path: String) {
    val imageStorage = koinInject<ImageStorage>()
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        imageStorage.loadImage(path)?.let {
            bitmap = it.decodeToImageBitmap()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FintrackLabelMediumText(text = stringResource(Res.string.label_attachment), color = LocalGlassColors.current.text3)
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16/9f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun getTransactionColors(type: TransactionType): Pair<Color, Color> {
    return when (type) {
        TransactionType.INCOME -> GlassGreen to GlassGreenSoft
        TransactionType.TRANSFER -> GlassBlue to GlassBlueSoft
        TransactionType.EXPENSE -> GlassRed to GlassRedSoft
        else -> GlassRed to GlassRedSoft
    }
}
