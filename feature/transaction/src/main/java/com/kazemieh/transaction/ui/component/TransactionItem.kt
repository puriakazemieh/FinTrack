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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.model.TransactionType
import com.kazemieh.transaction.R


@Composable
fun TransactionItem(
    uiTransactionWithRelation: TransactionWithRelationsUi,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FintrackTitleMediumText(
                    text = uiTransactionWithRelation.categoryName,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            FintrackTitleMediumText(
                text = "${stringResource(R.string.source)} : ${uiTransactionWithRelation.financialSourceName}",
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            val amountColor =
                if (uiTransactionWithRelation.transaction.amount >= 0)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.error

            val type = if (uiTransactionWithRelation.transaction.type == TransactionType.INCOME)
                stringResource(R.string.incoming)
            else stringResource(R.string.outcoming)

            FintrackBodyLargeText(
                text = stringResource(
                    R.string.amount_label,
                    type,
                    uiTransactionWithRelation.transaction.formatedAmount
                ),
                color = amountColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (uiTransactionWithRelation.tags.isNotEmpty()) {
                FintrackBodySmallText(
                    text = stringResource(R.string.tags_label, uiTransactionWithRelation.tags),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }


    }
}

