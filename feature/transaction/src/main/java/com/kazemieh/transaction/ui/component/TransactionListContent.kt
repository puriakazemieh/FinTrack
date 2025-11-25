package com.kazemieh.transaction.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.R


@Composable
fun TransactionListContent(
    uiTransactionWithRelations: List<TransactionWithRelationsUi>,
    isLoading: Boolean = true,
    onDelete: (TransactionWithRelationsUi) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column {
                if (uiTransactionWithRelations.isNotEmpty()) {
                    FintrackTitleMediumText(
                        text = stringResource(R.string.recent_transactions),
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    uiTransactionWithRelations.forEach { transactionWithRelations ->
                        TransactionItem(
                            uiTransactionWithRelation = transactionWithRelations,
                            onDelete = { onDelete(transactionWithRelations) }
                        )
                    }
                } else EmptyListScreen()

            }
        }
    }
}
