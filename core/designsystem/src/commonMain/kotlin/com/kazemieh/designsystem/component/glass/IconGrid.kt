package com.kazemieh.designsystem.component.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.LocalGlassColors
import com.kazemieh.designsystem.picker.PickableIcon
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconGrid(
    icons: List<PickableIcon>,
    pickedIndex: Int,
    color: Color,
    onIconPick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    LazyHorizontalGrid(
        rows = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(glassColors.glass, RoundedCornerShape(12.dp))
            .border(1.dp, glassColors.glassEdge, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        itemsIndexed(icons) { index, icon ->
            val isSelected = index == pickedIndex
            Box(
                modifier = Modifier
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
                    tint = if (isSelected) color else glassColors.text2,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
