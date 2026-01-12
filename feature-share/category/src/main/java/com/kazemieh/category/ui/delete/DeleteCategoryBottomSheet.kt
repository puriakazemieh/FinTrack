package com.kazemieh.category.ui.delete

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.kazemieh.common.model.Category
import com.kazemieh.designsystem.component.BottomSheetContent
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteCategoryBottomSheet(
    viewModel: DeleteCategoryViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    category: Category,
    onDismiss: () -> Unit,
    deleted: () -> Unit
) {

    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(category) {
        viewModel.onIntent(DeleteCategory.SetData(category))
    }


    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DeleteTransactionEffect.DeletedTransaction -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            deleted()
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

    BottomSheetContent(
        dismiss = {},
        submit = {},
        sheetState = sheetState
    )

}

