package com.kazemieh.designsystem.component.list.normal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.ItemUi
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FAB
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.list.ItemScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListBottomSheet(
    title: String,
    items: Set<ItemUi>,
    onItemClicked: (ItemUi) -> Unit = {},
    onItemEditClicked: (ItemUi) -> Unit = {},
    onItemDeleteClicked: (ItemUi) -> Unit = {},
    onAddClick: () -> Unit,
    onDismiss: () -> Unit,
    isDeleteShow: Boolean = false,
    isEditShow: Boolean = false,
    isShowTopContent: Boolean = false,
    clickable: Boolean = true,
    topContent: @Composable () -> Unit = { },
    itemContent: @Composable (ItemUi) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val space = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(space.medium)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isShowTopContent) {
                    FintrackTitleLargeText(
                        text = title,
                        modifier = Modifier.padding(space.small)
                    )
                } else topContent()

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    if (items.isNotEmpty())
                        items(items.toList()) { item ->
                            ItemScreen(
                                item = item,
                                isDeleteShow = isDeleteShow,
                                isEditShow = isEditShow,
                                clickable = clickable,
                                onItemClicked = onItemClicked,
                                onItemEditClicked = onItemEditClicked,
                                onItemDeleteClicked = onItemDeleteClicked,
                                content = itemContent
                            )
                        } else item { EmptyListScreen(title) }

                    item {
                        Spacer(Modifier.height(space.huge))
                    }
                }

            }

            FAB(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(space.small),
                onClick = onAddClick
            )

        }
    }
}

