package com.kazemieh.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toPersianPrice
import com.kazemieh.common.ImageStorage
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.picker.FinTrackIcons
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.btn_delete_transaction
import fintrack.core.designsystem.generated.resources.label_and_separator
import fintrack.core.designsystem.generated.resources.label_amount_with_unit
import fintrack.core.designsystem.generated.resources.label_comma
import fintrack.core.designsystem.generated.resources.label_more_count
import fintrack.core.designsystem.generated.resources.label_to
import fintrack.core.designsystem.generated.resources.transaction_credit_collection
import fintrack.core.designsystem.generated.resources.transaction_debt_payment
import fintrack.core.designsystem.generated.resources.title_edit_transaction
import fintrack.core.designsystem.generated.resources.unit_toman_short
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.math.abs

@Composable
fun SwipeableTxRow(
    item: TransactionWithRelations,
    onClick: (TransactionWithRelations) -> Unit,
    onEdit: (TransactionWithRelations) -> Unit,
    onDelete: (TransactionWithRelations) -> Unit,
) {
    FintrackSwipeableRow(
        onDelete = { onDelete(item) },
        onEdit = { onEdit(item) },
        backgroundContent = { progress -> TxSwipeBackground(progress) },
        content = { TxRow(item = item, onClick = onClick, onDelete = onDelete) }
    )
}

@Composable
fun SwipeableTxRowMinimal(
    item: TransactionWithRelations,
    onClick: () -> Unit,
    onEdit: (TransactionWithRelations) -> Unit,
    onDelete: (TransactionWithRelations) -> Unit,
    onRepeat: (TransactionWithRelations) -> Unit = {},
) {
    FintrackSwipeableRow(
        onDelete = { onDelete(item) },
        onEdit = { onEdit(item) },
        backgroundContent = { progress -> TxSwipeBackground(progress) },
        content = { TxRowMinimal(item = item, onClick = onClick, onLongClick = { onRepeat(item) }) }
    )
}

@Composable
private fun TxSwipeBackground(progress: Float) {
    if (progress == 0f) return
    val isDelete = progress < 0
    val absProgress = abs(progress)

    val bgColor = if (isDelete) GlassRedSoft else GlassBlueSoft
    val iconColor = if (isDelete) GlassRed else GlassBlue
    val alignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart
    val icon = if (isDelete) Icons.Default.Delete else Icons.Default.Edit

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = absProgress }
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TxRowMinimal(item: TransactionWithRelations, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    val (color, bgColor) = getTransactionColors(item.transaction.type)
    val icon = FinTrackIcons.findIcon(item.category.iconId)
    val glassColors = LocalGlassColors.current
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(glassColors.glass)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TransactionIconBox(icon.resource, color, bgColor)

        Column(modifier = Modifier.weight(1f)) {
            FintrackBodyMediumText(
                text = item.category.name,
                fontWeight = FontWeight.Bold
            )
            FintrackLabelSmallText(text = item.source.name)
        }

        TransactionAmountAndDate(
            amount = if (item.transaction.type == TransactionType.TRANSFER) item.transaction.amountTransfer.toLong() else item.transaction.amount.toLong(),
            date = item.transaction.date,
            color = color
        )
    }
}

@Composable
fun TxRow(
    item: TransactionWithRelations,
    onClick: (TransactionWithRelations) -> Unit,
    onDelete: (TransactionWithRelations) -> Unit = {}
) {
    val isExpandable = item.tags.size > 2 ||
            item.persons.size > 2 ||
            (item.transaction.description?.length ?: 0) > 35

    var isExpanded by remember { mutableStateOf(false) }

    val (color, bgColor) = getTransactionColors(item.transaction.type)
    val icon = FinTrackIcons.findIcon(item.category.iconId)
    val isTransfer = item.transaction.type == TransactionType.TRANSFER
    val glassColors = LocalGlassColors.current
    val settlementLabel = item.transaction.relatedDebtId?.let {
        stringResource(
            if (item.transaction.type == TransactionType.INCOME) {
                Res.string.transaction_credit_collection
            } else {
                Res.string.transaction_debt_payment
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(glassColors.glass)
            .clickable {
                if (isExpandable) isExpanded = !isExpanded else onClick(item)
            }
            .animateContentSize()
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransactionIconBox(icon.resource, color, bgColor)

            Column(modifier = Modifier.weight(1f)) {
                settlementLabel?.let {
                    FintrackLabelSmallText(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                FintrackBodyMediumText(
                    text = item.category.name,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SourceIconAndName(item.source.iconId, item.source.name)

                    if (isTransfer && item.sourceEnd != null) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(Res.string.label_to),
                            tint = LocalGlassColors.current.text3,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        SourceIconAndName(item.sourceEnd!!.iconId, item.sourceEnd!!.name)
                    }
                }
            }

            TransactionAmountAndDate(
                amount = if (isTransfer) item.transaction.amountTransfer.toLong() else item.transaction.amount.toLong(),
                date = item.transaction.date,
                color = color
            )
        }

        if (isExpanded) {
            ExpandedContent(item, onClick, onDelete)
        } else {
            CollapsedMetadata(item)
        }
    }
}

@Composable
private fun TransactionIconBox(iconRes: DrawableResource, color: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun TransactionAmountAndDate(amount: Long, date: String, color: Color) {
    val hidden = LocalHideBalance.current
    Column(horizontalAlignment = Alignment.End) {
        FintrackTitleSmallText(
            text = if (hidden) "••••••" else stringResource(
                Res.string.label_amount_with_unit,
                amount.toPersianPrice(),
                stringResource(Res.string.unit_toman_short)
            ),
            fontWeight = FontWeight.W700,
            color = color
        )
        FintrackLabelSmallText(
            text = date,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SourceIconAndName(iconId: Int, name: String) {
    val glassColors = LocalGlassColors.current
    val icon = FinTrackIcons.findIcon(iconId)
    Icon(
        painter = painterResource(icon.resource),
        contentDescription = null,
        tint = glassColors.text3,
        modifier = Modifier.size(12.dp).padding(top = 2.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    FintrackLabelSmallText(
        text = name,
        maxLines = 1
    )
}

@Composable
private fun ExpandedContent(
    item: TransactionWithRelations,
    onClick: (TransactionWithRelations) -> Unit,
    onDelete: (TransactionWithRelations) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (item.tags.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val firstTagIcon = FinTrackIcons.findIcon(item.tags.first().iconId)
                Icon(
                    painter = painterResource(firstTagIcon.resource),
                    contentDescription = null,
                    tint = LocalGlassColors.current.text3,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                FintrackLabelSmallText(
                    text = item.tags.joinToString(stringResource(Res.string.label_comma)) { it.name }
                )
            }
        }

        if (item.persons.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = LocalGlassColors.current.text3,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                FintrackLabelSmallText(
                    text = item.persons.joinToString(stringResource(Res.string.label_comma)) { it.name }
                )
            }
        }

        item.transaction.description?.let {
            if (it.isNotEmpty()) {
                FintrackLabelSmallText(text = it)
            }
        }

        item.transaction.photoPath?.let { path ->
            var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
            val imageStorage = koinInject<ImageStorage>()
            LaunchedEffect(path) {
                imageStorage.loadImage(path)?.let {
                    bitmap = it.decodeToImageBitmap()
                }
            }
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(vertical = 8.dp)
                        .clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(GlassBlueSoft)
                    .clickable { onClick(item) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.title_edit_transaction),
                    color = GlassBlue,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(GlassRedSoft)
                    .clickable { onDelete(item) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.btn_delete_transaction),
                    color = GlassRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun CollapsedMetadata(item: TransactionWithRelations) {
    val glassColors = LocalGlassColors.current
    if (item.tags.isNotEmpty() || item.persons.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.tags.isNotEmpty()) {
                val tagsText = if (item.tags.size > 2) {
                    item.tags.take(2).joinToString(stringResource(Res.string.label_and_separator)) { it.name } + stringResource(Res.string.label_more_count, item.tags.size - 2)
                } else {
                    item.tags.joinToString(stringResource(Res.string.label_and_separator)) { it.name }
                }
                MetadataItem(
                    iconRes = FinTrackIcons.findIcon(item.tags.first().iconId).resource,
                    text = tagsText,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            if (item.persons.isNotEmpty()) {
                val personsText = if (item.persons.size > 2) {
                    item.persons.take(2)
                        .joinToString(stringResource(Res.string.label_and_separator)) { it.name } + stringResource(Res.string.label_more_count, item.persons.size - 2)
                } else {
                    item.persons.joinToString(stringResource(Res.string.label_and_separator)) { it.name }
                }
                MetadataItem(
                    iconVector = Icons.Default.Person,
                    text = personsText,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }

    item.transaction.description?.let {
        if (it.isNotEmpty()) {
            FintrackLabelSmallText(text = it, maxLines = 1)
        }
    }
}

@Composable
private fun MetadataItem(
    iconRes: DrawableResource? = null,
    text: String,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = LocalGlassColors.current.text3,
                modifier = Modifier.size(12.dp)
            )
        } else if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = LocalGlassColors.current.text3,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(2.dp))
        FintrackLabelSmallText(text = text, maxLines = 1)
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
