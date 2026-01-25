package com.kazemieh.transaction.ui.delete

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.kazemieh.common.model.TransactionWithRelations
import com.kazemieh.designsystem.component.DeleteBottomSheet
import kotlinx.coroutines.launch
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

    DeleteBottomSheet(
        dismissClicked = { viewModel.onIntent(DeleteTransactionIntent.OnDismiss) },
        confirmClicked = { viewModel.onIntent(DeleteTransactionIntent.Submit) },
        sheetState = sheetState
    )

}
