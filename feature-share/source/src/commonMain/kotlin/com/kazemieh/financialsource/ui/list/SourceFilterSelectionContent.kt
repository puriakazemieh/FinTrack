package com.kazemieh.financialsource.ui.list

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.model.Source
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.Chip
import com.kazemieh.financialsource.ui.add.AddSourceBottomSheet
import com.kazemieh.financialsource.ui.delete.DeleteSourceBottomSheet
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SourceFilterSelectionContent(
    viewModel: SourceViewModel = koinViewModel(key = "SourceFilterSelectionContent"),
    snackbarHostState: SnackbarHostState,
    selectedSources: Set<Source>,
    isAllSelected: Boolean,
    onSelectionChanged: (Set<Source>, Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.onIntent(SourceIntent.LoadAllSource)
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
                    text = stringResource(Res.string.financial_sources),
                    color = GlassText2,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = GlassBlue,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { viewModel.onIntent(SourceIntent.OnAddSourceClick) }
                )
            }
            FintrackLabelSmallText(
                text = stringResource(Res.string.all),
                color = if (isAllSelected) GlassGreen else GlassText3,
                modifier = Modifier.clickable {
                    val all = state.sources.toSet()
                    onSelectionChanged(if (isAllSelected) emptySet() else all, !isAllSelected)
                }
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.sources.forEach { source ->
                val active = isAllSelected || selectedSources.contains(source)
                Chip(
                    active = active,
                    color = GlassBlue,
                    onClick = {
                        val fullSet = state.sources.toSet()
                        val currentSelected = if (isAllSelected) fullSet else selectedSources
                        val newSet = currentSelected.toMutableSet()
                        
                        if (newSet.contains(source)) {
                            newSet.remove(source)
                        } else {
                            newSet.add(source)
                        }
                        
                        val isAllNow = newSet.size == fullSet.size
                        onSelectionChanged(if (isAllNow) emptySet() else newSet, isAllNow)
                    },
                    onLongClick = { viewModel.onIntent(SourceIntent.OnEditClick(source)) }
                ) {
                    FintrackLabelSmallText(
                        text = source.name + if (active) " ✓" else "",
                        color = if (active) GlassBg0 else GlassText2
                    )
                }
            }
        }
    }

    if (state.isAddShow) {
        AddSourceBottomSheet(
            snackbarHostState = snackbarHostState,
            selectedSource = state.selectedSources,
            onDismiss = { viewModel.onIntent(SourceIntent.OnAddSourceClick) },
            setSource = { viewModel.onIntent(SourceIntent.OnAddSourceClick) }
        )
    }

    if (state.isDeleteShow && state.selectedSources != null) {
        DeleteSourceBottomSheet(
            snackbarHostState = snackbarHostState,
            source = state.selectedSources!!,
            onDismiss = { viewModel.onIntent(SourceIntent.OnDeleteClick()) },
            deleted = { viewModel.onIntent(SourceIntent.OnDeleteClick()) }
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
