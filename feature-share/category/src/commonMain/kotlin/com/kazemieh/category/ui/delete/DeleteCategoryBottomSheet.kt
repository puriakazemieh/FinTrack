package com.kazemieh.category.ui.delete


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.kazemieh.category.ui.list.CategoryPickerBottomSheet
import com.kazemieh.common.model.Category
import com.kazemieh.designsystem.component.bottomsheet.DeleteWithMoveBottomSheetContent
import com.kazemieh.designsystem.component.model.resolveString
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.transaction_delete
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

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
                            message = effect.message.resolveString(),
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
        title = stringResource(Res.string.transaction_delete),
        deleteAllText = stringResource(Res.string.delete_all_transaction),
        moveToAnotherText = stringResource(Res.string.move_to_another_category),
        targetTitleText = stringResource(Res.string.categories),
        targetPlaceholderText = stringResource(Res.string.select_category),
        targetValue = state.moveCategory?.name,
        isDeleteAll = state.isDeleteAllData,
        isTargetError = state.isCategoryError,
        onSelectDeleteAll = { viewModel.onIntent(DeleteCategoryIntent.DeleteAllTransaction) },
        onSelectMove = { viewModel.onIntent(DeleteCategoryIntent.MoveAllTransaction) },
        onPickTarget = { viewModel.onIntent(DeleteCategoryIntent.ShowAllCategoryList) },
        onConfirm = { viewModel.onIntent(DeleteCategoryIntent.Submit) },
        onDismiss = { viewModel.onIntent(DeleteCategoryIntent.Dismiss) },
        confirmButtonText = stringResource(Res.string.confirm),
        dismissButtonText = stringResource(Res.string.cancell_),
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