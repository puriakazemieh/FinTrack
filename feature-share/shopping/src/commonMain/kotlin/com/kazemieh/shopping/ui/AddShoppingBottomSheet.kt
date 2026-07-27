package com.kazemieh.shopping.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kazemieh.common.model.ShoppingItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddShoppingBottomSheet(
    item: ShoppingItem? = null,
    onDismiss: () -> Unit,
    viewModel: ShoppingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(item) {
        viewModel.onIntent(ShoppingIntent.OnEditItem(item))
    }

    ShoppingItemSheet(
        item = item,
        initialCategory = state.editingCategory,
        mostUsedCategories = state.mostUsedCategories,
        mostUsedTags = state.mostUsedTags,
        onDismiss = onDismiss,
        onConfirm = {
            if (item == null) {
                viewModel.onIntent(ShoppingIntent.OnSaveNewItem(it))
            } else {
                viewModel.onIntent(ShoppingIntent.OnUpdateItem(it))
            }
            onDismiss()
        },
        onSaveAndNext = { viewModel.onIntent(ShoppingIntent.OnSaveAndNext(it)) }
    )
}
