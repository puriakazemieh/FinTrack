package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.kazemieh.designsystem.component.EmptyListScreen
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazemieh.common.toFa
import com.kazemieh.designsystem.*
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.kazemieh.designsystem.component.*

/**
 * 2.9 EntityList — management list with summary header + search + edit/delete rows
 */
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
    onItemClick: (EntityItem) -> Unit = {},
    emptyHint: String? = null,
    showActions: Boolean = true
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
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
                                    FintrackLabelSmallText(text = s.label, color = GlassText3)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        FintrackTitleMediumText(
                                            text = s.value.toFa(),
                                            fontWeight = FontWeight.Bold,
                                            color = s.color ?: GlassText
                                        )
                                        s.unit?.let {
                                            FintrackLabelSmallText(
                                                text = it,
                                                color = GlassText3,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                }
                                if (index < summary.lastIndex) {
                                    VerticalDivider(
                                        modifier = Modifier.height(30.dp),
                                        color = GlassHairline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = stringResource(Res.string.hint_search_in, title),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            if (items.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                        EmptyListScreen(emptyHint ?: stringResource(Res.string.msg_empty_list))
                    }
                }
            } else {
                items(items) { it ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(200))
                    ) {
                        EntityRow(
                            item = it,
                            mainColor = color,
                            showActions = showActions,
                            onEdit = { onEditClick(it) },
                            onDelete = { onDeleteClick(it) },
                            onClick = { onItemClick(it) },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Fab(
            label = addLabel ?: "افزودن ${title.split(" ").first()} جدید",
            icon = rememberVectorPainter(Icons.Default.Add),
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        )
    }
}

@Composable
private fun EntityRow(
    item: EntityItem,
    mainColor: Color,
    showActions: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = item.color ?: mainColor
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        padding = 10.dp
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FinTrackLeadingIcon(
                    colorId = item.colorId,
                    iconId = item.iconId,
                    style = LeadingIconStyle.Badge,
                    size = 38.dp,
                    iconSize = 16.dp,
                    corner = 12.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FintrackTitleSmallText(
                            text = item.name,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassText
                        )
                        item.badge?.let {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color.copy(alpha = 0.12f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                FintrackLabelSmallText(
                                    text = it,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }
                    item.sub?.let {
                        FintrackBodySmallText(
                            text = it,
                            color = GlassText3,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

            if (showActions) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionIcon(icon = Icons.Default.Edit, onClick = onEdit, color = GlassText2)
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
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(GlassColor)
            .border(1.dp, GlassEdge, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
    }
}

data class EntitySummary(
    val label: String,
    val value: String,
    val unit: String? = null,
    val color: Color? = null
)

data class EntityItem(
    val id: Long,
    val name: String,
    val sub: String? = null,
    val badge: String? = null,
    val iconId: Int? = null,
    val colorId: Int? = null,
    val color: Color? = null
)
