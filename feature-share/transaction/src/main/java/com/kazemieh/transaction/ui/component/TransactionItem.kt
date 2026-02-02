package com.kazemieh.transaction.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.common.model.TransactionType
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText


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
    val tagsText = remember(uiTransactionWithRelation.tags) {
        uiTransactionWithRelation.tags.joinToString { it.name }
    }

    val typeLabel = when (trx.type) {
        TransactionType.INCOME -> stringResource(R.string.incoming)
        TransactionType.EXPENSE -> stringResource(R.string.outcoming)
        TransactionType.TRANSFER -> stringResource(R.string.transfer)
        else -> stringResource(R.string.all)
    }

    val amountColor =
        if (trx.type == TransactionType.EXPENSE) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.secondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = space.small),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = space.one)
    ) {
        Column(Modifier.padding(space.large)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackTitleMediumText(
                    modifier = Modifier.weight(1f),
                    text = uiTransactionWithRelation.category.name,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(space.mediumSmall))

            // ---------- Source / Transfer Info ----------
            if (isTransfer) {

                FintrackTitleMediumText(
                    text = stringResource(
                        R.string.transfer_from_to_label,
                        uiTransactionWithRelation.source.name,
                        uiTransactionWithRelation.sourceEnd?.name.orEmpty()
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(space.small))

                trx.amountTransferFormated?.let {
                    FintrackLabelSmallText(
                        text = stringResource(
                            R.string.amount_transfer_label,
                            it
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                FintrackTitleMediumText(
                    text = stringResource(R.string.source_with_value, uiTransactionWithRelation.source.name),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(space.mediumSmall))

            // ---------- Amount ----------
            FintrackBodyLargeText(
                text = stringResource(
                    R.string.amount_label,
                    typeLabel,
                    trx.formatedAmount
                ),
                color = amountColor
            )

            // ---------- Persons ----------
            if (uiTransactionWithRelation.persons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(space.small))
                FintrackBodySmallText(
                    text = stringResource(R.string.person_label, personsText),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---------- Description ----------
            val desc = trx.description
            if (!desc.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(space.small))
                FintrackBodyMediumText(
                    text = stringResource(R.string.description_with_value, desc),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ---------- Tags ----------
            if (uiTransactionWithRelation.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(space.small))
                FintrackBodySmallText(
                    text = stringResource(R.string.tags_label, tagsText),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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


