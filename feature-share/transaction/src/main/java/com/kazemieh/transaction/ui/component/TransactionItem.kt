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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = space.small),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = space.one)
    ) {
        Column(Modifier.padding(space.large)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackTitleMediumText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = uiTransactionWithRelation.category.name,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        modifier = Modifier.weight(0.1f),
                        imageVector = Icons.Default.Edit,
                        contentDescription = Icons.Default.Edit.name,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        modifier = Modifier.weight(0.1f),
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(space.mediumSmall))

            FintrackTitleMediumText(
                text = "${stringResource(R.string.source)} : ${uiTransactionWithRelation.source.name}",
                color = MaterialTheme.colorScheme.onSurface
            )

            if (uiTransactionWithRelation.transaction.type == TransactionType.TRANSFER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FintrackLabelSmallText(
                        text = "${
                            stringResource(R.string.source_from)
                        } : ${
                            uiTransactionWithRelation.source.name
                        } ${
                            stringResource(R.string.source_to)
                        } : ${
                            uiTransactionWithRelation.sourceEnd?.name
                        }",
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FintrackLabelSmallText(
                        text = "${stringResource(R.string.amount_transfer)} : ${uiTransactionWithRelation.transaction.amountTransferFormated} ",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(space.mediumSmall))

            val amountColor =
                if (uiTransactionWithRelation.transaction.type == TransactionType.EXPENSE)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.secondary

            val text = when (uiTransactionWithRelation.transaction.type) {
                TransactionType.INCOME -> {
                    stringResource(R.string.incoming)
                }

                TransactionType.EXPENSE -> {
                    stringResource(R.string.outcoming)
                }

                TransactionType.TRANSFER -> {
                    stringResource(R.string.transfer)
                }

                else -> {
                    stringResource(R.string.all)
                }

            }

            FintrackBodyLargeText(
                text = stringResource(
                    R.string.amount_label,
                    text,
                    uiTransactionWithRelation.transaction.formatedAmount
                ),
                color = amountColor
            )

            Spacer(modifier = Modifier.height(space.small))

            if (uiTransactionWithRelation.persons.isNotEmpty()) {
                FintrackBodySmallText(
                    text = stringResource(
                        R.string.person_label,
                        uiTransactionWithRelation.persons.joinToString { it.name }),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(space.small))

            if (!uiTransactionWithRelation.transaction.description.isNullOrEmpty()) {
                FintrackBodyMediumText(
                    text = "${stringResource(R.string.description)} :" +
                            " ${uiTransactionWithRelation.transaction.description}",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(space.small))

            if (uiTransactionWithRelation.tags.isNotEmpty()) {
                FintrackBodySmallText(
                    text = stringResource(
                        R.string.tags_label,
                        uiTransactionWithRelation.tags.joinToString { it.name }),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(space.small))

            FintrackBodySmallText(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                text = uiTransactionWithRelation.transaction.date,
                color = MaterialTheme.colorScheme.onSurface
            )

        }


    }
}

