package com.kazemieh.designsystem.component.list.normal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kazemieh.common.model.ItemUi
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.EmptyListScreen
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackTitleLargeText


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListBottomSheet(
    title: String,
    items: Set<ItemUi>,
    onConfirm: (ItemUi) -> Unit,
    onAddClick: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable (ItemUi) -> Unit = {}
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
                FintrackTitleLargeText(
                    text = title,
                    modifier = Modifier.padding(
                        vertical = space.small,
                        horizontal = space.extraSmall
                    )
                )
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    if (items.isNotEmpty())
                        items(items.toList()) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = space.extraSmall)
                                    .clickable { onConfirm(item) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(defaultElevation = space.one)
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        vertical = space.medium,
                                        horizontal = space.large
                                    )
                                ) {
                                    FintrackBodyMediumText(text = item.title)
                                    content(item)
                                }
                            }
                        } else item { EmptyListScreen(title) }

                    item {
                        Spacer(Modifier.height(space.extraLarge))
                    }
                }

            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(space.extraSmall)
            ) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomStart),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = title
                    )
                }
            }
            Spacer(Modifier.height(space.small))
        }
    }
}

