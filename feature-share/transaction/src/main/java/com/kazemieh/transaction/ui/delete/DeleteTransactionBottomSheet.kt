package com.kazemieh.transaction.ui.delete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTransactionBottomSheet(
    viewModel: DeleteTransactionViewModel = koinViewModel(),
    transactionWithRelations: TransactionWithRelations? = null,
    onDismiss: () -> Unit,
    transactionDeleted: () -> Unit
) {

    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(transactionWithRelations) {
        viewModel.onIntent(DeleteTransactionIntent.SetData(transactionWithRelations))
    }


    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DeleteTransactionEffect.DeletedTransaction -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            transactionDeleted()
                        }
                    }
                }

                is DeleteTransactionEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                DeleteTransactionEffect.OnDismiss -> onDismiss()
            }
        }
    }

    BottomSheetContent(viewModel::onIntent, sheetState, snackbarHostState)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(
    onIntent: (intent: DeleteTransactionIntent) -> Unit,
    sheetState: SheetState,
    snackbarHostState: SnackbarHostState
) {

    val space = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = { onIntent(DeleteTransactionIntent.OnDismiss) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
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
                        onClick = { onIntent(DeleteTransactionIntent.Submit) },
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
                        onClick = { onIntent(DeleteTransactionIntent.OnDismiss) },
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

            Box {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

        }
    }
}

