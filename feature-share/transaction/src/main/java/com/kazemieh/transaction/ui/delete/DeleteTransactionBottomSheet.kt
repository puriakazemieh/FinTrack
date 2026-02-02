package com.kazemieh.transaction.ui.delete

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.component.DeleteBottomSheet
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTransactionBottomSheet(
    viewModel: DeleteTransactionViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    transactionWithRelations: TransactionWithRelations? = null,
    onDismiss: () -> Unit,
    transactionDeleted: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(transactionWithRelations?.transaction?.id) {
        viewModel.onIntent(DeleteTransactionIntent.SetData(transactionWithRelations))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                DeleteTransactionEffect.DeletedTransaction -> {
                    sheetState.hide()
                    transactionDeleted()
                    onDismiss()
                }

                is DeleteTransactionEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(effect.message),
                        duration = SnackbarDuration.Short
                    )
                }

                DeleteTransactionEffect.OnDismiss -> {
                    sheetState.hide()
                    onDismiss()
                }
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    DeleteBottomSheet(
        dismissClicked = { viewModel.onIntent(DeleteTransactionIntent.OnDismiss) },
        confirmClicked = { viewModel.onIntent(DeleteTransactionIntent.Submit) },
        sheetState = sheetState,
        confirmEnabled = !state.isLoading,
        dismissEnabled = !state.isLoading,
        isLoading = state.isLoading
    )
}
