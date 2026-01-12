package com.kazemieh.designsystem.component


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContent(
    dismiss: () -> Unit,
    submit: () -> Unit,
    sheetState: SheetState,
) {

    val space = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column {
            FintrackTitleLargeText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = space.large),
                text = stringResource(R.string.transaction_delete),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(space.large))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(space.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space.mediumSmall)
            ) {
                Button(
                    onClick = submit,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    FintrackTitleMediumText(
                        text = stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.background
                    )
                }
                Button(
                    onClick = dismiss,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackTitleMediumText(
                        text = stringResource(R.string.cancell_),
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    }
}
