package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.GlassColor
import com.kazemieh.designsystem.GlassEdge
import com.kazemieh.designsystem.GlassText
import com.kazemieh.designsystem.picker.bestOnColor

@Composable
fun ColorSwatches(
    colors: List<Color>,
    pickedIndex: Int,
    onColorPick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(GlassColor, RoundedCornerShape(12.dp))
            .border(1.dp, GlassEdge, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        itemsIndexed(colors) { index, color ->
            val isSelected = index == pickedIndex
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) Modifier.border(2.dp, GlassText, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorPick(index) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = bestOnColor(color),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
