package com.kazemieh.category.ui.add

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.common.model.Category
import com.kazemieh.common.model.TransactionType
import com.kazemieh.designsystem.R
import com.kazemieh.designsystem.component.bottomsheet.FormBottomSheetScaffold
import com.kazemieh.designsystem.component.form.NameDescriptionFields
import com.kazemieh.designsystem.picker.ColorIconPickerBottomSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(transactionType) {
        viewModel.onIntent(AddCategoryIntent.SetCategoryType(transactionType))
    }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory != null) {
            viewModel.onIntent(AddCategoryIntent.ShowEditData(selectedCategory))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddCategoryEffect.ShowMessage -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.message),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is AddCategoryEffect.AddedCategory -> setCategory(effect.category)
                AddCategoryEffect.OnDismiss -> onDismiss()
            }
        }
    }

    FormBottomSheetScaffold(
        sheetState = sheetState,
        onDismissRequest = { viewModel.onIntent(AddCategoryIntent.OnDismiss) },
        primaryButtonText = stringResource(R.string.save_category),
        onPrimaryClick = { viewModel.onIntent(AddCategoryIntent.AddCategory) }
    ) {
        NameDescriptionFields(
            name = state.categoryName.orEmpty(),
            onNameChange = { viewModel.onIntent(AddCategoryIntent.SetCategoryName(it)) },
            nameLabel = stringResource(R.string.category_name_label),
            description = state.description.orEmpty(),
            onDescriptionChange = { viewModel.onIntent(AddCategoryIntent.SetDescription(it)) },
            descriptionLabel = stringResource(R.string.description_label),
            initialColorId = null,
            initialIconId = null,
            isIconShow = true,
        )


        ColorIconPickerBottomSheet(
            initialColorId = null,
            initialIconId = null,
            onDismiss = {
            },
            onSave = { color, icon ->
                //   color.id + icon.id
            }
        )
    }
}