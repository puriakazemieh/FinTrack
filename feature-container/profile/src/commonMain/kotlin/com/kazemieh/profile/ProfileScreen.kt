package com.kazemieh.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.category.ui.list.CategoryManageBottomSheet
import com.kazemieh.designsystem.component.ItemScreen
import com.kazemieh.financialsource.ui.list.SourceManageBottomSheet
import com.kazemieh.person.ui.list.PersonManageBottomSheet
import com.kazemieh.tag.ui.list.TagManageBottomSheet
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState,
    onNavigateToTransactions: (Any) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.menuItems) { item ->
            ItemScreen(
                item = item,
                onItemClicked = { viewModel.onIntent(SettingIntent.OnClick(item)) }
            )
        }
    }

    when (state.selectedItem?.id) {
        ItemId.ITEM_1.id -> {
            CategoryManageBottomSheet(
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(SettingIntent.OnClick()) },
                onNavigateToTransactions = { category ->
                    onNavigateToTransactions(category)
                }
            )
        }

        ItemId.ITEM_2.id -> {
            SourceManageBottomSheet(
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(SettingIntent.OnClick()) },
                onNavigateToTransactions = { source ->
                    onNavigateToTransactions(source)
                }
            )
        }

        ItemId.ITEM_3.id -> {
            TagManageBottomSheet(
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(SettingIntent.OnClick()) },
                onNavigateToTransactions = { tag ->
                    onNavigateToTransactions(tag)
                }
            )
        }

        ItemId.ITEM_4.id -> {
            PersonManageBottomSheet(
                snackbarHostState = snackbarHostState,
                onDismiss = { viewModel.onIntent(SettingIntent.OnClick()) },
                onNavigateToTransactions = { person ->
                    onNavigateToTransactions(person)
                }
            )
        }
    }
}

