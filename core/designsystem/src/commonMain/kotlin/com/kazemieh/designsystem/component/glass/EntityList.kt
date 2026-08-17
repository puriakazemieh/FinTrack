package com.kazemieh.designsystem.component.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.toPersianDigits
import com.kazemieh.designsystem.GlassGreen
import com.kazemieh.designsystem.GlassRed
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.component.EmptyList
import com.kazemieh.designsystem.component.FinTrackLeadingIcon
import com.kazemieh.designsystem.component.FintrackBodySmallText
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackLabelSmallText
import com.kazemieh.designsystem.component.FintrackTitleMediumText
import com.kazemieh.designsystem.component.FintrackTitleSmallText
import com.kazemieh.designsystem.component.LeadingIconStyle
import com.kazemieh.designsystem.component.model.UiText
import com.kazemieh.designsystem.component.model.asString
import fintrack.core.designsystem.generated.resources.Res
import fintrack.core.designsystem.generated.resources.hint_search_in
import fintrack.core.designsystem.generated.resources.label_add_specific_new_entity
import fintrack.core.designsystem.generated.resources.msg_empty_list
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 2.9 EntityList — management list with summary header + search + edit/delete rows
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntityList(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = GlassGreen,
    addLabel: String? = null,
    summary: List<EntitySummary>? = null,
    items: List<EntityItem>,
    onEditClick: (EntityItem) -> Unit,
    onDeleteClick: (EntityItem) -> Unit,
    onFilterClick: ((EntityItem) -> Unit)? = null,
    onItemClick: (EntityItem) -> Unit = {},
    onExpandClick: ((EntityItem) -> Unit)? = null,
    emptyHint: String? = null,
    showActions: Boolean = true,
    showAddFab: Boolean = true,
    isReorderMode: Boolean = false,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    indentSubCategories: Boolean = false,
    parentId: Long? = null,
    searchTrailing: (@Composable () -> Unit)? = null,
    itemGroups: List<EntityItemGroup>? = null
) {
    var internalItems by remember(items.map { it.id }) { mutableStateOf(items) }

    Box(modifier = modifier.fillMaxSize()) {
        if (isReorderMode) {
            val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
            val state = rememberReorderableLazyListState(
                lazyListState = lazyListState,
                onMove = { from, to ->
                    val fromIdx = from.index
                    val toIdx = to.index
                    if (fromIdx >= 0 && toIdx >= 0 && fromIdx < internalItems.size && toIdx < internalItems.size) {
                        internalItems = internalItems.toMutableList().apply {
                            add(toIdx, removeAt(fromIdx))
                        }
                        onMove(fromIdx, toIdx)
                    }
                }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                SummaryHeader(summary)
                SearchHeader(query, onQueryChange, title, searchTrailing)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(internalItems, key = { it.id }) { item ->
                        ReorderableItem(state, key = item.id) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                            EntityRow(
                                item = item,
                                mainColor = color,
                                showActions = false,
                                onEdit = {},
                                onDelete = {},
                                onClick = {},
                                isReorderMode = true,
                                onMoveUp = {
                                    val idx = internalItems.indexOf(item)
                                    if (idx > 0) {
                                        internalItems = internalItems.toMutableList().apply {
                                            add(idx - 1, removeAt(idx))
                                        }
                                        onMove(idx, idx - 1)
                                    }
                                },
                                onMoveDown = {
                                    val idx = internalItems.indexOf(item)
                                    if (idx < internalItems.size - 1) {
                                        internalItems = internalItems.toMutableList().apply {
                                            add(idx + 1, removeAt(idx))
                                        }
                                        onMove(idx, idx + 1)
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                                    .shadow(elevation)
                                    .draggableHandle()
                            )
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                SummaryHeader(summary)
                SearchHeader(query, onQueryChange, title, searchTrailing)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    val groups = itemGroups ?: listOf(EntityItemGroup(items = items))
                    if (groups.all { it.items.isEmpty() }) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                                EmptyList(emptyHint ?: stringResource(Res.string.msg_empty_list))
                            }
                        }
                    } else {
                        groups.forEach { group ->
                            if (group.title != null && group.items.isNotEmpty()) {
                                stickyHeader { EntityGroupHeader(group.title) }
                            }
                            items(group.items) { item ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(200)),
                                    exit = fadeOut(tween(200))
                                ) {
                                    EntityRow(
                                        item = item,
                                        mainColor = color,
                                        showActions = showActions,
                                        onEdit = { onEditClick(item) },
                                        onDelete = { onDeleteClick(item) },
                                        onFilter = onFilterClick?.let { callback -> { callback(item) } },
                                        onClick = { onItemClick(item) },
                                        onExpand = onExpandClick?.let { callback -> { callback(item) } },
                                        isSubCategory = indentSubCategories && item.parentId != null,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!isReorderMode && showAddFab) {
            Fab(
                label = addLabel ?: stringResource(Res.string.label_add_specific_new_entity, title.split(" ").first()),
                icon = rememberVectorPainter(Icons.Default.Add),
                onClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            )
        }
    }
}

@Composable
fun SummaryHeader(summary: List<EntitySummary>?) {
    val glassColors = LocalGlassColors.current
    if (summary != null) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            tone = GlassTone.Strong,
            padding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                summary.forEachIndexed { index, s ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        FintrackLabelSmallText(text = s.label.asString(), color = glassColors.text3)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FintrackTitleMediumText(
                                text = s.value.toPersianDigits(),
                                fontWeight = FontWeight.Bold,
                                color = s.color ?: glassColors.text
                            )
                            s.unit?.let {
                                FintrackLabelSmallText(
                                    text = it,
                                    color = glassColors.text3,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                    if (index < summary.lastIndex) {
                        VerticalDivider(
                            modifier = Modifier.height(30.dp),
                            color = glassColors.glassHairline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    title: String,
    trailing: (@Composable () -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(Res.string.hint_search_in, title),
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@Composable
private fun EntityGroupHeader(title: String) {
    val glassColors = LocalGlassColors.current
    FintrackBodyLargeText(
        text = title,
        fontWeight = FontWeight.Bold,
        color = glassColors.text,
        modifier = Modifier.stickyHeaderSurface().padding(horizontal = 14.dp, vertical = 9.dp)
    )
}

@Composable
fun EntityRow(
    item: EntityItem,
    mainColor: Color,
    showActions: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFilter: (() -> Unit)? = null,
    onClick: () -> Unit,
    onExpand: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isReorderMode: Boolean = false,
    isSubCategory: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {}
) {
    val glassColors = LocalGlassColors.current
    val color = item.color ?: mainColor
    GlassCard(
        modifier = modifier
            .padding(start = if (isSubCategory) 24.dp else 0.dp)
            .clickable(enabled = !isReorderMode, onClick = onClick),
        padding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isReorderMode) {
                Icon(
                    imageVector = Icons.Default.Reorder,
                    contentDescription = null,
                    tint = glassColors.text3,
                    modifier = Modifier.size(20.dp)
                )
            }

            FinTrackLeadingIcon(
                colorId = item.colorId,
                iconId = item.iconId,
                style = LeadingIconStyle.Badge,
                size = 38.dp,
                iconSize = 16.dp,
                corner = 12.dp
            )

            if (item.isExpandable && onExpand != null && !isReorderMode) {
                ActionIcon(
                    icon = if (item.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    onClick = onExpand,
                    color = glassColors.text2
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FintrackTitleSmallText(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.text,
                    maxLines = 1
                )
                item.badge?.let {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.12f))
                            .then(
                                if (onExpand != null) Modifier.clickable(onClick = onExpand)
                                else Modifier
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        FintrackLabelSmallText(
                            text = it,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
                item.sub?.let {
                    FintrackBodySmallText(
                        text = it,
                        color = glassColors.text3,
                        maxLines = 1
                    )
                }
                item.sub2?.let {
                    FintrackBodySmallText(
                        text = it,
                        color = color.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            item.trailingContent?.let { it() }

            if (isReorderMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionIcon(
                        icon = Icons.Default.KeyboardArrowUp,
                        onClick = onMoveUp,
                        color = glassColors.text2
                    )
                    ActionIcon(
                        icon = Icons.Default.KeyboardArrowDown,
                        onClick = onMoveDown,
                        color = glassColors.text2
                    )
                }
            } else if (showActions) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onFilter?.let {
                        ActionIcon(
                            icon = Icons.Default.FilterList,
                            onClick = it,
                            color = GlassGreen
                        )
                    }
                    ActionIcon(icon = Icons.Default.Edit, onClick = onEdit, color = glassColors.text2)
                    ActionIcon(icon = Icons.Default.Delete, onClick = onDelete, color = GlassRed)
                }
            }
        }
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(glassColors.glass)
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
    }
}

data class EntitySummary(
    val label: UiText,
    val value: String,
    val unit: String? = null,
    val color: Color? = null
)

data class EntityItem(
    val id: Long,
    val name: String,
    val sub: String? = null,
    val sub2: String? = null,
    val badge: String? = null,
    val iconId: Int? = null,
    val colorId: Int? = null,
    val color: Color? = null,
    val parentId: Long? = null,
    val isExpandable: Boolean = false,
    val isExpanded: Boolean = false,
    val trailingContent: (@Composable () -> Unit)? = null
)

data class EntityItemGroup(
    val title: String? = null,
    val items: List<EntityItem>
)
