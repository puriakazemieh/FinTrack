package com.kazemieh.category.ui.delete


import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.kazemieh.category.ui.list.CategoryManageBottomSheet
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.bottomsheet.DeleteWithMoveBottomSheetContent
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(category) {
        viewModel.onIntent(DeleteCategoryIntent.SetData(category))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DeleteCategoryEffect.DeletedTransaction -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) deleted()
                    }
                }

                is DeleteCategoryEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                DeleteCategoryEffect.OnDismiss -> onDismiss()
            }
        }
    }

    DeleteWithMoveBottomSheetContent(
        sheetState = sheetState,
        title = stringResource(R.string.transaction_delete),
        deleteAllText = stringResource(R.string.delete_all_transaction),
        moveToAnotherText = stringResource(R.string.move_to_another_category),
        targetTitleText = stringResource(R.string.categories),
        targetPlaceholderText = stringResource(R.string.select_category),
        targetValue = state.moveCategory?.name,
        isDeleteAll = state.isDeleteAllData,
        isTargetError = state.isCategoryError,
        onSelectDeleteAll = { viewModel.onIntent(DeleteCategoryIntent.DeleteAllTransaction) },
        onSelectMove = { viewModel.onIntent(DeleteCategoryIntent.MoveAllTransaction) },
        onPickTarget = { viewModel.onIntent(DeleteCategoryIntent.ShowAllCategoryList) },
        onConfirm = { viewModel.onIntent(DeleteCategoryIntent.Submit) },
        onDismiss = { viewModel.onIntent(DeleteCategoryIntent.Dismiss) },
        confirmButtonText = stringResource(R.string.confirm),
        dismissButtonText = stringResource(R.string.cancell_),
    )

    if (state.isCategoryListShow) {
        CategoryPickerBottomSheet(
            transactionType = category.type,
            snackbarHostState = snackbarHostState,
            onCategoryClick = { viewModel.onIntent(DeleteCategoryIntent.SetMoveCategory(it)) },
            onDismiss = { viewModel.onIntent(DeleteCategoryIntent.ShowAllCategoryList) }
        )
    }
}