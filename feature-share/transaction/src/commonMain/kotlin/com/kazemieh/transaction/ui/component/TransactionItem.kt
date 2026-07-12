package com.kazemieh.transaction.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.common.toPersianPrice
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.glass.EntityChip
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.all
import fintrack.core.designsystem.generated.resources.amount_label
import fintrack.core.designsystem.generated.resources.amount_transfer_label
import fintrack.core.designsystem.generated.resources.delete
import fintrack.core.designsystem.generated.resources.description_with_value
import fintrack.core.designsystem.generated.resources.edit
import fintrack.core.designsystem.generated.resources.incoming
import fintrack.core.designsystem.generated.resources.label_transfer_arrow
import fintrack.core.designsystem.generated.resources.outcoming
import fintrack.core.designsystem.generated.resources.person_label
import fintrack.core.designsystem.generated.resources.source
import fintrack.core.designsystem.generated.resources.transfer
import org.jetbrains.compose.resources.stringResource

@Composable
fun TransactionItem(
    uiTransactionWithRelation: TransactionWithRelations,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val space = LocalSpacing.current

    val trx = uiTransactionWithRelation.transaction
    val isTransfer = trx.type == TransactionType.TRANSFER

    val personsText = remember(uiTransactionWithRelation.persons) {
        uiTransactionWithRelation.persons.joinToString { it.name }
    }

    val typeLabel = when (trx.type) {
        TransactionType.INCOME -> stringResource(Res.string.incoming)
        TransactionType.EXPENSE -> stringResource(Res.string.outcoming)
        TransactionType.TRANSFER -> stringResource(Res.string.transfer)
        else -> stringResource(Res.string.all)
    }

    val amountColor =
        if (trx.type == TransactionType.EXPENSE) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.secondary

    // Sign accompanies the color so direction reads at a glance (transfers stay neutral).
    val amountSign = when (trx.type) {
        TransactionType.INCOME -> "+ "
        TransactionType.EXPENSE -> "− "
        else -> ""
    }

    // ---------------------------
    val sourceIcon = remember(uiTransactionWithRelation.source.iconId) {
        FinTrackIcons.findIcon(uiTransactionWithRelation.source.iconId)
    }

    val colors = FinTrackPickerColors.rainbow()
    val color = remember(colors, uiTransactionWithRelation.category.colorId) {
        colors.firstOrNull { it.id == uiTransactionWithRelation.category.colorId } ?: colors.first()
    }

    // ---------------------------

    val barWidth = space.extraSmall

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = space.small),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = space.one)
    ) {
        val barWidthPx = with(LocalDensity.current) { barWidth.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val x = size.width - barWidthPx
                    drawRect(
                        color = color.color,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidthPx, size.height)
                    )
                }
                .padding(space.mediumSmall)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = barWidth + space.small)
            ) {
                // ---------- Header (Category + Edit/Delete) ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FinTrackLeadingIcon(
                            colorId = uiTransactionWithRelation.category.colorId,
                            iconId = uiTransactionWithRelation.category.iconId,
                            style = LeadingIconStyle.Badge,
                            size = space.extraLarge,
                            iconSize = space.large,
                        )
                        Spacer(modifier = Modifier.width(space.small))

                        FintrackTitleMediumText(
                            text = uiTransactionWithRelation.category.name,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(space.mediumSmall))

                // ---------- Source / Transfer Info ----------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    FintrackTitleMediumText(
                        text = stringResource(Res.string.source),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(space.small))
                    // 2) آیکون + نام منبع
                    FinTrackLeadingIcon(
                        colorId = uiTransactionWithRelation.source.colorId,
                        iconId = uiTransactionWithRelation.source.iconId,
                        style = LeadingIconStyle.Badge,
                        size = space.large,
                        iconSize = space.mediumSmall,
                    )
                    Spacer(modifier = Modifier.width(space.small))
                    FintrackTitleMediumText(
                        text = uiTransactionWithRelation.source.name,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isTransfer) {
                        Spacer(modifier = Modifier.width(space.small))
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.label_transfer_arrow),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(space.small))

                        uiTransactionWithRelation.sourceEnd?.let { sourceEnd ->
                            FinTrackLeadingIcon(
                                colorId = sourceEnd.colorId,
                                iconId = sourceEnd.iconId,
                                style = LeadingIconStyle.Badge,
                                size = space.large,
                                iconSize = space.mediumSmall,
                            )
                            Spacer(modifier = Modifier.width(space.small))
                            FintrackTitleMediumText(
                                text = sourceEnd.name,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    }
                }

                if (isTransfer) {
                    trx.amountTransferFormated?.let {
                        FintrackLabelSmallText(
                            text = stringResource(Res.string.amount_transfer_label, it),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(space.mediumSmall))

                // ---------- Amount ----------
                FintrackBodyLargeText(
                    text = stringResource(
                        Res.string.amount_label,
                        typeLabel,
                        amountSign + trx.amount.toLong().toPersianPrice()
                    ),
                    fontWeight = FontWeight.W700,
                    color = amountColor
                )

                // ---------- Persons ----------
                if (uiTransactionWithRelation.persons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(space.small))
                    FintrackBodySmallText(
                        text = stringResource(Res.string.person_label, personsText),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ---------- Description ----------
                val desc = trx.description
                if (!desc.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(space.small))
                    FintrackBodyMediumText(
                        text = stringResource(Res.string.description_with_value, desc),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ---------- Tags ----------
                if (uiTransactionWithRelation.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(space.small))

                    // نمایش چیپ‌های تگ با آیکون (اضافه)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(space.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(uiTransactionWithRelation.tags) { tag ->
                            val tagIcon = remember(tag.iconId) {
                                FinTrackIcons.findIcon(tag.iconId)
                            }
                            EntityChip(
                                name = tag.name,
                                iconRes = tagIcon.resource,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // ---------- Date ----------
                Spacer(modifier = Modifier.height(space.small))
                FintrackBodySmallText(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    text = trx.date,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

