package com.kazemieh.tag.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Tag
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.tag.ui.add.AddTagBottomSheet
import com.kazemieh.tag.ui.delete.DeleteTagBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TagFilterSelectionContent(
    viewModel: TagViewModel = koinViewModel(key = "TagFilterSelectionContent"),
    snackbarHostState: SnackbarHostState,
    selectedTags: Set<Tag>,
    isAllSelected: Boolean,
    onSelectionChanged: (Set<Tag>, Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(TagIntent.GetAllTag)
    }

    val state by viewModel.state.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FintrackLabelSmallText(
                    text = stringResource(Res.string.tags),
                    color = GlassText2,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = GlassAmber,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { viewModel.onIntent(TagIntent.ShowAddTag) }
                )
            }
            FintrackLabelSmallText(
                text = stringResource(Res.string.all),
                color = if (isAllSelected) GlassGreen else GlassText3,
                modifier = Modifier.clickable {
                    val all = state.tags.toSet()
                    onSelectionChanged(if (isAllSelected) emptySet() else all, !isAllSelected)
                }
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.tags.forEach { tag ->
                val active = isAllSelected || selectedTags.contains(tag)
                Chip(
                    active = active,
                    color = GlassAmber,
                    onClick = {
                        val fullSet = state.tags.toSet()
                        val currentSelected = if (isAllSelected) fullSet else selectedTags
                        val newSet = currentSelected.toMutableSet()
                        
                        if (newSet.contains(tag)) {
                            newSet.remove(tag)
                        } else {
                            newSet.add(tag)
                        }
                        
                        val isAllNow = newSet.size == fullSet.size
                        onSelectionChanged(if (isAllNow) emptySet() else newSet, isAllNow)
                    },
                    onLongClick = { viewModel.onIntent(TagIntent.OnEditClick(tag)) }
                ) {
                    FintrackLabelSmallText(
                        text = tag.name + if (active) " ✓" else "",
                        color = if (active) Color.White else GlassText2
                    )
                }
            }
        }
    }

    if (state.showAddTag) {
        AddTagBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedTag = state.selectedTag,
            onDismiss = { viewModel.onIntent(TagIntent.ShowAddTag) },
            setTag = { viewModel.onIntent(TagIntent.SetSelectedTag(it)) }
        )
    }

    if (state.isDeleteShow && state.selectedTag != null) {
        DeleteTagBottomSheet(
            snackbarHostState = snackbarHostState,
            tag = state.selectedTag!!,
            onDismiss = { viewModel.onIntent(TagIntent.OnDeleteClick()) },
            deleted = { viewModel.onIntent(TagIntent.OnDeleteClick()) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
