package com.kazemieh.designsystem.component.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.*
import com.kazemieh.designsystem.component.*
import com.kazemieh.designsystem.component.glass.*
import com.kazemieh.designsystem.component.model.ItemUi
import com.kazemieh.designsystem.component.model.asString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.add_new_item
import fintrack.core.designsystem.generated.resources.confirm
import fintrack.core.designsystem.generated.resources.search_placeholder
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectableFlowRowBottomSheet(
    title: String,
    items: Set<ItemUi>,
    initialSelection: Set<ItemUi> = emptySet(),
    onConfirm: (selectedItem: Set<ItemUi>) -> Unit,
    onAddClick: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val space = LocalSpacing.current

    val itemsList = remember(items) { items.toList().sortedBy { it.id } }

    var selected by remember(items, initialSelection) {
        mutableStateOf(initialSelection.intersect(items))
    }

    var searchQuery by remember { mutableStateOf("") }

    // Resolve titles in a composable way
    val resolvedItems = itemsList.map { it to it.title.asString() }

    val filteredItems = remember(resolvedItems, searchQuery) {
        if (searchQuery.isEmpty()) resolvedItems
        else resolvedItems.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(initialSelection, items) {
        selected = initialSelection.intersect(items)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GlassBg1, GlassBg0)))
        ) {
            ScreenHeader(
                title = title,
                onClose = onDismiss
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = stringResource(Res.string.search_placeholder),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(space.mediumSmall),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (filteredItems.isEmpty()) {
                        item {
                            EmptyListScreen(title)
                        }
                    } else {
                        items(filteredItems) { (item, resolvedTitle) ->
                            val isSelected = selected.contains(item)
                            
                            GlassCard(
                                onClick = {
                                    selected = if (isSelected) selected - item else selected + item
                                },
                                padding = 10.dp,
                                tone = if (isSelected) GlassTone.Strong else GlassTone.Default,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (item.iconId != null || item.colorId != null) {
                                        FinTrackLeadingIcon(
                                            colorId = item.colorId ?: 1,
                                            iconId = item.iconId ?: 1,
                                            style = LeadingIconStyle.Badge,
                                            size = 32.dp,
                                            iconSize = 14.dp,
                                            corner = 10.dp
                                        )
                                        Spacer(Modifier.width(10.dp))
                                    }

                                    FintrackBodyMediumText(
                                        text = resolvedTitle,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) GlassText else GlassText2,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isSelected) GlassGreen else GlassEdgeStrong,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .background(if (isSelected) GlassGreen else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = GlassGreenDark,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Add new at the bottom
                AddButton(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = GlassGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    FintrackBodyMediumText(
                        text = stringResource(Res.string.add_new_item),
                        fontWeight = FontWeight.Bold,
                        color = GlassGreen
                    )
                }
            }

            // Footer / CTAs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onConfirm(selected) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    FintrackTitleMediumText(
                        text = "${stringResource(Res.string.confirm)} (${selected.size.toLong().toPersianDigits()})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(1.5.dp, GlassEdgeStrong, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}
