package com.kazemieh.category.ui.add

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.component.bottomsheet.FormBottomSheetScaffold
import com.kazemieh.designsystem.component.form.NameDescriptionFields
import com.kazemieh.designsystem.component.model.resolveString
import com.kazemieh.designsystem.picker.ColorIconPickerBottomSheet
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.category_name_label
import fintrack.core.designsystem.generated.resources.description_label
import fintrack.core.designsystem.generated.resources.save_category
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryBottomSheet(
    viewModel: AddCategoryViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    transactionType: TransactionType,
    selectedCategory: Category? = null,
    onDismiss: () -> Unit,
    setCategory: (Category) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(selectedCategory?.id, transactionType) {
        if (selectedCategory != null) {
            viewModel.onIntent(AddCategoryIntent.StartEdit(selectedCategory))
        } else {
            viewModel.onIntent(AddCategoryIntent.StartAdd(transactionType))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddCategoryEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message.resolveString(),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is AddCategoryEffect.SavedCategory -> {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            setCategory(effect.category)
                            onDismiss()
                        }
                    }
                }
                AddCategoryEffect.OnDismiss -> onDismiss()
            }
        }
    }

    FormBottomSheetScaffold(
        sheetState = sheetState,
        onDismissRequest = { viewModel.onIntent(AddCategoryIntent.OnDismiss) },
        primaryButtonText = stringResource(Res.string.save_category),
        onPrimaryClick = { viewModel.onIntent(AddCategoryIntent.Save) }
    ) {
        NameDescriptionFields(
            name = state.draft.name,
            onNameChange = { viewModel.onIntent(AddCategoryIntent.UpdateName(it)) },
            nameLabel = stringResource(Res.string.category_name_label),

            description = state.draft.description.orEmpty(),
            onDescriptionChange = { viewModel.onIntent(AddCategoryIntent.UpdateDescription(it)) },
            descriptionLabel = stringResource(Res.string.description_label),

            initialColorId = state.draft.colorId,
            initialIconId = state.draft.iconId,
            isIconShow = true,

            onIconClick = { viewModel.onIntent(AddCategoryIntent.OpenPicker) }
        )

        if (state.isPickerOpen) {
            ColorIconPickerBottomSheet(
                initialColorId = state.draft.colorId,
                initialIconId = state.draft.iconId,
                onDismiss = { viewModel.onIntent(AddCategoryIntent.ClosePicker) },
                onSave = { color, icon ->
                    viewModel.onIntent(AddCategoryIntent.SetColorIcon(color.id, icon.id))
                }
            )
        }
    }
}
