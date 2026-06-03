package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassEdge
import com.kazemieh.designsystem.GlassText2
import com.kazemieh.designsystem.picker.PickableIcon
import org.jetbrains.compose.resources.painterResource

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed

@Composable
fun IconGrid(
    icons: List<PickableIcon>,
    pickedIndex: Int,
    color: Color,
    onIconPick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // Show ~3.5 items to hint scrolling
        val itemWidth = maxWidth / 3.5f

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassColor, RoundedCornerShape(12.dp))
                .border(1.dp, GlassEdge, RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            itemsIndexed(icons) { index, icon ->
                val isSelected = index == pickedIndex
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) color.copy(alpha = 0.14f) else Color.Transparent)
                        .then(
                            if (isSelected) Modifier.border(
                                1.dp,
                                color.copy(alpha = 0.33f),
                                RoundedCornerShape(10.dp)
                            )
                            else Modifier
                        )
                        .clickable { onIconPick(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon.resource),
                        contentDescription = null,
                        tint = if (isSelected) color else GlassText2,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
