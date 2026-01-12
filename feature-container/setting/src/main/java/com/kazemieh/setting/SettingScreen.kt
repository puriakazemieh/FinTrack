package com.kazemieh.setting

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kazemieh.category.ui.CategoryTabListBottomSheet
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.list.ItemScreen
import com.kazemieh.financialsource.ui.SourceListBottomSheet
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = koinViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    val space = LocalSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.menuItems) { item ->
            ItemScreen(item, onItemClicked = { viewModel.onIntent(SettingIntent.OnClick(item)) })
        }
    }

    when (state.selectedItem?.id) {
        ItemId.ITEM_1.id -> {
            CategoryTabListBottomSheet(
                snackbarHostState = snackbarHostState,
//                onCategoryClick = { viewModel.onIntent(SettingIntent.SetCategory(it)) },
                onDismiss = { viewModel.onIntent(SettingIntent.OnClick()) }
            )
        }

        ItemId.ITEM_2.id -> {
            SourceListBottomSheet(
                snackbarHostState = snackbarHostState,
                onSourceClick = { viewModel.onIntent(SettingIntent.SetSource(it)) },
                onDismiss = { viewModel.onIntent(SettingIntent.OnClick()) }
            )
        }

        ItemId.ITEM_3.id -> {
            /*TagListBottomSheet(
                snackbarHostState = snackbarHostState,
                selectedTags = state.tags,
                onSubmitClick = { viewModel.onIntent(SettingIntent.SetTags(it)) },
                onDismiss = { viewModel.onIntent(SettingIntent.OnClick()) }
            )*/
        }

        ItemId.ITEM_4.id -> {
            /* PersonListBottomSheet(
                 snackbarHostState = snackbarHostState,
                 selectedPersons = state.persons,
                 onSubmitClick = { viewModel.onIntent(SettingIntent.SetPerson(it)) },
                 onDismiss = { viewModel.onIntent(SettingIntent.OnClick()) }
             )*/
        }
    }

}
