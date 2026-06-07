package com.kazemieh.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.GlassBlue
import com.kazemieh.designsystem.GlassBlueSoft
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassGreenSoft
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.GlassRedSoft
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.GlassText3
import com.kazemieh.designsystem.picker.FinTrackIcons
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.btn_delete_transaction
import fintrack.core.designsystem.generated.resources.label_amount_with_unit
import fintrack.core.designsystem.generated.resources.label_to
import fintrack.core.designsystem.generated.resources.title_edit_transaction
import fintrack.core.designsystem.generated.resources.unit_toman_short
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
        backgroundContent = { progress ->
            if (progress != 0f) {
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
        },
        content = { TxRow(item = item, onClick = onClick, onDelete = onDelete) }
    )
}


@Composable
fun SwipeableTxRowMinimal(
    item: TransactionWithRelations,
    onClick: () -> Unit,
    onEdit: (TransactionWithRelations) -> Unit,
    onDelete: (TransactionWithRelations) -> Unit,
) {

    FintrackSwipeableRow(
        onDelete = { onDelete(item) },
        onEdit = { onEdit(item) },
        backgroundContent = { progress ->
            if (progress != 0f) {
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
        },
        content = { TxRowMinimal(item = item, onClick = onClick) }
    )
}

@Composable
fun TxRowMinimal(item: TransactionWithRelations, onClick: () -> Unit) {
    val isIncome = item.transaction.type == TransactionType.INCOME
    val isTransfer = item.transaction.type == TransactionType.TRANSFER
    val color = when {
        isIncome -> GlassGreen
        isTransfer -> GlassBlue
        else -> GlassRed
    }
    val bgColor = when {
        isIncome -> GlassGreenSoft
        isTransfer -> GlassBlueSoft
        else -> GlassRedSoft
    }

    val icon = FinTrackIcons.findIcon(item.category.iconId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(GlassColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(MaterialTheme.shapes.small)
                .background(bgColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon.resource),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(17.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            FintrackBodyMediumText(
                text = item.category.name,
                fontWeight = FontWeight.Bold,
                color = GlassText
            )

            FintrackLabelSmallText(text = item.source.name, color = GlassText3)
        }

        Column(horizontalAlignment = Alignment.End) {

            val displayAmount =
                if (isTransfer) item.transaction.amountTransfer else item.transaction.amount

            FintrackTitleSmallText(
                text = stringResource(
                    Res.string.label_amount_with_unit,
                    displayAmount.toPersianPrice(),
                    stringResource(Res.string.unit_toman_short)
                ),
                fontWeight = FontWeight.W700,
                color = color
            )

            FintrackLabelSmallText(
                text = item.transaction.date,
                color = GlassText3,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
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

    val isIncome = item.transaction.type == TransactionType.INCOME
    val isTransfer = item.transaction.type == TransactionType.TRANSFER
    val color = when {
        isIncome -> GlassGreen
        isTransfer -> GlassBlue
        else -> GlassRed
    }
    val bgColor = when {
        isIncome -> GlassGreenSoft
        isTransfer -> GlassBlueSoft
        else -> GlassRedSoft
    }

    val icon = FinTrackIcons.findIcon(item.category.iconId)
    val displayAmount = if (isTransfer) item.transaction.amountTransfer else item.transaction.amount

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(GlassColor)
            .clickable {
                if (isExpandable) {
                    isExpanded = !isExpanded
                } else {
                    onClick(item)
                }
            }
            .animateContentSize()
            .padding(14.dp),
//        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(bgColor)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon.resource),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(17.dp)
                )
            }


            Column(modifier = Modifier.weight(1f)) {
                FintrackBodyMediumText(
                    text = item.category.name,
                    fontWeight = FontWeight.Bold,
                    color = GlassText
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val sourceIcon = FinTrackIcons.findIcon(item.source.iconId)
                    Icon(
                        painter = painterResource(sourceIcon.resource),
                        contentDescription = null,
                        tint = GlassText3,
                        modifier = Modifier.size(12.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FintrackLabelSmallText(
                        text = item.source.name,
                        color = GlassText3,
                        maxLines = 1
                    )

                    if (isTransfer && item.sourceEnd != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Forward,
                            contentDescription = stringResource(Res.string.label_to),
                            tint = GlassText3,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        val sourceEndIcon = FinTrackIcons.findIcon(item.sourceEnd?.iconId)
                        Icon(
                            painter = painterResource(sourceEndIcon.resource),
                            contentDescription = null,
                            tint = GlassText3,
                            modifier = Modifier.size(12.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FintrackLabelSmallText(
                            text = item.sourceEnd?.name ?: "",
                            color = GlassText3,
                            maxLines = 1
                        )
                    }
                }

            }

            Column(horizontalAlignment = Alignment.End) {
                FintrackTitleSmallText(
                    text = stringResource(
                        Res.string.label_amount_with_unit,
                        displayAmount.toPersianPrice(),
                        stringResource(Res.string.unit_toman_short)
                    ),
                    fontWeight = FontWeight.W700,
                    color = color
                )
                FintrackLabelSmallText(
                    text = item.transaction.date,
                    color = GlassText3,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }


        val hasTags = item.tags.isNotEmpty()
        val hasPersons = item.persons.isNotEmpty()
        val hasDescription = !item.transaction.description.isNullOrEmpty()

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (hasTags) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val firstTagIcon = FinTrackIcons.findIcon(item.tags.first().iconId)
                        Icon(
                            painter = painterResource(firstTagIcon.resource),
                            contentDescription = null,
                            tint = GlassText3,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FintrackLabelSmallText(
                            text = item.tags.joinToString("، ") { it.name },
                            color = GlassText3
                        )
                    }
                }

                if (hasPersons) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GlassText3,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FintrackLabelSmallText(
                            text = item.persons.joinToString("، ") { it.name },
                            color = GlassText3
                        )
                    }
                }

                if (hasDescription) {
                    FintrackLabelSmallText(
                        text = item.transaction.description ?: "",
                        color = GlassText3
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
        } else {
            if (hasTags || hasPersons) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasTags) {
                        val tagsText = if (item.tags.size > 2) {
                            item.tags.take(2)
                                .joinToString("، ") { it.name } + " (+${item.tags.size - 2})"
                        } else {
                            item.tags.joinToString("، ") { it.name }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            val firstTagIcon = FinTrackIcons.findIcon(item.tags.first().iconId)
                            Icon(
                                painter = painterResource(firstTagIcon.resource),
                                contentDescription = null,
                                tint = GlassText3,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            FintrackLabelSmallText(
                                text = tagsText,
                                color = GlassText3,
                                maxLines = 1
                            )
                        }
                    }

                    if (hasPersons) {
                        val personsText = if (item.persons.size > 2) {
                            item.persons.take(2)
                                .joinToString("، ") { it.name } + " (+${item.persons.size - 2})"
                        } else {
                            item.persons.joinToString("، ") { it.name }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GlassText3,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            FintrackLabelSmallText(
                                text = personsText,
                                color = GlassText3,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (hasDescription) {
                FintrackLabelSmallText(
                    text = item.transaction.description ?: "",
                    color = GlassText3,
                    maxLines = 1
                )
            }
        }
    }
}